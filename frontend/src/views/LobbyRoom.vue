<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import SockJS from 'sockjs-client'
import { Client } from '@stomp/stompjs'

const route = useRoute()
const router = useRouter()
const lobby = ref(null)
const gameId = ref(null)
const user = JSON.parse(localStorage.getItem('user'))
const username = user?.username || user?.name || 'Guest'
let stompClient = null
let heartbeatInterval = null
const HEARTBEAT_INTERVAL = 5000

// === API HELPERS (for actions only, not for sync) ===
async function post(url, body) {
  const res = await fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  })
  if (!res.ok) throw new Error(`HTTP ${res.status}`)
  return res.json().catch(() => ({}))
}

// === API CALLS (single loads if needed) ===
async function loadLobbyOnce() {
  const res = await fetch(`http://localhost:8080/api/lobbies/${route.params.id}`)
  if (!res.ok) throw new Error('Failed to load lobby')
  lobby.value = await res.json()
  if (lobby.value.gameId) gameId.value = lobby.value.gameId
}

// === PLAYER ACTIONS ===
async function toggleReady() {
  await post(`http://localhost:8080/api/lobbies/${route.params.id}/ready`, {
    username: user.username
  })
}

async function leaveLobby() {
  await post(`http://localhost:8080/api/lobbies/${route.params.id}/leave`, {
    username: user.username
  })
  // Also notify pregame service of disconnect
  if (gameId.value) {
    const url = `http://localhost:8080/pregame/${gameId.value}/disconnect/${username}`
    navigator.sendBeacon(url)
  }
  // ✅ Player deliberately left the lobby - disconnect socket
  if (stompClient) {
    stompClient.deactivate()
    console.log("✅ Socket deactivated - player left lobby intentionally")
  }
  if (heartbeatInterval) clearInterval(heartbeatInterval)
  router.push('/lobbies')
}

async function startGame() {
  try {
    const res = await fetch(`http://localhost:8080/api/lobbies/${route.params.id}/start`, {
      method: 'POST'
    })
    if (!res.ok) {
      const err = await res.json().catch(() => ({}))
      alert('Cannot start game: ' + (err.error || err.message || 'Unknown error'))
      return
    }
    const updatedLobby = await res.json()
    localStorage.setItem('gameId', updatedLobby.gameId)
    router.push('/game')
  } catch (e) {
    alert('Could not start game — check backend connection.')
  }
}

// === HELPER ===
function allPlayersReady() {
  return lobby.value?.players.length >= 2 && lobby.value.players.every(p => p.ready)
}

// === COPY LINK ===
async function copyInviteLink() {
  const inviteLink = `${window.location.origin}/lobby/${route.params.id}`
  await navigator.clipboard.writeText(inviteLink)
  alert(`✅ Invite link copied!\n${inviteLink}`)
}

// === DISCONNECT/RECONNECT HANDLERS ===
function setupDisconnectHandlers() {
  // When user closes tab/refreshes
  window.addEventListener('beforeunload', (event) => {
    console.log("⚠️ beforeunload triggered - sending disconnect notification from lobby")
    if (gameId.value) {
      navigator.sendBeacon(`http://localhost:8080/pregame/${gameId.value}/disconnect/${username}`)
    }
  })
}

function sendReconnectEvent() {
  if (gameId.value) {
    const url = `http://localhost:8080/pregame/${gameId.value}/reconnect/${username}`
    console.log("✅ Sending reconnect to lobby:", url)
    fetch(url, {
      method: "POST",
      headers: { "Content-Type": "application/json" }
    }).then(res => {
      console.log("✅ Lobby reconnect response:", res.status)
    }).catch(err => console.error("❌ Lobby reconnect notification failed:", err))
  }
}

