<template>
  <div class="game">
    <h1>🎲 Regenwormen</h1>

    <!-- Display while joining/loading game -->
    <div v-if="!gameReady">
      <p>⏳ Joining game...</p>
      <p v-if="errorMsg" class="err">{{ errorMsg }}</p>
    </div>

    <div v-else>
      <!-- Navigation -->
      <button class="back-button" @click="goToLobby">⬅️ Terug naar Lobby</button>

      <!-- Game info -->
      <h3>Game ID: {{ gameId }}</h3>
      <h4>Jij: {{ username }}</h4>

      <!-- System messages from backend -->
      <div v-if="gameMessage" class="system-msg">
        {{ gameMessage }}
      </div>

      <!-- Turn timer -->
      <div class="timer-box" v-if="timeLeft > 0 || currentTimerPlayer">
        <p v-if="currentTimerPlayer === username">
          ⏳ Jouw beurt: <strong>{{ timeLeft }}s</strong> over
        </p>
        <p v-else>
          🧍 {{ currentTimerPlayer }} is aan de beurt ({{ timeLeft }}s)
        </p>
      </div>

      <!-- Turn info -->
      <p v-if="turnInfo" class="turn">Beurt: {{ turnInfo }}</p>

      <!-- Points for current round -->
      <p v-if="hasStartedRoll" class="points">
        Points this round: <strong>{{ roundPoints }}</strong>
      </p>

      <!-- Roll button (only for current player) -->
      <button
          v-if="currentPlayerId === username && !isBusted"
          class="roll-btn"
          @click="rollDice"
          :disabled="rolling || (timeLeft <= 0 && currentPlayerId !== username)"
      >
        {{ hasStartedRoll ? "Roll Again" : "🎲 Roll Dice" }}
      </button>

      <!-- Dice display and selection -->
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

      <!-- Collected dice summary -->
      <DiceCollected :collectedDice="collectedDice" />

      <!-- Tiles available to claim -->
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

      <!-- Player info: self and others -->
      <div class="game-board">
        <div class="my-section">
          <h3>Mijn Tegels</h3>
          <div class="my-tiles-list">
            <div v-for="t in myTiles" :key="t.value" class="my-tile">
              {{ t.value }} <small>🪱 x{{ t.points }}</small>
            </div>
          </div>
          <p class="my-score">Totale punten: <strong>{{ myTilesScore }}</strong></p>
        </div>

        <div class="others-section">
          <div
              v-for="p in players.filter(pl => pl.name !== username)"
              :key="p?.id || p?.name"
              class="other-player"
          >
            <h4>{{ p.name || 'Unknown' }}</h4>
            <p>Total Tile Points: <strong>{{ p.points ?? playerScore(p) }}</strong></p>
            <TilesOtherPlayer :tiles="p.tiles || []" :topTile="p.topTile" />
          </div>
        </div>
      </div>

      <!-- Help/Rules popup -->
      <button class="help-button" @click="showRules = true">❓</button>
      <HowToPlayButton :visible="showRules" @close="showRules = false" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from "vue"
import { useRouter } from "vue-router"
import DiceCollected from "./DiceCollected.vue"
import TilesCollected from "./TilesCollected.vue"
import TilesOtherPlayer from "./TilesOtherPlayer.vue"
import HowToPlayButton from "./HowToPlayButton.vue"
import SockJS from "sockjs-client"
import { Client } from "@stomp/stompjs"

const router = useRouter()

// -----------------------------
// Constants / API Endpoints
// -----------------------------
const API_INGAME = "http://localhost:8080/ingame"
const SOCKJS_URL = "http://localhost:8080/ws"

let stompClient = null

// -----------------------------
// User & Game State
// -----------------------------
const user = JSON.parse(localStorage.getItem("user"))
const username = user?.username || user?.name || "Guest"
const gameId = ref(localStorage.getItem("gameId") || null)
const gameReady = ref(!!gameId.value)
const errorMsg = ref("")
const showRules = ref(false)
const rolling = ref(false)

