<template>
  <div class="game">
    <h1>🎲 Regenwormen</h1>

    <div v-if="!gameReady">
      <p>⏳ Joining game...</p>
      <p v-if="errorMsg" class="err">{{ errorMsg }}</p>
    </div>

    <div v-else>
      <h3>Game ID: {{ gameId }}</h3>
      <h4>Jij: {{ username }}</h4>
      <p v-if="turnInfo" class="turn">Beurt: {{ turnInfo }}</p>

      <!-- 🧮 Points counter -->
      <p v-if="hasStartedRoll" class="points">
        🎯 Points this round: <strong>{{ roundPoints }}</strong>
      </p>

      <!-- 🎲 Roll button -->
      <button
          v-if="currentPlayerId === username && !isBusted"
          class="roll-btn"
          @click="rollDice"
          :disabled="rolling"
      >
        {{ hasStartedRoll ? "🎯 Roll Again" : "🎲 Roll Dice" }}
      </button>

      <!-- 🎲 Dice area -->
      <div class="dice-area" v-if="rolledDice.length">
        <div
            v-for="(face, idx) in rolledDice"
            :key="idx"
            :class="[
            'die',
            { disabled: disabledFaces.includes(face), chosen: chosenFaces.includes(face) }
          ]"
            @click="trySelectDie(face)"
        >
          {{ faceEmoji(face) }}
        </div>
      </div>

      <!-- 🧱 Collected dice summary -->
      <DiceCollected :collectedDice="collectedDice" />

      <!-- 🧩 Tiles to claim -->
      <div class="tiles-table">
        <div
            v-for="tile in tilesOnTable"
            :key="tile.value"
            :class="['tile', { disabled: !canClaim(tile) }]"
            @click="tryPickTile(tile)"
        >
          <span>{{ tile.value }}</span>
          <span class="worms">🪱 x{{ tile.points || 1 }}</span>
        </div>
      </div>

      <!-- 👤 My tiles & score -->
      <div class="my-tiles" v-if="myTiles.length">
        <h3>🧱 My Tiles</h3>
        <div class="my-tiles-list">
          <div
              v-for="t in myTiles"
              :key="t.value"
              class="my-tile"
          >
            {{ t.value }} <small>🪱 x{{ t.points }}</small>
          </div>
        </div>
        <p class="my-score">💰 Total Tile Points: <strong>{{ myTilesScore }}</strong></p>
      </div>

      <!-- 👥 Players overview -->
      <div class="players">
        <TilesCollected
            v-for="p in players"
            :key="p.id"
            :tiles="p.tiles || []"
            :isCurrentPlayer="p.name === currentPlayerId"
        />
      </div>

      <button class="help-button" @click="showRules = true">❓</button>
      <HowToPlayButton :visible="showRules" @close="showRules = false" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from "vue"
import DiceCollected from "./DiceCollected.vue"
import TilesCollected from "./TilesCollected.vue"
import HowToPlayButton from "./HowToPlayButton.vue"

const API_INGAME = "http://localhost:8080/ingame"

const user = JSON.parse(localStorage.getItem("user"))
const username = user?.username || user?.name || "Guest"

const gameId = ref(localStorage.getItem("gameId") || null)
const gameReady = ref(!!gameId.value)
const errorMsg = ref("")
const showRules = ref(false)
const rolling = ref(false)

const rolledDice = ref([])
const disabledFaces = ref([])
const chosenFaces = ref([])
const collectedDice = ref([])
const tilesOnTable = ref([])
const players = ref([])
const currentPlayerId = ref(null)
const currentTurnIndex = ref(null)
const hasStartedRoll = ref(false)
const busted = ref(false)
const roundPoints = ref(0)
const myTiles = ref([])

let pollInterval = null
let pollPaused = false

const turnInfo = computed(() => {
  if (!players.value?.length || currentTurnIndex.value == null) return ""
  const p = players.value[currentTurnIndex.value]
  return p ? `${p.name ?? p.id}` : ""
})

const isBusted = computed(() => busted.value)

// ✅ FIXED: sum worm points, not tile values
const myTilesScore = computed(() =>
    myTiles.value.reduce((sum, t) => sum + (t.points || 0), 0)
)

