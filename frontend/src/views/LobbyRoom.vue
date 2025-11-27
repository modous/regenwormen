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
let stompClient = null

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
  if (stompClient) stompClient.deactivate()
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

  connectLobbySocket()
})

onBeforeUnmount(() => {
  if (stompClient) stompClient.deactivate()
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
