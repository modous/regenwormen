<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const lobby = ref(null)
const countdown = ref(null)
let timer = null

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

  // If not everyone ready anymore, stop countdown
  if (!allPlayersReady()) stopCountdown()
}

function startCountdown() {
  if (countdown.value) return // prevent multiple timers

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

  setInterval(async () => {
    await loadLobby()
    if (allPlayersReady() && !countdown.value) {
      startCountdown()
    }
  }, 2000)
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

    <!-- ✅ Button now always visible -->
    <button
        @click="toggleReady"
        :style="{ background: lobby.players.find(p => p.username === user.email)?.ready ? 'green' : 'red' }"
    >
      {{
        lobby.players.find(p => p.username === user.email)?.ready
            ? 'Unready'
            : 'Click to Ready'
      }}
    </button>

    <!-- ✅ Start button visible when all ready -->
    <button
        v-if="allPlayersReady()"
        @click="startCountdown"
        class="start-btn"
    >
      Start Game
    </button>

    <div v-if="countdown">Game starting in {{ countdown }}</div>
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
button {
  margin-top: 1rem;
  border: none;
  border-radius: 8px;
  padding: 0.5rem 1rem;
  color: white;
  font-size: 1rem;
  cursor: pointer;
}
.start-btn {
  background-color: #008000;
  margin-left: 10px;
  transition: 0.3s;
}
.start-btn:hover {
  background-color: #005f00;
  transform: translateY(-2px);
}
</style>