// -----------------------------
// Game Data
// -----------------------------
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

// -----------------------------
// Timer & system messages
// -----------------------------
const timeLeft = ref(0)
const currentTimerPlayer = ref("")
const gameMessage = ref("")

// -----------------------------
// Computed properties
// -----------------------------
const turnInfo = computed(() => {
  if (!players.value?.length || currentTurnIndex.value == null) return ""
  const p = players.value[currentTurnIndex.value]
  return p ? `${p.name ?? p.id}` : ""
})

const isBusted = computed(() => busted.value)

const myTilesScore = computed(() =>
    myTiles.value.reduce((sum, t) => sum + (t.points || 0), 0)
)

// -----------------------------
// Utility: calculate score for any player
// -----------------------------
function playerScore(player) {
  if (!player || !player.tiles || !Array.isArray(player.tiles)) return 0
  return player.tiles.reduce((sum, t) => sum + (t.points || 0), 0)
}

// -----------------------------
// Helper for POST requests
// -----------------------------
async function post(url, body = null) {
  const res = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: body ? JSON.stringify(body) : null,
  })

  if (!res.ok) {
    const t = await res.text().catch(() => "")
    throw new Error(`Backend error ${res.status}: ${t}`)
  }

  const type = res.headers.get("content-type") || ""
  return type.includes("application/json") ? res.json() : null
}

// -----------------------------
// Apply game snapshot from WebSocket
// -----------------------------
function applyGame(game) {
  if (!game) return

  const previousPlayer = currentPlayerId.value

  players.value = game.players || []
  tilesOnTable.value = game.tilesPot?.tiles || []
  currentTurnIndex.value = game.turnIndex ?? null
  currentPlayerId.value = players.value?.[game.turnIndex]?.name || null

  // Reset UI if it's now this player's turn
  if (currentPlayerId.value !== previousPlayer && currentPlayerId.value === username) {
    resetRound()
    busted.value = false
    gameMessage.value = "It's your turn!"
  }

  // Sync current player's tiles
  const me = players.value.find(p => p.name === username || p.id === username)
  myTiles.value = Array.isArray(me?.tiles) ? me.tiles : myTiles.value
}

// -----------------------------
// WebSocket (STOMP/SockJS) setup
// -----------------------------
function connectStomp() {
  const sock = new SockJS(SOCKJS_URL)
  stompClient = new Client({
    debug: () => {},
    reconnectDelay: 500,
    webSocketFactory: () => sock,
  })

  stompClient.onConnect = () => {
    // Subscribe to game state updates
    stompClient.subscribe(`/topic/game/${gameId.value}`, (msg) => {
      try {
        const game = JSON.parse(msg.body)
        applyGame(game)
        gameReady.value = true
      } catch (e) {
        console.warn("Failed parsing game snapshot:", e)
      }
    })

    // Subscribe to timer updates
    stompClient.subscribe(`/topic/game/${gameId.value}/timer`, (msg) => {
      try {
        const data = JSON.parse(msg.body)
        currentTimerPlayer.value = data.player
        timeLeft.value = data.timeLeft
      } catch {}
    })

    // Subscribe to turn timeout events
    stompClient.subscribe(`/topic/game/${gameId.value}/turnTimeout`, (msg) => {
      try {
        const data = JSON.parse(msg.body)
        timeLeft.value = 0

        if (data.player === username) {
          gameMessage.value = "Your turn expired! You lost this round."
        } else {
          gameMessage.value = `${data.player}'s turn expired!`
        }

        if (data.reset) {
          resetRound()
          busted.value = true
        }

        setTimeout(() => (gameMessage.value = ""), 5000)
      } catch {
        console.warn("Failed to process turn timeout")
      }
    })

    // Subscribe to system messages
    stompClient.subscribe(`/topic/game/${gameId.value}/message`, (msg) => {
      try {
        const data = JSON.parse(msg.body)
        gameMessage.value = data.text
        setTimeout(() => (gameMessage.value = ""), 5000)
      } catch {
        console.warn("Invalid system message")
      }
    })

    // Request initial game state
    stompClient.publish({ destination: "/app/timerSync", body: gameId.value })
    stompClient.publish({ destination: "/app/sync", body: gameId.value })
  }

  stompClient.onStompError = (frame) => {
    console.error("Broker error:", frame.headers["message"])
    errorMsg.value = "WebSocket broker error."
  }

  stompClient.onWebSocketError = (e) => {
    console.error("WebSocket error:", e)
    errorMsg.value = "WebSocket connection failed."
  }

  stompClient.activate()
}

