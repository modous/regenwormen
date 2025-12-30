<template>
  <div class="game">
    <h1>🎲 Regenwormen</h1>

    <div v-if="!gameReady">
      <p>⏳ Joining game...</p>
      <p v-if="errorMsg" class="err">{{ errorMsg }}</p>
    </div>

    <div v-else>
      <button class="back-button" @click="goToLobby">⬅️ Terug naar Lobby</button>

      <h3>Game ID: {{ gameId }}</h3>
      <h4>Jij: {{ username }}</h4>

      <div class="system-popup" :class="{ visible: !!gameMessage }">
        {{ gameMessage }}
      </div>

      <div
          class="timer-box"
          :class="{ hidden: timeLeft > 9 }"
      >
        <p v-if="currentTimerPlayer === username">
          ⏳ Jouw beurt: <strong>{{ timeLeft }}s</strong> over
        </p>
        <p v-else>
          🧍 {{ currentTimerPlayer }} is aan de beurt ({{ timeLeft }}s)
        </p>
      </div>

      <p v-if="turnInfo" class="turn">Beurt: {{ turnInfo }}</p>

      <div class="game-arena">
        <DiceRoll
            :rolledDice="rolledDice"
            :disabledFaces="disabledFaces"
            :chosenFaces="chosenFaces"
            :isCurrentPlayer="currentPlayerId === username"
            :isBusted="isBusted"
            :rolling="rolling"
            :hasStartedRoll="hasStartedRoll"
            :canRoll="timeLeft > 0 || currentPlayerId !== username"
            @roll="rollDice"
            @selectDie="trySelectDie"
        />

        <!--        <hr class="arena-divider" />-->

        <TilesTable
            :tiles="tilesOnTable"
            :currentPoints="roundPoints"
            :hasWormInCurrentThrow="hasWormInCurrentThrow"
            @pickTile="tryPickTile"
        />
      </div>

      <DiceCollected
          :collectedDice="collectedDice"
          :roundPoints="roundPoints"
          :hasStartedRoll="hasStartedRoll"
      />

      <div class="game-board">
        <TilesCollected
            :tiles="players.find(p => p.name === username)?.tiles || []"
        />


        <div class="others-section">
          <TilesOtherPlayer
              v-for="p in players.filter(pl => pl.name !== username)"
              :key="p.id"
              :playerName="p.name || 'Unknown'"
              :tiles="p.tiles || []"
              :topTile="p.topTile"
              @steal="() => stealTile(p.name)"
          />
        </div>

        <button class="error-button" @click="showErrorForm = true">❗</button>
        <ErrorHandelingForm
            :visible="showErrorForm"
            :gameState="getCurrentGameState()"
            @close="showErrorForm = false"
            @open="showErrorForm = true"
        />

        <button class="help-button" @click="showRules = true">❓</button>
        <HowToPlayButton :visible="showRules" @close="showRules = false" />
      </div>


      <button class="error-button" @click="showErrorForm = true">❗</button>
      <ErrorHandelingForm
          :visible="showErrorForm"
          :gameState="getCurrentGameState()"
          @close="showErrorForm = false"
          @open="showErrorForm = true"
      />

      <button class="help-button" @click="showRules = true">❓</button>
      <HowToPlayButton :visible="showRules" @close="showRules = false" />

      <!-- CHAT COMPONENT -->
      <GameChat
          v-if="gameId && username"
          :chatId="'game_' + gameId"
          :username="username"
      />

    </div>

    <GameEndPopup
        :visible="gameStateEnded"
        :won="playerWon"
        @close="handleGameEndClose"
    />

  </div>

</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from "vue"
import { useRouter } from "vue-router"
import SockJS from "sockjs-client"
import { Client } from "@stomp/stompjs"

import DiceRoll from "./DiceRoll.vue"
import DiceCollected from "./DiceCollected.vue"
import TilesTable from "./TilesTable.vue"
import TilesCollected from "./TilesCollected.vue"
import TilesOtherPlayer from "./TilesOtherPlayer.vue"
import HowToPlayButton from "./game_assistance/HowToPlayButton.vue"
import ErrorHandelingForm from "@/components/Game/game_assistance/ErrorHandelingForm.vue"
import GameChat from "@/components/Game/GameChat.vue"

import "./game.css"
import GameEndPopup from "@/components/Game/GameEndPopup.vue";

const router = useRouter()
const showTimer = ref(false)
const showErrorForm = ref(false)

// API & WebSocket
const API_INGAME = "http://localhost:8080/ingame"
const SOCKJS_URL = "http://localhost:8080/ws"
let stompClient = null

