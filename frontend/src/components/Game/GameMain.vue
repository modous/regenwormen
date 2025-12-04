<!--<template>-->
<!--  <div class="game">-->
<!--    <h1>🎲 Regenwormen</h1>-->

<!--    <div v-if="!gameReady">-->
<!--      <p>⏳ Joining game...</p>-->
<!--      <p v-if="errorMsg" class="err">{{ errorMsg }}</p>-->
<!--    </div>-->

<!--    <div v-else>-->
<!--      <button class="back-button" @click="goToLobby">⬅️ Terug naar Lobby</button>-->

<!--      <h3>Game ID: {{ gameId }}</h3>-->
<!--      <h4>Jij: {{ username }}</h4>-->

<!--      &lt;!&ndash; 💬 System messages &ndash;&gt;-->
<!--      <div v-if="gameMessage" class="system-msg">-->
<!--        {{ gameMessage }}-->
<!--      </div>-->

<!--      &lt;!&ndash; 🕒 Turn timer box (always visible) &ndash;&gt;-->
<!--      <div class="timer-box" v-if="timeLeft > 0 || currentTimerPlayer">-->
<!--        <p v-if="currentTimerPlayer === username">-->
<!--          ⏳ Jouw beurt: <strong>{{ timeLeft }}s</strong> over-->
<!--        </p>-->
<!--        <p v-else>-->
<!--          🧍 {{ currentTimerPlayer }} is aan de beurt ({{ timeLeft }}s)-->
<!--        </p>-->
<!--      </div>-->

<!--      <p v-if="turnInfo" class="turn">Beurt: {{ turnInfo }}</p>-->

<!--      &lt;!&ndash; 🧮 Points counter &ndash;&gt;-->
<!--      <p v-if="hasStartedRoll" class="points">-->
<!--        🎯 Points this round: <strong>{{ roundPoints }}</strong>-->
<!--      </p>-->

<!--      &lt;!&ndash; 🎲 Roll button &ndash;&gt;-->
<!--      <button-->
<!--          v-if="currentPlayerId === username && !isBusted"-->
<!--          class="roll-btn"-->
<!--          @click="rollDice"-->
<!--          :disabled="rolling || (timeLeft <= 0 && currentPlayerId !== username)"-->
<!--      >-->
<!--        {{ hasStartedRoll ? "Roll Again" : "🎲 Roll Dice" }}-->
<!--      </button>-->

<!--      &lt;!&ndash; 🎲 Dice area &ndash;&gt;-->
<!--      <div class="dice-area" v-if="rolledDice.length">-->
<!--        <div-->
<!--            v-for="(face, idx) in rolledDice"-->
<!--            :key="idx"-->
<!--            :class="[-->
<!--            'die',-->
<!--            { disabled: disabledFaces.includes(face), chosen: chosenFaces.includes(face) }-->
<!--          ]"-->
<!--            @click="trySelectDie(face)"-->
<!--        >-->
<!--          {{ faceEmoji(face) }}-->
<!--        </div>-->
<!--      </div>-->

<!--      &lt;!&ndash; 🧱 Collected dice summary &ndash;&gt;-->
<!--      <DiceCollected :collectedDice="collectedDice" />-->

<!--      &lt;!&ndash; 🧩 Tiles to claim &ndash;&gt;-->
<!--      <div class="tiles-table">-->
<!--        <div-->
<!--            v-for="tile in tilesOnTable"-->
<!--            :key="tile.value"-->
<!--            :class="['tile', { disabled: !canClaim(tile) }]"-->
<!--            @click="tryPickTile(tile)"-->
<!--        >-->
<!--          <span>{{ tile.value }}</span>-->
<!--          <span class="worms">🪱 x{{ tile.points || 1 }}</span>-->
<!--        </div>-->
<!--      </div>-->

<!--      &lt;!&ndash; 👤 & 👥 Players section &ndash;&gt;-->
<!--      <div class="game-board">-->
<!--        &lt;!&ndash; 👤 My tiles & score &ndash;&gt;-->
<!--        <div class="my-section">-->
<!--          <h3>Mijn Tegels</h3>-->
<!--          <div class="my-tiles-list">-->
<!--            <div v-for="t in myTiles" :key="t.value" class="my-tile">-->
<!--              {{ t.value }} <small>🪱 x{{ t.points }}</small>-->
<!--            </div>-->
<!--          </div>-->
<!--          <p class="my-score">Totale punten: <strong>{{ myTilesScore }}</strong></p>-->
<!--        </div>-->

<!--        &lt;!&ndash; 👥 Other players &ndash;&gt;-->
<!--        <div class="others-section">-->
<!--          <div-->
<!--              v-for="p in players.filter(pl => pl.name !== username)"-->
<!--              :key="p?.id || p?.name"-->
<!--              class="other-player"-->
<!--          >-->
<!--            <h4>{{ p.name || 'Unknown' }}</h4>-->
<!--            <p>Total Tile Points: <strong>{{ p.points ?? playerScore(p) }}</strong></p>-->
<!--            <TilesOtherPlayer :tiles="p.tiles || []" :topTile="p.topTile" />-->
<!--          </div>-->
<!--        </div>-->
<!--      </div>-->
<!--      <button class="error-button" @click="showErrorForm = true">❗</button>-->
<!--      <ErrorHandelingForm-->
<!--        :visible="showErrorForm"-->
<!--        :gameState="getCurrentGameState()"-->
<!--        @close="showErrorForm = false"-->
<!--        @open="showErrorForm = true"-->
<!--      />-->

<!--      <button class="help-button" @click="showRules = true">❓</button>-->
<!--      <HowToPlayButton :visible="showRules" @close="showRules = false" />-->
<!--    </div>-->
<!--  </div>-->
<!--</template>-->