// === FETCH HELPERS ===
async function get(url) {
  const res = await fetch(url)
  if (!res.ok) throw new Error(`GET failed: ${res.status}`)
  return res.json()
}

async function post(url, body = null) {
  const res = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: body ? JSON.stringify(body) : null,
  })
  if (!res.ok) {
    const text = await res.text()
    throw new Error(`Backend error ${res.status}: ${text}`)
  }
  const type = res.headers.get("content-type") || ""
  return type.includes("application/json") ? res.json() : null
}

// === 🔁 GAME STATE POLLING ===
async function refreshGameState() {
  if (!gameId.value || pollPaused) return
  try {
    const game = await get(`${API_INGAME}/${gameId.value}`)
    players.value = game.players || []
    tilesOnTable.value = game.tilesPot?.tiles || []
    currentTurnIndex.value = game.turnIndex ?? null
    currentPlayerId.value = players.value[game.turnIndex]?.name ?? null

    const me = players.value.find(p => p.name === username || p.id === username)
    if (me?.tiles && Array.isArray(me.tiles) && me.tiles.length > 0) {
      const backendVals = me.tiles.map(t => t.value)
      const localVals = myTiles.value.map(t => t.value)
      const merged = [
        ...myTiles.value,
        ...me.tiles.filter(t => !localVals.includes(t.value)),
      ]
      if (merged.length !== myTiles.value.length) myTiles.value = merged
    }
  } catch (err) {
    console.warn("Could not refresh game state:", err.message)
  }
}

// === LIFECYCLE ===
onMounted(async () => {
  if (!gameId.value) {
    errorMsg.value = "No active game found — start one from lobby."
    return
  }
  gameReady.value = true
  await refreshGameState()
  pollInterval = setInterval(refreshGameState, 2000)
})

onUnmounted(() => clearInterval(pollInterval))

// === 🎲 DICE ACTIONS ===
async function rollDice() {
  rolling.value = true
  try {
    const endpoint = hasStartedRoll.value ? "reroll" : "startroll"
    const data = await post(`${API_INGAME}/${gameId.value}/${endpoint}/${username}`)
    if (!data || data.fullThrow == null) {
      alert("💀 You busted! Your turn is over.")
      busted.value = true
      resetRound()
      return
    }

    rolledDice.value = Object.entries(data.fullThrow).flatMap(([face, count]) =>
        Array(count).fill(face)
    )
    disabledFaces.value = data.disabledFaces || []
    chosenFaces.value = Array.from(data.chosenFaces || [])
    hasStartedRoll.value = true
  } catch {
    alert("Something went wrong while rolling dice.")
  } finally {
    rolling.value = false
  }
}

async function trySelectDie(face) {
  if (disabledFaces.value.includes(face)) return
  if (chosenFaces.value.includes(face)) return

  try {
    const data = await post(`${API_INGAME}/${gameId.value}/pickdice/${username}`, face)
    if (!data || data.fullThrow == null) {
      alert("💀 You busted after this pick! Turn over.")
      busted.value = true
      resetRound()
      return
    }

    const pickedCount = rolledDice.value.filter(f => f === face).length
    for (let i = 0; i < pickedCount; i++) collectedDice.value.push(face)

    updateRoundPoints()

    rolledDice.value = Object.entries(data.fullThrow || {}).flatMap(([f, count]) =>
        Array(count).fill(f)
    )
    disabledFaces.value = data.disabledFaces || []
    chosenFaces.value = Array.from(data.chosenFaces || [])
  } catch {
    alert("Failed to select dice face.")
  }
}

// === 🧩 TILE LOGIC ===
function canClaim(tile) {
  return roundPoints.value >= tile.value
}

function tryPickTile(tile) {
  if (!canClaim(tile)) return
  pickTile(tile)
}

async function pickTile(tile) {
  try {
    pollPaused = true

    const game = await post(`${API_INGAME}/${gameId.value}/claimfrompot/${username}`)

    if (!myTiles.value.find(t => t.value === tile.value)) {
      myTiles.value.push(tile)
    }

    tilesOnTable.value = tilesOnTable.value.filter(t => t.value !== tile.value)

    players.value = game.players || []
    currentTurnIndex.value = game.turnIndex ?? null
    currentPlayerId.value = players.value[game.turnIndex]?.name ?? null

    resetRound()
    busted.value = false

    setTimeout(() => {
      pollPaused = false
      refreshGameState()
    }, 1000)
  } catch (e) {
    alert("Failed to claim tile.")
    pollPaused = false
  }
}