// === STOMP SOCKET ===
function connectLobbySocket() {
  const sock = new SockJS('http://localhost:8080/ws')

  stompClient = new Client({
    webSocketFactory: () => sock,
    reconnectDelay: 500,
    debug: () => {}
  })

  stompClient.onConnect = () => {
    console.log('✅ Connected to lobby socket')

    // subscribe for real-time lobby updates
    stompClient.subscribe(`/topic/lobby/${route.params.id}`, (msg) => {
      const updated = JSON.parse(msg.body)
      lobby.value = updated

      // 🚫 CHECK: Is the current user still in the lobby?
      const userStillInLobby = updated.players.some(p => p.username === user.username)
      if (!userStillInLobby && lobby.value && lobby.value.players.length > 0) {
        // User was removed from lobby (probably joined another lobby)
        console.warn("⚠️ You were removed from this lobby because you joined another one")
        alert("⚠️ You were automatically removed from this lobby because you joined a different one.")
        if (stompClient) stompClient.deactivate()
        if (heartbeatInterval) clearInterval(heartbeatInterval)
        router.push('/lobbies')
        return
      }

      if (updated.gameStarted && updated.gameId) {
        console.log('🎮 Game started — redirecting...')
        localStorage.setItem('gameId', updated.gameId)
        router.push('/game')
      }
    })

    // request initial state
    stompClient.publish({
      destination: '/app/lobbySync',
      body: route.params.id
    })

    // Send reconnect notification
    sendReconnectEvent()

    // ❤️‍🩹 Heartbeat
    heartbeatInterval = setInterval(() => {
      if (stompClient && stompClient.connected) {
        const playerId = user?.id || username
        stompClient.publish({
          destination: "/app/heartbeat",
          body: JSON.stringify({ gameId: gameId.value, playerId: playerId })
        })
      }
    }, HEARTBEAT_INTERVAL)
  }

  stompClient.onDisconnect = () => {
    console.log("⚠️ STOMP Disconnecting from lobby")
    if (gameId.value && stompClient && stompClient.connected) {
      stompClient.publish({
        destination: '/app/disconnect',
        body: JSON.stringify({ gameId: gameId.value, playerId: username })
      })
    }
  }

  stompClient.activate()
}

onMounted(async () => {
  await loadLobbyOnce()

  // Auto-join if not already in lobby
  const alreadyIn = lobby.value.players.some(p => p.username === user.username)
  if (!alreadyIn && !lobby.value.isFull) {
    await post(`http://localhost:8080/api/lobbies/join/${route.params.id}`, {
      username: user.username,
      ready: false
    })
  }

  // Extract gameId if available
  if (lobby.value.gameId) {
    gameId.value = lobby.value.gameId
  }

  connectLobbySocket()
  setupDisconnectHandlers()
})

onBeforeUnmount(() => {
  if (stompClient) stompClient.deactivate()
  if (heartbeatInterval) clearInterval(heartbeatInterval)
})
</script>

<template>
  <section class="lobby-room" v-if="lobby">
    <h1>{{ lobby.name }}</h1>

    <ul>
      <li
          v-for="p in lobby.players"
          :key="p.username"
          :style="{ color: p.ready ? 'green' : 'red' }"
      >
        {{ p.username }} - {{ p.ready ? 'Ready' : 'Not Ready' }}
      </li>
    </ul>

    <div class="buttons">
      <button
          @click="toggleReady"
          :style="{ background: lobby.players.find(p => p.username === user.username)?.ready ? 'red' : 'green' }"
      >
        {{
          lobby.players.find(p => p.username === user.username)?.ready
              ? 'Unready'
              : 'Click to Ready'
        }}
      </button>

      <button class="leave-btn" @click="leaveLobby">Leave Lobby</button>
    </div>

    <button class="invite-link-btn" @click="copyInviteLink">Invite via Link</button>

    <div v-if="allPlayersReady()" class="start-game-container">
      <button class="start-btn" @click="startGame">🚀 Start Game</button>
    </div>
  </section>
</template>

<style scoped>
.lobby-room { text-align: center; color: black; }
ul { list-style: none; padding: 0; }
li { margin: 0.5rem 0; font-weight: bold; }
.buttons { display: flex; justify-content: center; gap: 10px; margin-top: 1rem; }
button { border: none; border-radius: 8px; padding: 0.5rem 1rem; color: white; font-size: 1rem; cursor: pointer; transition: 0.3s; }
.leave-btn { background-color: #000; }
.leave-btn:hover { background-color: #7a0606; transform: translateY(-2px); }
.invite-link-btn { background-color: #0077cc; margin-top: 20px; margin-left: 10px; }
.invite-link-btn:hover { background-color: #005fa3; transform: translateY(-2px); }
.start-game-container { margin-top: 2rem; }
.start-btn { background-color: #28a745; font-size: 1.2rem; padding: 0.8rem 1.5rem; }
.start-btn:hover { background-color: #218838; transform: translateY(-2px); }
</style>