<!--<script setup>-->
<!--import { ref, computed, onMounted, onUnmounted } from "vue"-->
<!--import { useRouter } from "vue-router"-->
<!--import DiceCollected from "./DiceCollected.vue"-->
<!--import TilesCollected from "./TilesCollected.vue"-->
<!--import ErrorHandelingForm from "@/components/Game/game_assistance/ErrorHandelingForm.vue";-->
<!--import TilesOtherPlayer from "./TilesOtherPlayer.vue"-->
<!--import HowToPlayButton from "./game_assistance/HowToPlayButton.vue"-->
<!--import SockJS from "sockjs-client"-->
<!--import { Client } from "@stomp/stompjs"-->

<!--const router = useRouter()-->

<!--// -&#45;&#45; 🔗 API + STOMP/SockJS -&#45;&#45;-->
<!--const API_INGAME = "http://localhost:8080/ingame"-->
<!--const SOCKJS_URL = "http://localhost:8080/ws"-->

<!--let stompClient = null-->

<!--// -&#45;&#45; USER / GAME STATE -&#45;&#45;-->
<!--const user = JSON.parse(localStorage.getItem("user"))-->
<!--const username = user?.username || user?.name || "Guest"-->
<!--const gameId = ref(localStorage.getItem("gameId") || null)-->
<!--const gameReady = ref(!!gameId.value)-->
<!--const errorMsg = ref("")-->
<!--const showRules = ref(false)-->
<!--const showErrorForm = ref(false)-->
<!--const rolling = ref(false)-->

<!--// -&#45;&#45; GAME DATA -&#45;&#45;-->
<!--const rolledDice = ref([])-->
<!--const disabledFaces = ref([])-->
<!--const chosenFaces = ref([])-->
<!--const collectedDice = ref([])-->
<!--const tilesOnTable = ref([])-->
<!--const players = ref([])-->
<!--const currentPlayerId = ref(null)-->
<!--const currentTurnIndex = ref(null)-->
<!--const hasStartedRoll = ref(false)-->
<!--const busted = ref(false)-->
<!--const roundPoints = ref(0)-->
<!--const myTiles = ref([])-->

<!--// -&#45;&#45; ⏳ TIMER & MESSAGE STATE -&#45;&#45;-->
<!--const timeLeft = ref(0)-->
<!--const currentTimerPlayer = ref("")-->
<!--const gameMessage = ref("") // 💬 from backend (/message)-->

<!--// -&#45;&#45; COMPUTED -&#45;&#45;-->
<!--const turnInfo = computed(() => {-->
<!--  if (!players.value?.length || currentTurnIndex.value == null) return ""-->
<!--  const p = players.value[currentTurnIndex.value]-->
<!--  return p ? `${p.name ?? p.id}` : ""-->
<!--})-->
<!--const isBusted = computed(() => busted.value)-->

<!--const myTilesScore = computed(() =>-->
<!--    myTiles.value.reduce((sum, t) => sum + (t.points || 0), 0)-->
<!--)-->

<!--function playerScore(player) {-->
<!--  if (!player || !player.tiles || !Array.isArray(player.tiles)) return 0-->
<!--  return player.tiles.reduce((sum, t) => sum + (t.points || 0), 0)-->
<!--}-->

<!--// === FETCH HELPERS ===-->
<!--async function post(url, body = null) {-->
<!--  const res = await fetch(url, {-->
<!--    method: "POST",-->
<!--    headers: { "Content-Type": "application/json" },-->
<!--    body: body ? JSON.stringify(body) : null,-->
<!--  })-->
<!--  if (!res.ok) {-->
<!--    const t = await res.text().catch(() => "")-->
<!--    throw new Error(`Backend error ${res.status}: ${t}`)-->
<!--  }-->
<!--  const type = res.headers.get("content-type") || ""-->
<!--  return type.includes("application/json") ? res.json() : null-->
<!--}-->

<!--// === 🧠 Apply game snapshot from WS ===-->
<!--function applyGame(game) {-->
<!--  if (!game) return-->

<!--  const previousPlayer = currentPlayerId.value-->

<!--  players.value = game.players || []-->
<!--  tilesOnTable.value = game.tilesPot?.tiles || []-->
<!--  currentTurnIndex.value = game.turnIndex ?? null-->
<!--  currentPlayerId.value = players.value?.[game.turnIndex]?.name || null-->

<!--  // 🧩 If it's a new round and now your turn — reset your UI-->
<!--  if (currentPlayerId.value !== previousPlayer && currentPlayerId.value === username) {-->
<!--    resetRound()-->
<!--    busted.value = false-->
<!--    gameMessage.value = "🎯 It's your turn!"-->
<!--  }-->

<!--  // Sync my tiles from snapshot-->
<!--  const me = players.value.find(p => p.name === username || p.id === username)-->
<!--  myTiles.value = Array.isArray(me?.tiles) ? me.tiles : myTiles.value-->
<!--}-->