// -----------------------------
// Lifecycle hooks
// -----------------------------
onMounted(() => {
  if (!gameId.value) {
    errorMsg.value = "No active game found — start one from lobby."
    return
  }
  connectStomp()
})

onUnmounted(() => {
  if (stompClient) stompClient.deactivate()
})

// -----------------------------
// Game Actions
// -----------------------------
async function rollDice() {
  rolling.value = true
  try {
    const endpoint = hasStartedRoll.value ? "reroll" : "startroll"
    const data = await post(`${API_INGAME}/${gameId.value}/${endpoint}/${username}`)
    if (!data || data.fullThrow == null) {
      gameMessage.value = "You busted! Your turn is over."
      busted.value = true
      resetRound()
      return
    }

    // Update dice state
    rolledDice.value = Object.entries(data.fullThrow).flatMap(([face, count]) =>
        Array(count).fill(face)
    )
    disabledFaces.value = data.disabledFaces || []
    chosenFaces.value = Array.from(data.chosenFaces || [])
    hasStartedRoll.value = true
  } catch {
    gameMessage.value = "Something went wrong while rolling dice."
  } finally {
    rolling.value = false
  }
}

async function trySelectDie(face) {
  if (disabledFaces.value.includes(face) || chosenFaces.value.includes(face)) return

  try {
    const data = await post(`${API_INGAME}/${gameId.value}/pickdice/${username}`, face)
    if (!data || data.fullThrow == null) {
      gameMessage.value = "You busted after this pick! Turn over."
      busted.value = true
      resetRound()
      return
    }

    // Update collected dice and remaining dice
    const pickedCount = rolledDice.value.filter(f => f === face).length
    for (let i = 0; i < pickedCount; i++) collectedDice.value.push(face)
    updateRoundPoints()
    rolledDice.value = Object.entries(data.fullThrow || {}).flatMap(([f, count]) =>
        Array(count).fill(f)
    )
    disabledFaces.value = data.disabledFaces || []
    chosenFaces.value = Array.from(data.chosenFaces || [])
  } catch {
    gameMessage.value = "Failed to select dice face."
  }
}

// -----------------------------
// Tile selection logic
// -----------------------------
function canClaim(tile) {
  return roundPoints.value >= tile.value
}

async function tryPickTile(tile) {
  if (!canClaim(tile)) return
  await pickTile(tile)
}

async function pickTile(tile) {
  try {
    await post(`${API_INGAME}/${gameId.value}/claimfrompot/${username}`)
    resetRound()
    busted.value = false
  } catch {
    gameMessage.value = "Failed to claim tile."
  }
}

// -----------------------------
// Utility functions
// -----------------------------
function updateRoundPoints() {
  const faceValue = { ONE: 1, TWO: 2, THREE: 3, FOUR: 4, FIVE: 5, SPECIAL: 5 }
  const counts = collectedDice.value.reduce((acc, f) => {
    acc[f] = (acc[f] || 0) + 1
    return acc
  }, {})
  roundPoints.value = Object.entries(counts).reduce(
      (total, [face, count]) => total + (faceValue[face] || 0) * count,
      0
  )
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

function goToLobby() {
  router.push("/lobbies")
}
</script>