// User / game state
const user = JSON.parse(localStorage.getItem("user"))
const username = user?.username || user?.name || "Guest"
const gameId = ref(localStorage.getItem("gameId") || null)
const gameReady = ref(!!gameId.value)
const errorMsg = ref("")
const showRules = ref(false)
const rolling = ref(false)

// Game data
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

// Timer & messages
const timeLeft = ref(0)
const currentTimerPlayer = ref("")
const gameMessage = ref("")

// Computed
const turnInfo = computed(() => {
  if (!players.value?.length || currentTurnIndex.value == null) return ""
  const p = players.value[currentTurnIndex.value]
  return p ? `${p.name ?? p.id}` : ""
})
const isBusted = computed(() => busted.value)
function playerScore(player) {
  if (!player || !player.tiles || !Array.isArray(player.tiles)) return 0
  return player.tiles.reduce((sum, t) => sum + (t.points || 0), 0)
}

const hasWormInCurrentThrow = computed(() => {
  return collectedDice.value.includes("SPECIAL")
})

// Fetch helper
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



function applyGame(game) {
  if (!game) return
  const previousPlayer = currentPlayerId.value

  // Backend stuurt de volledige players-array en tiles
  players.value = game.players || []

  // Backend stuurt de tiles op tafel
  tilesOnTable.value = (game.tilesPot?.tiles || []).filter(t => t.availableInPot)

  // Beurt info
  currentTurnIndex.value = game.turnIndex ?? null
  currentPlayerId.value = players.value?.[game.turnIndex]?.name || null

  // Reset round alleen als jouw beurt net start
  if (currentPlayerId.value !== previousPlayer && currentPlayerId.value === username) {
    resetRound()
    busted.value = false
    gameMessage.value = "🎯 It's your turn!"
    setTimeout(() => {
      if (gameMessage.value === "🎯 It's your turn!") {
        gameMessage.value = ""
      }
    }, 3000)
  }

  // Ensure message is cleared if it's not my turn
  if (currentPlayerId.value !== username && gameMessage.value === "🎯 It's your turn!") {
    gameMessage.value = ""
  }
}


// WebSocket setup
function connectStomp() {
  const sock = new SockJS(SOCKJS_URL)
  stompClient = new Client({
    debug: () => {},
    reconnectDelay: 500,
    webSocketFactory: () => sock,
  })

  stompClient.onConnect = () => {
    stompClient.subscribe(`/topic/game/${gameId.value}`, msg => {
      try { applyGame(JSON.parse(msg.body)); gameReady.value = true }
      catch (e) { console.warn("Failed parsing game snapshot:", e) }
    })

    stompClient.subscribe(`/topic/game/${gameId.value}/timer`, msg => {
      try {
        const data = JSON.parse(msg.body)
        currentTimerPlayer.value = data.player

        const t = timeLeft
        t.value = data.timeLeft

        if (!showTimer.value && t.value === 10) showTimer.value = true
        if (t.value <= 0) showTimer.value = false
      } catch {}
    })

    stompClient.subscribe(`/topic/game/${gameId.value}/turnTimeout`, msg => {
      try {
        const data = JSON.parse(msg.body)
        timeLeft.value = 0
        gameMessage.value = data.player === username
            ? "⏰ Your turn expired! You lost this round."
            : `⚠️ ${data.player}'s turn expired!`
        if (data.reset) { resetRound(); busted.value = true }
        setTimeout(() => (gameMessage.value = ""), 5000)
      } catch {}
    })

    stompClient.subscribe(`/topic/game/${gameId.value}/message`, msg => {
      try {
        const data = JSON.parse(msg.body)
        gameMessage.value = data.text
        setTimeout(() => (gameMessage.value = ""), 5000)
      } catch {}
    })

    stompClient.publish({ destination: "/app/timerSync", body: gameId.value })
    stompClient.publish({ destination: "/app/sync", body: gameId.value })
  }

  stompClient.onStompError = frame => { console.error("Broker error:", frame.headers["message"]); errorMsg.value = "WebSocket broker error." }
  stompClient.onWebSocketError = e => { console.error("WebSocket error:", e); errorMsg.value = "WebSocket connection failed." }
  stompClient.activate()
}

// Lifecycle
onMounted(() => {
  if (!gameId.value) { errorMsg.value = "No active game found — start one from lobby."; return }
  connectStomp()
})
onUnmounted(() => { if (stompClient) stompClient.deactivate() })

// Game actions
async function rollDice() {
  showTimer.value = false
  rolling.value = true
  try {
    const endpoint = hasStartedRoll.value ? "reroll" : "startroll"
    const data = await post(`${API_INGAME}/${gameId.value}/${endpoint}/${username}`)
    if (!data || data.fullThrow == null) { gameMessage.value = "💀 You busted!"; busted.value = true; resetRound(); return }
    rolledDice.value = Object.entries(data.fullThrow).flatMap(([face, count]) => Array(count).fill(face))
    disabledFaces.value = data.disabledFaces || []
    chosenFaces.value = Array.from(data.chosenFaces || [])
    hasStartedRoll.value = true
  } catch { gameMessage.value = "Something went wrong while rolling dice." }
  finally { rolling.value = false }
}