<!--// === 🔌 STOMP/SockJS SETUP ===-->
<!--function connectStomp() {-->
<!--  const sock = new SockJS(SOCKJS_URL)-->
<!--  stompClient = new Client({-->
<!--    debug: () => {},-->
<!--    reconnectDelay: 500,-->
<!--    webSocketFactory: () => sock,-->
<!--  })-->

<!--  stompClient.onConnect = () => {-->
<!--    // -&#45;&#45; Game state updates -&#45;&#45;-->
<!--    stompClient.subscribe(`/topic/game/${gameId.value}`, (msg) => {-->
<!--      try {-->
<!--        const game = JSON.parse(msg.body)-->
<!--        applyGame(game)-->
<!--        gameReady.value = true-->
<!--      } catch (e) {-->
<!--        console.warn("Failed parsing game snapshot:", e)-->
<!--      }-->
<!--    })-->

<!--    // -&#45;&#45; Timer updates -&#45;&#45;-->
<!--    stompClient.subscribe(`/topic/game/${gameId.value}/timer`, (msg) => {-->
<!--      try {-->
<!--        const data = JSON.parse(msg.body)-->
<!--        currentTimerPlayer.value = data.player-->
<!--        timeLeft.value = data.timeLeft-->
<!--      } catch (e) {}-->
<!--    })-->

<!--    // -&#45;&#45; Turn timeout -&#45;&#45;-->
<!--    stompClient.subscribe(`/topic/game/${gameId.value}/turnTimeout`, (msg) => {-->
<!--      try {-->
<!--        const data = JSON.parse(msg.body)-->

<!--        // Always stop local timer-->
<!--        timeLeft.value = 0-->

<!--        // 🔔 Notify all players-->
<!--        if (data.player === username) {-->
<!--          gameMessage.value = "⏰ Your turn expired! You lost this round."-->
<!--        } else {-->
<!--          gameMessage.value = `⚠️ ${data.player}'s turn expired!`-->
<!--        }-->

<!--        // 🧹 Reset the local round state if backend requests it-->
<!--        if (data.reset) {-->
<!--          resetRound()-->
<!--          busted.value = true-->
<!--        }-->

<!--        // Clear message after 5 seconds-->
<!--        setTimeout(() => (gameMessage.value = ""), 5000)-->
<!--      } catch (e) {-->
<!--        console.warn("Failed to process turn timeout:", e)-->
<!--      }-->
<!--    })-->


<!--    // -&#45;&#45; 💬 System messages -&#45;&#45;-->
<!--    stompClient.subscribe(`/topic/game/${gameId.value}/message`, (msg) => {-->
<!--      try {-->
<!--        const data = JSON.parse(msg.body)-->
<!--        gameMessage.value = data.text-->
<!--        setTimeout(() => (gameMessage.value = ""), 5000)-->
<!--      } catch (e) {-->
<!--        console.warn("Invalid system message:", e)-->
<!--      }-->
<!--    })-->


<!--    // Request initial state-->
<!--    stompClient.publish({ destination: "/app/timerSync", body: gameId.value })-->
<!--    stompClient.publish({ destination: "/app/sync", body: gameId.value })-->
<!--  }-->

<!--  stompClient.onStompError = (frame) => {-->
<!--    console.error("Broker error:", frame.headers["message"])-->
<!--    errorMsg.value = "WebSocket broker error."-->
<!--  }-->

<!--  stompClient.onWebSocketError = (e) => {-->
<!--    console.error("WebSocket error:", e)-->
<!--    errorMsg.value = "WebSocket connection failed."-->
<!--  }-->

<!--  stompClient.activate()-->
<!--}-->

<!--// === LIFECYCLE ===-->
<!--onMounted(() => {-->
<!--  if (!gameId.value) {-->
<!--    errorMsg.value = "No active game found — start one from lobby."-->
<!--    return-->
<!--  }-->
<!--  connectStomp()-->
<!--})-->
<!--onUnmounted(() => {-->
<!--  if (stompClient) stompClient.deactivate()-->
<!--})-->

<!--// === 🎲 GAME ACTIONS ===-->
<!--async function rollDice() {-->
<!--  rolling.value = true-->
<!--  try {-->
<!--    const endpoint = hasStartedRoll.value ? "reroll" : "startroll"-->
<!--    const data = await post(`${API_INGAME}/${gameId.value}/${endpoint}/${username}`)-->
<!--    if (!data || data.fullThrow == null) {-->
<!--      gameMessage.value = "💀 You busted! Your turn is over."-->
<!--      busted.value = true-->
<!--      resetRound()-->
<!--      return-->
<!--    }-->
<!--    rolledDice.value = Object.entries(data.fullThrow).flatMap(([face, count]) =>-->
<!--        Array(count).fill(face)-->
<!--    )-->
<!--    disabledFaces.value = data.disabledFaces || []-->
<!--    chosenFaces.value = Array.from(data.chosenFaces || [])-->
<!--    hasStartedRoll.value = true-->
<!--  } catch {-->
<!--    gameMessage.value = "Something went wrong while rolling dice."-->
<!--  } finally {-->
<!--    rolling.value = false-->
<!--  }-->
<!--}-->

<!--async function trySelectDie(face) {-->
<!--  if (disabledFaces.value.includes(face) || chosenFaces.value.includes(face)) return-->
<!--  try {-->
<!--    const data = await post(`${API_INGAME}/${gameId.value}/pickdice/${username}`, face)-->
<!--    if (!data || data.fullThrow == null) {-->
<!--      gameMessage.value = "💀 You busted after this pick! Turn over."-->
<!--      busted.value = true-->
<!--      resetRound()-->
<!--      return-->
<!--    }-->
<!--    const pickedCount = rolledDice.value.filter(f => f === face).length-->
<!--    for (let i = 0; i < pickedCount; i++) collectedDice.value.push(face)-->
<!--    updateRoundPoints()-->
<!--    rolledDice.value = Object.entries(data.fullThrow || {}).flatMap(([f, count]) =>-->
<!--        Array(count).fill(f)-->
<!--    )-->
<!--    disabledFaces.value = data.disabledFaces || []-->
<!--    chosenFaces.value = Array.from(data.chosenFaces || [])-->
<!--  } catch {-->
<!--    gameMessage.value = "Failed to select dice face."-->
<!--  }-->
<!--}-->

<!--// === 🧩 TILE LOGIC ===-->
<!--function canClaim(tile) {-->
<!--  return roundPoints.value >= tile.value-->
<!--}-->
<!--async function tryPickTile(tile) {-->
<!--  if (!canClaim(tile)) return-->
<!--  await pickTile(tile)-->
<!--}-->
<!--async function pickTile(tile) {-->
<!--  try {-->
<!--    await post(`${API_INGAME}/${gameId.value}/claimfrompot/${username}`)-->
<!--    resetRound()-->
<!--    busted.value = false-->
<!--  } catch {-->
<!--    gameMessage.value = "Failed to claim tile."-->
<!--  }-->
<!--}-->

