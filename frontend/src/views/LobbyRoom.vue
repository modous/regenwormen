<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const lobby = ref(null)
const countdown = ref(null)
let timer = null
let poller = null

const user = JSON.parse(localStorage.getItem('user'))

async function loadLobby() {
  const res = await fetch(`http://localhost:8080/api/lobbies/${route.params.id}`)
  lobby.value = await res.json()
}

async function toggleReady() {
  await fetch(`http://localhost:8080/api/lobbies/${route.params.id}/ready`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username: user.email })
  })
  await loadLobby()

  // stop countdown if someone unreadies
  if (!allPlayersReady()) stopCountdown()
}

async function leaveLobby() {
  await fetch(`http://localhost:8080/api/lobbies/${route.params.id}/leave`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username: user.email })
  })

  clearInterval(poller)
  stopCountdown()
  router.push('/lobbies')
}

function startCountdown() {
  if (countdown.value) return
  countdown.value = 10
  timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
      timer = null
      router.push('/game')
    }
  }, 1000)
}

function stopCountdown() {
  if (timer) clearInterval(timer)
  timer = null
  countdown.value = null
}

function allPlayersReady() {
  return lobby.value?.players.length > 0 && lobby.value.players.every(p => p.ready)
}

onMounted(async () => {
  await loadLobby()

  const alreadyIn = lobby.value.players.some(p => p.username === user.email)
  if (!alreadyIn && !lobby.value.isFull) {
    await fetch(`http://localhost:8080/api/lobbies/join/${route.params.id}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username: user.username, ready: false })
    })
    await loadLobby()
  }

  poller = setInterval(async () => {
    await loadLobby()
    if (allPlayersReady() && !countdown.value) {
      startCountdown()
    }
  }, 2000)
})

onBeforeUnmount(() => {
  clearInterval(poller)
  stopCountdown()
})

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
      <!-- Ready / Unready -->
      <button
          @click="toggleReady"
          :style="{ background: lobby.players.find(p => p.username === user.email)?.ready ? 'red' : 'green' }"
      >
        {{
          lobby.players.find(p => p.username === user.email)?.ready
              ? 'Unready'
              : 'Click to Ready'
        }}
      </button>

      <!-- Leave Lobby -->
      <button class="leave-btn" @click="leaveLobby">Leave Lobby</button>
    </div>
    <button class="invite-link-btn" @click="copyInviteLink">Invite via Link</button>

    <div v-if="countdown" class="countdown">Game starting in {{ countdown }}</div>
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
  background-color: #000000;
}
.leave-btn:hover {
  background-color: #7a0606;
  transform: translateY(-2px);
}
.countdown {
  margin-top: 1rem;
  font-size: 1.3rem;
  font-weight: bold;
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
</style>