async function trySelectDie(face) {
  if (disabledFaces.value.includes(face) || chosenFaces.value.includes(face)) return
  try {
    const data = await post(`${API_INGAME}/${gameId.value}/pickdice/${username}`, face)
    if (!data || data.fullThrow == null) { gameMessage.value = "💀 You busted after this pick!"; busted.value = true; resetRound(); return }
    const pickedCount = rolledDice.value.filter(f => f === face).length
    for (let i = 0; i < pickedCount; i++) collectedDice.value.push(face)
    updateRoundPoints()
    rolledDice.value = Object.entries(data.fullThrow || {}).flatMap(([f, count]) => Array(count).fill(f))
    disabledFaces.value = data.disabledFaces || []
    chosenFaces.value = Array.from(data.chosenFaces || [])
  } catch { gameMessage.value = "Failed to select dice face." }
}

// Tile actions
async function tryPickTile(tile) {
  if (roundPoints.value < tile.value) return
  await pickTile(tile)
}



// pickTile() en stealTile() hoeven geen push/filter meer te doen
async function pickTile(tile) {
  try {
    console.log("🟦 FRONTEND pick tile:", tile.value)

    await post(
        `${API_INGAME}/${gameId.value}/claimfrompot/${username}`,
        tile.value   // 🔥 DIT is de keuze
    )

    resetRound()
    busted.value = false
  } catch (e) {
    console.error("❌ PICK TILE FAILED", e)
    gameMessage.value = "Failed to claim tile."
  }
}

async function stealTile(victimName) {
  try {
    await post(
        `${API_INGAME}/${gameId.value}/stealFromPlayer/${username}`,
        victimName
    )
  } catch (e) {
    console.error("STEAL FAILED", e)
    gameMessage.value = "Failed to steal tile."
  }
}



// Helpers
function updateRoundPoints() {
  const faceValue = { ONE: 1, TWO: 2, THREE: 3, FOUR: 4, FIVE: 5, SPECIAL: 5 }
  const counts = collectedDice.value.reduce((acc, f) => { acc[f] = (acc[f] || 0) + 1; return acc }, {})
  roundPoints.value = Object.entries(counts).reduce((total, [face, count]) => total + (faceValue[face] || 0) * count, 0)
}

function resetRound() {
  rolledDice.value = []
  collectedDice.value = []
  disabledFaces.value = []
  chosenFaces.value = []
  hasStartedRoll.value = false
  roundPoints.value = 0
}

function leaveGame() {
  // Notify backend of disconnect
  if (gameId.value) {
    const url = `http://localhost:8080/pregame/${gameId.value}/disconnect/${username}`
    navigator.sendBeacon(url)
  }
  if (stompClient) stompClient.deactivate()
  router.push("/lobbies")
}

function goToLobby() { leaveGame() }

// --- 🎮 GET GAME STATE FOR ERROR REPORT ---
function getCurrentGameState() {
  return {
    gameId: gameId.value,
    currentPlayer: currentPlayerId.value,
    currentTurnIndex: currentTurnIndex.value,
    players: players.value,
    tilesOnTable: tilesOnTable.value,
    rolledDice: rolledDice.value,
    collectedDice: collectedDice.value,
    roundPoints: roundPoints.value,
    myTiles: myTiles.value,
    timeLeft: timeLeft.value,
    hasStartedRoll: hasStartedRoll.value,
    busted: busted.value,
  }
}

// --- 🎮 Endgame screen ----
const gameStateEnded = ref(false)
const playerWon = ref(false)

function calculatePoints(tiles) {
  return tiles.reduce((sum, t) => sum + (t.points || 0), 0)
}

// Watch tilesOnTable en players om einde spel te detecteren
watch([tilesOnTable, players], ([newTiles, newPlayers]) => {
  if (!newTiles || !newPlayers) return

  if (newTiles.length === 0 && !gameStateEnded.value) {
    gameStateEnded.value = true

    // Vind jouw punten
    const me = newPlayers.find(p => p.name === username)
    const myPoints = calculatePoints(me?.tiles || [])

    // Vind hoogste score van andere spelers
    const otherPoints = Math.max(...newPlayers.filter(p => p.name !== username).map(p => calculatePoints(p.tiles || [])))

    playerWon.value = myPoints > otherPoints
  }
})

function handleGameEndClose() {
  gameStateEnded.value = false
  leaveGame()
}

</script>