<!--// === HELPERS ===-->
<!--function updateRoundPoints() {-->
<!--  const faceValue = { ONE: 1, TWO: 2, THREE: 3, FOUR: 4, FIVE: 5, SPECIAL: 5 }-->
<!--  const counts = collectedDice.value.reduce((acc, f) => {-->
<!--    acc[f] = (acc[f] || 0) + 1-->
<!--    return acc-->
<!--  }, {})-->
<!--  roundPoints.value = Object.entries(counts)-->
<!--      .reduce((total, [face, count]) => total + (faceValue[face] || 0) * count, 0)-->
<!--}-->
<!--function resetRound() {-->
<!--  rolledDice.value = []-->
<!--  collectedDice.value = []-->
<!--  disabledFaces.value = []-->
<!--  chosenFaces.value = []-->
<!--  hasStartedRoll.value = false-->
<!--  roundPoints.value = 0-->
<!--}-->
<!--function faceEmoji(face) {-->
<!--  const map = { ONE: "1️⃣", TWO: "2️⃣", THREE: "3️⃣", FOUR: "4️⃣", FIVE: "5️⃣", SPECIAL: "🪱" }-->
<!--  return map[face] || face-->
<!--}-->

<!--function goToLobby() {-->
<!--  router.push("/lobbies")-->
<!--}-->

<!--// === 🎮 GET GAME STATE FOR ERROR REPORT ===-->
<!--function getCurrentGameState() {-->
<!--  return {-->
<!--    gameId: gameId.value,-->
<!--    currentPlayer: currentPlayerId.value,-->
<!--    currentTurnIndex: currentTurnIndex.value,-->
<!--    players: players.value,-->
<!--    tilesOnTable: tilesOnTable.value,-->
<!--    rolledDice: rolledDice.value,-->
<!--    collectedDice: collectedDice.value,-->
<!--    roundPoints: roundPoints.value,-->
<!--    myTiles: myTiles.value,-->
<!--    timeLeft: timeLeft.value,-->
<!--    hasStartedRoll: hasStartedRoll.value,-->
<!--    busted: busted.value,-->
<!--  }-->
<!--}-->
<!--</script>-->

