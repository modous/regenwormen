<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const lobby = ref(null)
const gameId = ref(null)
let poller = null

const user = JSON.parse(localStorage.getItem('user'))

// === API CALLS ===
async function loadLobby() {
  const res = await fetch(`http://localhost:8080/api/lobbies/${route.params.id}`)
  if (!res.ok) throw new Error('Failed to load lobby')
  lobby.value = await res.json()

  // ✅ Always capture real backend gameId if available
  if (lobby.value.gameId) {
    gameId.value = lobby.value.gameId
  }
}

async function toggleReady() {
  await fetch(`http://localhost:8080/api/lobbies/${route.params.id}/ready`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username: user.username })
  })
  await loadLobby()
}

async function leaveLobby() {
  await fetch(`http://localhost:8080/api/lobbies/${route.params.id}/leave`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username: user.username })
  })
  clearInterval(poller)
  router.push('/lobbies')
}

// === START GAME LOGIC ===
async function startGame() {
  try {
    console.log('🚀 Manually starting game for lobby:', route.params.id)
    const res = await fetch(`http://localhost:8080/api/lobbies/${route.params.id}/start`, {
      method: 'POST'
    })

    if (!res.ok) {
      const err = await res.json().catch(() => ({}))
      alert('Cannot start game: ' + (err.error || err.message || 'Unknown error'))
      return
    }

    const updatedLobby = await res.json()
    console.log('✅ Game started successfully:', updatedLobby)

    // ✅ Save backend gameId so GameMain.vue knows what to load
    localStorage.setItem('gameId', updatedLobby.gameId)
    router.push('/game')
  } catch (err) {
    console.error('Failed to start game:', err)
    alert('Could not start game — check backend connection.')
  }
}


// === HELPER ===
function allPlayersReady() {
  return lobby.value?.players.length >= 2 && lobby.value.players.every(p => p.ready)
}

// === LIFECYCLE ===
onMounted(async () => {
  await loadLobby()

  // Auto-join if not already in lobby
  const alreadyIn = lobby.value.players.some(p => p.username === user.username)
  if (!alreadyIn && !lobby.value.isFull) {
    await fetch(`http://localhost:8080/api/lobbies/join/${route.params.id}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username: user.username, ready: false })
    })
    await loadLobby()
  }

  // Poll every 2 s for lobby updates
  poller = setInterval(async () => {
    try {
      await loadLobby()

      // ✅ If backend reports game started, redirect all players
      if (lobby.value?.gameStarted && lobby.value?.gameId && !window.location.pathname.includes('/game')) {
        console.log('🎮 Game started — redirecting player to game screen...')
        localStorage.setItem('gameId', lobby.value.gameId)
        router.push('/game')
      }
    } catch (err) {
      console.warn('Polling failed:', err)
    }
  }, 2000)
})

onBeforeUnmount(() => clearInterval(poller))

async function copyInviteLink() {
  const inviteLink = `${window.location.origin}/lobby/${route.params.id}`
  await navigator.clipboard.writeText(inviteLink)
  alert(`✅ Invite link copied!\n${inviteLink}`)
}
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

    <!-- ✅ Start Game Button appears when all ready -->
    <div v-if="allPlayersReady()" class="start-game-container">
      <button class="start-btn" @click="startGame">🚀 Start Game</button>
    </div>
  </section>
</template>

<style scoped>
.lobby-room {
  text-align: center;
  color: black;
}
ul {
  list-style: none;
  padding: 0;
}
li {
  margin: 0.5rem 0;
  font-weight: bold;
}
.buttons {
  display: flex;
  justify-content: center;
  gap: 10px;
  margin-top: 1rem;
}
button {
  border: none;
  border-radius: 8px;
  padding: 0.5rem 1rem;
  color: white;
  font-size: 1rem;
  cursor: pointer;
  transition: 0.3s;
}
.leave-btn {
  background-color: #000;
}
.leave-btn:hover {
  background-color: #7a0606;
  transform: translateY(-2px);
}
.invite-link-btn {
  background-color: #0077cc;
  margin-top: 20px;
  margin-left: 10px;
}
.invite-link-btn:hover {
  background-color: #005fa3;
  transform: translateY(-2px);
}
.start-game-container {
  margin-top: 2rem;
}
.start-btn {
  background-color: #28a745;
  font-size: 1.2rem;
  padding: 0.8rem 1.5rem;
}
.start-btn:hover {
  background-color: #218838;
  transform: translateY(-2px);
}
</style>