// === 🧮 HELPERS ===
function updateRoundPoints() {
  const faceValue = { ONE: 1, TWO: 2, THREE: 3, FOUR: 4, FIVE: 5, SPECIAL: 5 }
  const counts = collectedDice.value.reduce((acc, f) => {
    acc[f] = (acc[f] || 0) + 1
    return acc
  }, {})
  roundPoints.value = Object.entries(counts)
      .reduce((total, [face, count]) => total + (faceValue[face] || 0) * count, 0)
}

function resetRound() {
  rolledDice.value = []
  collectedDice.value = []
  disabledFaces.value = []
  chosenFaces.value = []
  hasStartedRoll.value = false
  roundPoints.value = 0
}

function faceEmoji(face) {
  const map = { ONE: "1️⃣", TWO: "2️⃣", THREE: "3️⃣", FOUR: "4️⃣", FIVE: "5️⃣", SPECIAL: "🪱" }
  return map[face] || face
}
</script>

<style scoped>
.game {
  background: #fafafa;
  color: #111;
  min-height: 100vh;
  padding: 2rem;
  text-align: center;
  font-family: "Inter", sans-serif;
}

h1, h3, h4, p { color: #111; }
.points { margin: 0.5rem 0 1rem; font-weight: 700; color: #2c7a2c; font-size: 1.2rem; }

.roll-btn {
  background: #4caf50;
  color: white;
  border: none;
  padding: 12px 25px;
  font-size: 1.2rem;
  border-radius: 10px;
  cursor: pointer;
  margin-top: 1rem;
}
.roll-btn:hover { background: #43a047; transform: scale(1.05); }
.roll-btn:disabled { background: #aaa; cursor: not-allowed; }

.dice-area { display: flex; justify-content: center; flex-wrap: wrap; gap: 1rem; margin: 1.5rem 0; }
.die { font-size: 2.2rem; width: 55px; height: 55px; border-radius: 12px; display: flex; align-items: center; justify-content: center; background: #fff; box-shadow: 0 2px 5px rgba(0,0,0,0.2); cursor: pointer; transition: all 0.2s ease; }
.die.disabled { opacity: 0.4; cursor: not-allowed; background: #ddd; }
.die.chosen { background: #e3f8d3; border: 2px solid #4caf50; }

.tiles-table { display: flex; justify-content: center; flex-wrap: wrap; gap: 0.8rem; margin-top: 1rem; }
.tile { background: #fff; border: 2px solid #4caf50; border-radius: 10px; padding: 10px 15px; cursor: pointer; font-weight: bold; transition: all 0.2s ease; }
.tile:hover { background: #e8f5e9; transform: scale(1.05); }
.tile.disabled { border-color: #aaa; background: #f3f3f3; color: #999; cursor: not-allowed; transform: none; }
.worms { display: block; font-size: 0.8rem; color: #555; }

.my-tiles { margin-top: 2rem; }
.my-tiles-list { display: flex; justify-content: center; gap: 0.5rem; flex-wrap: wrap; }
.my-tile { background: #fefefe; border: 2px solid #2196f3; border-radius: 8px; padding: 6px 12px; font-weight: 600; box-shadow: 0 2px 4px rgba(0,0,0,0.15); }
.my-tile small { font-size: 0.8rem; margin-left: 4px; color: #444; }
.my-score { font-weight: 700; color: #1565c0; margin-top: 0.5rem; }

.players { display: flex; justify-content: center; flex-wrap: wrap; gap: 1rem; margin-top: 2rem; }
.help-button { position: fixed; bottom: 20px; right: 20px; background: #eee; border: none; border-radius: 50%; width: 45px; height: 45px; font-size: 22px; cursor: pointer; color: #333; box-shadow: 0 4px 10px rgba(0,0,0,0.25); }
.help-button:hover { background: #ddd; transform: scale(1.05); }
.err { color: #b00020; margin-top: .5rem; font-weight: 600; }
.turn { margin: .25rem 0 1rem; font-weight: 600; color: #333; }
</style>