<!--<style scoped>-->
<!--.game {-->
<!--  background: #fafafa;-->
<!--  color: #111;-->
<!--  min-height: 100vh;-->
<!--  padding: 2rem;-->
<!--  text-align: center;-->
<!--  font-family: "Inter", sans-serif;-->
<!--}-->
<!--h1, h3, h4, p { color: #111; }-->
<!--.points {-->
<!--  margin: 0.5rem 0 1rem;-->
<!--  font-weight: 700;-->
<!--  color: #2c7a2c;-->
<!--  font-size: 1.2rem;-->
<!--}-->

<!--.timer-box {-->
<!--  margin: 1rem 0;-->
<!--  font-weight: bold;-->
<!--  color: #e63946;-->
<!--  background: #fff3cd;-->
<!--  border-radius: 8px;-->
<!--  padding: .5rem 1rem;-->
<!--  display: inline-block;-->
<!--  transition: all 0.4s ease;-->
<!--}-->

<!--.roll-btn {-->
<!--  background: #4caf50;-->
<!--  color: white;-->
<!--  border: none;-->
<!--  padding: 12px 25px;-->
<!--  font-size: 1.2rem;-->
<!--  border-radius: 10px;-->
<!--  cursor: pointer;-->
<!--  margin-top: 1rem;-->
<!--}-->
<!--.roll-btn:hover { background: #43a047; transform: scale(1.05); }-->
<!--.roll-btn:disabled { background: #aaa; cursor: not-allowed; }-->

<!--.back-button {-->
<!--  position: fixed;-->
<!--  bottom: 20px;-->
<!--  left: 20px;-->
<!--  background: #eee;-->
<!--  border: none;-->
<!--  padding: 10px 16px;-->
<!--  border-radius: 8px;-->
<!--  cursor: pointer;-->
<!--  font-size: 16px;-->
<!--  box-shadow: 0 4px 10px rgba(0,0,0,0.25);-->
<!--}-->
<!--.back-button:hover { background: #ddd; }-->


<!--.dice-area {-->
<!--  display: flex;-->
<!--  justify-content: center;-->
<!--  flex-wrap: wrap;-->
<!--  gap: 1rem;-->
<!--  margin: 1.5rem 0;-->
<!--}-->
<!--.die {-->
<!--  font-size: 2.2rem;-->
<!--  width: 55px;-->
<!--  height: 55px;-->
<!--  border-radius: 12px;-->
<!--  display: flex;-->
<!--  align-items: center;-->
<!--  justify-content: center;-->
<!--  background: #fff;-->
<!--  box-shadow: 0 2px 5px rgba(0,0,0,0.2);-->
<!--  cursor: pointer;-->
<!--  transition: all 0.2s ease;-->
<!--}-->
<!--.die.disabled { opacity: 0.4; cursor: not-allowed; background: #ddd; }-->
<!--.die.chosen { background: #e3f8d3; border: 2px solid #4caf50; }-->

<!--.tiles-table {-->
<!--  display: flex;-->
<!--  justify-content: center;-->
<!--  flex-wrap: wrap;-->
<!--  gap: 0.8rem;-->
<!--  margin-top: 1rem;-->
<!--}-->
<!--.tile {-->
<!--  background: #fff;-->
<!--  border: 2px solid #4caf50;-->
<!--  border-radius: 10px;-->
<!--  padding: 10px 15px;-->
<!--  cursor: pointer;-->
<!--  font-weight: bold;-->
<!--  transition: all 0.2s ease;-->
<!--}-->
<!--.tile:hover { background: #e8f5e9; transform: scale(1.05); }-->
<!--.tile.disabled {-->
<!--  border-color: #aaa;-->
<!--  background: #f3f3f3;-->
<!--  color: #999;-->
<!--  cursor: not-allowed;-->
<!--  transform: none;-->
<!--}-->
<!--.worms { display: block; font-size: 0.8rem; color: #555; }-->

<!--.game-board { display: flex; justify-content: space-between; margin-top: 2rem; gap: 2rem; flex-wrap: wrap; }-->
<!--.my-section, .others-section { flex: 1; min-width: 200px; }-->
<!--.my-tiles-list {-->
<!--  display: flex;-->
<!--  justify-content: center;-->
<!--  gap: 0.5rem;-->
<!--  flex-wrap: wrap;-->
<!--}-->
<!--.my-tile {-->
<!--  background: #fefefe;-->
<!--  border: 2px solid #2196f3;-->
<!--  border-radius: 8px;-->
<!--  padding: 6px 12px;-->
<!--  font-weight: 600;-->
<!--  box-shadow: 0 2px 4px rgba(0,0,0,0.15);-->
<!--}-->
<!--.my-tile small { font-size: 0.8rem; margin-left: 4px; color: #444; }-->
<!--.my-score { font-weight: 700; color: #1565c0; margin-top: 0.5rem; }-->

<!--.other-player {-->
<!--  margin-bottom: 1.5rem; border: 1px solid #ddd; border-radius: 8px; padding: 10px; background: #fff;-->
<!--}-->
<!--.help-button, .error-button{-->
<!--  position: fixed;-->
<!--  bottom: 20px;-->
<!--  right: 20px;-->
<!--  background: #eee;-->
<!--  border: none;-->
<!--  border-radius: 50%;-->
<!--  width: 45px;-->
<!--  height: 45px;-->
<!--  font-size: 22px;-->
<!--  cursor: pointer;-->
<!--  color: #333;-->
<!--  box-shadow: 0 4px 10px rgba(0,0,0,0.25);-->
<!--}-->

<!--.error-button{-->
<!--  right: 70px;-->
<!--}-->
<!--.help-button:hover, .error-button:hover { background: #ddd; transform: scale(1.05); }-->


<!--.err { color: #b00020; margin-top: .5rem; font-weight: 600; }-->
<!--.turn { margin: .25rem 0 1rem; font-weight: 600; color: #333; }-->
<!--</style>-->









<!--<template>-->
<!--  <div class="game">-->
<!--    <h1>🎲 Regenwormen</h1>-->

<!--    <div v-if="!gameReady">-->
<!--      <p>⏳ Joining game...</p>-->
<!--      <p v-if="errorMsg" class="err">{{ errorMsg }}</p>-->
<!--    </div>-->

<!--    <div v-else>-->
<!--      <button class="back-button" @click="goToLobby">⬅️ Terug naar Lobby</button>-->

<!--      <h3>Game ID: {{ gameId }}</h3>-->
<!--      <h4>Jij: {{ username }}</h4>-->

<!--      <div class="system-popup" :class="{ visible: !!gameMessage }">-->
<!--        {{ gameMessage }}-->
<!--      </div>-->

<!--      <div class="timer-box" v-if="showTimer && (timeLeft > 0 || currentTimerPlayer)">-->
<!--        <p v-if="currentTimerPlayer === username">-->
<!--          ⏳ Jouw beurt: <strong>{{ timeLeft }}s</strong> over-->
<!--        </p>-->
<!--        <p v-else>-->
<!--          🧍 {{ currentTimerPlayer }} is aan de beurt ({{ timeLeft }}s)-->
<!--        </p>-->
<!--      </div>-->

<!--      <p v-if="turnInfo" class="turn">Beurt: {{ turnInfo }}</p>-->

<!--      <div class="game-arena">-->
<!--        <DiceRoll-->
<!--            :rolledDice="rolledDice"-->
<!--            :disabledFaces="disabledFaces"-->
<!--            :chosenFaces="chosenFaces"-->
<!--            :isCurrentPlayer="currentPlayerId === username"-->
<!--            :isBusted="isBusted"-->
<!--            :rolling="rolling"-->
<!--            :hasStartedRoll="hasStartedRoll"-->
<!--            :canRoll="timeLeft > 0 || currentPlayerId !== username"-->
<!--            @roll="rollDice"-->
<!--            @selectDie="trySelectDie"-->
<!--        />-->

<!--        <hr class="arena-divider" />-->

<!--        <TilesTable-->
<!--            :tiles="tilesOnTable"-->
<!--            :currentPoints="roundPoints"-->
<!--            @pickTile="tryPickTile"-->
<!--        />-->
<!--      </div>-->

<!--      <DiceCollected-->
<!--          :collectedDice="collectedDice"-->
<!--          :roundPoints="roundPoints"-->
<!--          :hasStartedRoll="hasStartedRoll"-->
<!--      />-->

<!--      <div class="game-board">-->
<!--        <TilesCollected :tiles="myTiles" />-->

<!--        <div class="others-section">-->
<!--          <div-->
<!--              v-for="p in players.filter(function(pl) { return pl.name !== username })"-->
<!--              :key="p.id || p.name"-->
<!--              class="other-player"-->
<!--          >-->
<!--            <h4>{{ p.name || 'Unknown' }}</h4>-->
<!--            <p>Total Tile Points: <strong>{{ playerScore(p) }}</strong></p>-->

<!--            <TilesOtherPlayer-->
<!--                :tiles="p.tiles || []"-->
<!--                :topTile="p.topTile"-->
<!--                @steal="tile => stealTile(p.name, tile)"-->
<!--            />-->
<!--          </div>-->
<!--        </div>-->


<!--        <button class="help-button" @click="showRules = true">❓</button>-->
<!--        <HowToPlayButton :visible="showRules" @close="showRules = false" />-->
<!--      </div>-->
<!--    </div>-->
<!--  </div>-->
<!--</template>-->

<!--<script setup>-->
<!--import { ref, computed, onMounted, onUnmounted } from "vue"-->
<!--import { useRouter } from "vue-router"-->
<!--import SockJS from "sockjs-client"-->
<!--import { Client } from "@stomp/stompjs"-->

<!--import DiceRoll from "./DiceRoll.vue"-->
<!--import DiceCollected from "./DiceCollected.vue"-->
<!--import TilesTable from "./TilesTable.vue"-->
<!--import TilesCollected from "./TilesCollected.vue"-->
<!--import TilesOtherPlayer from "./TilesOtherPlayer.vue"-->
<!--import HowToPlayButton from "./HowToPlayButton.vue"-->

<!--import "./game.css"-->

<!--const router = useRouter()-->
<!--const showTimer = ref(false)-->

<!--// API & WebSocket-->
<!--const API_INGAME = "http://localhost:8080/ingame"-->
<!--const SOCKJS_URL = "http://localhost:8080/ws"-->
<!--let stompClient = null-->

<!--// User / game state-->
<!--const user = JSON.parse(localStorage.getItem("user"))-->
<!--const username = user?.username || user?.name || "Guest"-->
<!--const gameId = ref(localStorage.getItem("gameId") || null)-->
<!--const gameReady = ref(!!gameId.value)-->
<!--const errorMsg = ref("")-->
<!--const showRules = ref(false)-->
<!--const rolling = ref(false)-->

<!--// Game data-->
<!--const rolledDice = ref([])-->
<!--const disabledFaces = ref([])-->
<!--const chosenFaces = ref([])-->
<!--const collectedDice = ref([])-->
<!--const tilesOnTable = ref([])-->
<!--const players = ref([])-->
<!--const currentPlayerId = ref(null)-->
<!--const currentTurnIndex = ref(null)-->
<!--const hasStartedRoll = ref(false)-->
<!--const busted = ref(false)-->
<!--const roundPoints = ref(0)-->
<!--const myTiles = ref([])-->

<!--// Timer & messages-->
<!--const timeLeft = ref(0)-->
<!--const currentTimerPlayer = ref("")-->
<!--const gameMessage = ref("")-->

<!--// Computed-->
<!--const turnInfo = computed(() => {-->
<!--  if (!players.value?.length || currentTurnIndex.value == null) return ""-->
<!--  const p = players.value[currentTurnIndex.value]-->
<!--  return p ? `${p.name ?? p.id}` : ""-->
<!--})-->
<!--const isBusted = computed(() => busted.value)-->
<!--function playerScore(player) {-->
<!--  if (!player || !player.tiles || !Array.isArray(player.tiles)) return 0-->
<!--  return player.tiles.reduce((sum, t) => sum + (t.points || 0), 0)-->
<!--}-->

<!--// Fetch helper-->
<!--async function post(url, body = null) {-->
<!--  const res = await fetch(url, {-->
<!--    method: "POST",-->
<!--    headers: { "Content-Type": "application/json" },-->
<!--    body: body ? JSON.stringify(body) : null,-->
<!--  })-->
<!--  if (!res.ok) {-->
<!--    const t = await res.text().catch(() => "")-->
<!--    throw new Error(`Backend error ${res.status}: ${t}`)-->
<!--  }-->
<!--  const type = res.headers.get("content-type") || ""-->
<!--  return type.includes("application/json") ? res.json() : null-->
<!--}-->

<!--// Apply game snapshot-->
<!--function applyGame(game) {-->
<!--  if (!game) return-->
<!--  const previousPlayer = currentPlayerId.value-->

<!--  players.value = game.players || []-->
<!--  tilesOnTable.value = game.tilesPot?.tiles || []-->
<!--  currentTurnIndex.value = game.turnIndex ?? null-->
<!--  currentPlayerId.value = players.value?.[game.turnIndex]?.name || null-->

<!--  if (currentPlayerId.value !== previousPlayer && currentPlayerId.value === username) {-->
<!--    resetRound()-->
<!--    busted.value = false-->
<!--    gameMessage.value = "🎯 It's your turn!"-->
<!--  }-->

<!--  const me = players.value.find(p => p.name === username || p.id === username)-->
<!--  myTiles.value = Array.isArray(me?.tiles) ? me.tiles : myTiles.value-->
<!--}-->

<!--// WebSocket setup-->
<!--function connectStomp() {-->
<!--  const sock = new SockJS(SOCKJS_URL)-->
<!--  stompClient = new Client({-->
<!--    debug: () => {},-->
<!--    reconnectDelay: 500,-->
<!--    webSocketFactory: () => sock,-->
<!--  })-->

<!--  stompClient.onConnect = () => {-->
<!--    stompClient.subscribe(`/topic/game/${gameId.value}`, msg => {-->
<!--      try { applyGame(JSON.parse(msg.body)); gameReady.value = true }-->
<!--      catch (e) { console.warn("Failed parsing game snapshot:", e) }-->
<!--    })-->

<!--    stompClient.subscribe(`/topic/game/${gameId.value}/timer`, msg => {-->
<!--      try {-->
<!--        const data = JSON.parse(msg.body)-->
<!--        currentTimerPlayer.value = data.player-->

<!--        const t = timeLeft-->
<!--        t.value = data.timeLeft-->

<!--        if (!showTimer.value && t.value === 10) {-->
<!--          showTimer.value = true-->
<!--        }-->
<!--        if (t.value <= 0) {-->
<!--          showTimer.value = false-->
<!--        }-->
<!--      } catch {}-->
<!--    })-->




<!--    stompClient.subscribe(`/topic/game/${gameId.value}/turnTimeout`, msg => {-->
<!--      try {-->
<!--        const data = JSON.parse(msg.body)-->
<!--        timeLeft.value = 0-->
<!--        gameMessage.value = data.player === username-->
<!--            ? "⏰ Your turn expired! You lost this round."-->
<!--            : `⚠️ ${data.player}'s turn expired!`-->
<!--        if (data.reset) { resetRound(); busted.value = true }-->
<!--        setTimeout(() => (gameMessage.value = ""), 5000)-->
<!--      } catch {}-->
<!--    })-->

<!--    stompClient.subscribe(`/topic/game/${gameId.value}/message`, msg => {-->
<!--      try {-->
<!--        const data = JSON.parse(msg.body)-->
<!--        gameMessage.value = data.text-->
<!--        setTimeout(() => (gameMessage.value = ""), 5000)-->
<!--      } catch {}-->
<!--    })-->

<!--    stompClient.publish({ destination: "/app/timerSync", body: gameId.value })-->
<!--    stompClient.publish({ destination: "/app/sync", body: gameId.value })-->
<!--  }-->

<!--  stompClient.onStompError = frame => { console.error("Broker error:", frame.headers["message"]); errorMsg.value = "WebSocket broker error." }-->
<!--  stompClient.onWebSocketError = e => { console.error("WebSocket error:", e); errorMsg.value = "WebSocket connection failed." }-->
<!--  stompClient.activate()-->
<!--}-->

<!--// Lifecycle-->
<!--onMounted(() => {-->
<!--  if (!gameId.value) { errorMsg.value = "No active game found — start one from lobby."; return }-->
<!--  connectStomp()-->
<!--})-->
<!--onUnmounted(() => { if (stompClient) stompClient.deactivate() })-->

<!--// Game actions-->
<!--async function rollDice() {-->
<!--  rolling.value = true-->
<!--  try {-->
<!--    const endpoint = hasStartedRoll.value ? "reroll" : "startroll"-->
<!--    const data = await post(`${API_INGAME}/${gameId.value}/${endpoint}/${username}`)-->
<!--    if (!data || data.fullThrow == null) { gameMessage.value = "💀 You busted!"; busted.value = true; resetRound(); return }-->
<!--    rolledDice.value = Object.entries(data.fullThrow).flatMap(([face, count]) => Array(count).fill(face))-->
<!--    disabledFaces.value = data.disabledFaces || []-->
<!--    chosenFaces.value = Array.from(data.chosenFaces || [])-->
<!--    hasStartedRoll.value = true-->
<!--  } catch { gameMessage.value = "Something went wrong while rolling dice." }-->
<!--  finally { rolling.value = false }-->
<!--}-->

<!--async function trySelectDie(face) {-->
<!--  if (disabledFaces.value.includes(face) || chosenFaces.value.includes(face)) return-->
<!--  try {-->
<!--    const data = await post(`${API_INGAME}/${gameId.value}/pickdice/${username}`, face)-->
<!--    if (!data || data.fullThrow == null) { gameMessage.value = "💀 You busted after this pick!"; busted.value = true; resetRound(); return }-->
<!--    const pickedCount = rolledDice.value.filter(f => f === face).length-->
<!--    for (let i = 0; i < pickedCount; i++) collectedDice.value.push(face)-->
<!--    updateRoundPoints()-->
<!--    rolledDice.value = Object.entries(data.fullThrow || {}).flatMap(([f, count]) => Array(count).fill(f))-->
<!--    disabledFaces.value = data.disabledFaces || []-->
<!--    chosenFaces.value = Array.from(data.chosenFaces || [])-->
<!--  } catch { gameMessage.value = "Failed to select dice face." }-->
<!--}-->

<!--// Tile actions-->
<!--async function tryPickTile(tile) {-->
<!--  if (roundPoints.value < tile.value) return-->
<!--  await pickTile(tile)-->
<!--}-->
<!--async function pickTile(tile) {-->
<!--  try {-->
<!--    await post(`${API_INGAME}/${gameId.value}/claimfrompot/${username}`)-->
<!--    // Optioneel: direct UI bijwerken-->
<!--    tilesOnTable.value = tilesOnTable.value.filter(t => t.value !== tile.value)-->
<!--    resetRound()-->
<!--    busted.value = false-->
<!--  } catch {-->
<!--    gameMessage.value = "Failed to claim tile."-->
<!--  }-->
<!--}-->

<!--async function stealTile(playerName, tile) {-->
<!--  try {-->
<!--    await post(`${API_INGAME}/${gameId.value}/claimfromplayer/${username}`, { target: playerName })-->
<!--    const player = players.value.find(p => p.name === playerName)-->
<!--    if (player) player.tiles = player.tiles.filter(t => t.value !== tile.value)-->
<!--    myTiles.value.push(tile)-->
<!--  } catch { gameMessage.value = "Failed to steal tile." }-->
<!--}-->

<!--// Helpers-->
<!--function updateRoundPoints() {-->
<!--  const faceValue = { ONE: 1, TWO: 2, THREE: 3, FOUR: 4, FIVE: 5, SPECIAL: 5 }-->
<!--  const counts = collectedDice.value.reduce((acc, f) => { acc[f] = (acc[f] || 0) + 1; return acc }, {})-->
<!--  roundPoints.value = Object.entries(counts).reduce((total, [face, count]) => total + (faceValue[face] || 0) * count, 0)-->
<!--}-->

<!--function resetRound() {-->
<!--  rolledDice.value = []-->
<!--  collectedDice.value = []-->
<!--  disabledFaces.value = []-->
<!--  chosenFaces.value = []-->
<!--  hasStartedRoll.value = false-->
<!--  roundPoints.value = 0-->
<!--}-->

<!--function goToLobby() { router.push("/lobbies") }-->
<!--</script>-->

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

      <div class="timer-box" v-if="showTimer && (timeLeft > 0 || currentTimerPlayer)">
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

        <hr class="arena-divider" />

        <TilesTable
            :tiles="tilesOnTable"
            :currentPoints="roundPoints"
            @pickTile="tryPickTile"
        />
      </div>

      <DiceCollected
          :collectedDice="collectedDice"
          :roundPoints="roundPoints"
          :hasStartedRoll="hasStartedRoll"
      />

      <div class="game-board">
        <TilesCollected :tiles="myTiles" />

        <div class="others-section">
          <div
              v-for="p in players.filter(function(pl) { return pl.name !== username })"
              :key="p.id || p.name"
              class="other-player"
          >
            <h4>{{ p.name || 'Unknown' }}</h4>
            <p>Total Tile Points: <strong>{{ playerScore(p) }}</strong></p>

            <TilesOtherPlayer
                :tiles="p.tiles || []"
                :topTile="p.topTile"
                @steal="tile => stealTile(p.name, tile)"
            />
          </div>
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
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from "vue"
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

import "./game.css"

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

// Apply game snapshot
function applyGame(game) {
  if (!game) return
  const previousPlayer = currentPlayerId.value

  players.value = game.players || []
  tilesOnTable.value = game.tilesPot?.tiles || []
  currentTurnIndex.value = game.turnIndex ?? null
  currentPlayerId.value = players.value?.[game.turnIndex]?.name || null

  if (currentPlayerId.value !== previousPlayer && currentPlayerId.value === username) {
    resetRound()
    busted.value = false
    gameMessage.value = "🎯 It's your turn!"
  }

  const me = players.value.find(p => p.name === username || p.id === username)
  myTiles.value = Array.isArray(me?.tiles) ? me.tiles : myTiles.value
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
async function pickTile(tile) {
  try {
    await post(`${API_INGAME}/${gameId.value}/claimfrompot/${username}`)
    tilesOnTable.value = tilesOnTable.value.filter(t => t.value !== tile.value)
    resetRound()
    busted.value = false
  } catch {
    gameMessage.value = "Failed to claim tile."
  }
}

async function stealTile(playerName, tile) {
  try {
    await post(`${API_INGAME}/${gameId.value}/claimfromplayer/${username}`, { target: playerName })
    const player = players.value.find(p => p.name === playerName)
    if (player) player.tiles = player.tiles.filter(t => t.value !== tile.value)
    myTiles.value.push(tile)
  } catch { gameMessage.value = "Failed to steal tile." }
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

function goToLobby() { router.push("/lobbies") }

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
</script>

<style scoped>
/* alle bestaande styling van jouw versie behouden, met extra .error-button */
.game {
  background: #fafafa;
  color: #111;
  min-height: 100vh;
  padding: 2rem;
  text-align: center;
  font-family: "Inter", sans-serif;
}
h1, h3, h4, p { color: #111; }
.points {
  margin: 0.5rem 0 1rem;
  font-weight: 700;
  color: #2c7a2c;
  font-size: 1.2rem;
}
.timer-box {
  margin: 1rem 0;
  font-weight: bold;
  color: #e63946;
  background: #fff3cd;
  border-radius: 8px;
  padding: .5rem 1rem;
  display: inline-block;
  transition: all 0.4s ease;
}
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
.back-button {
  position: fixed;
  bottom: 20px;
  left: 20px;
  background: #eee;
  border: none;
  padding: 10px 16px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 16px;
  box-shadow: 0 4px 10px rgba(0,0,0,0.25);
}
.back-button:hover { background: #ddd; }
.dice-area { display: flex; justify-content: center; flex-wrap: wrap; gap: 1rem; margin: 1.5rem 0; }
.die {
  font-size: 2.2rem;
  width: 55px;
  height: 55px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  box-shadow: 0 2px 5px rgba(0,0,0,0.2);
  cursor: pointer;
  transition: all 0.2s ease;
}
.die.disabled { opacity: 0.4; cursor: not-allowed; background: #ddd; }
.die.chosen { background: #e3f8d3; border: 2px solid #4caf50; }
.tiles-table {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 0.8rem;
  margin-top: 1rem;
}
.tile {
  background: #fff;
  border: 2px solid #4caf50;
  border-radius: 10px;
  padding: 10px 15px;
  cursor: pointer;
  font-weight: bold;
  transition: all 0.2s ease;
}
.tile:hover { background: #e8f5e9; transform: scale(1.05); }
.tile.disabled { border-color: #aaa; background: #f3f3f3; color: #999; cursor: not-allowed; transform: none; }
.worms { display: block; font-size: 0.8rem; color: #555; }
.game-board { display: flex; justify-content: space-between; margin-top: 2rem; gap: 2rem; flex-wrap: wrap; }
.my-section, .others-section { flex: 1; min-width: 200px; }
.my-tiles-list { display: flex; justify-content: center; gap: 0.5rem; flex-wrap: wrap; }
.my-tile {
  background: #fefefe;
  border: 2px solid #2196f3;
  border-radius: 8px;
  padding: 6px 12px;
  font-weight: 600;
  box-shadow: 0 2px 4px rgba(0,0,0,0.15);
}
.my-tile small { font-size: 0.8rem; margin-left: 4px; color: #444; }
.my-score { font-weight: 700; color: #1565c0; margin-top: 0.5rem; }
.other-player { margin-bottom: 1.5rem; border: 1px solid #ddd; border-radius: 8px; padding: 10px; background: #fff; }
.help-button, .error-button {
   position: fixed;
   bottom: 20px;
   background: #eee;
   border: none;
   border-radius: 50%;
   width: 45px;
   height: 45px;
   font-size: 22px;
   cursor: pointer;
   color: #111;
 }

</style>
