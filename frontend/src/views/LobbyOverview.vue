<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const lobbies = ref([])

async function loadLobbies() {
  try {
    const res = await fetch('http://localhost:8080/api/lobbies')
    if (!res.ok) throw new Error(`Failed to load lobbies: ${res.status}`)
    lobbies.value = await res.json()
  } catch (err) {
    console.error('Error loading lobbies:', err)
  }
}

async function joinLobby(id) {
  console.log("Joining lobby with id:", id)

  const user = JSON.parse(localStorage.getItem('user'))
  if (!user) {
    alert('Please log in first!')
    return
  }

  try {
    const res = await fetch(`http://localhost:8080/api/lobbies/join/${id}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username: user.username, ready: false })
    })

    if (!res.ok) {
      console.error('Failed to join lobby:', res.status)
      alert('Unable to join lobby.')
      return
    }

    const joinedLobby = await res.json()
    console.log('Joined lobby:', joinedLobby)

    router.push(`/lobby/${joinedLobby?.id ?? id}`)
  } catch (err) {
    console.error('Error joining lobby:', err)
    alert('An error occurred while joining the lobby.')
  }
}

onMounted(loadLobbies)
</script>

<template>
  <section class="lobby-overview">

    <div class="top-bar">

      <h1>Available Lobbies</h1>
    </div>

    <div class="lobby-grid">
      <div v-for="lobby in lobbies" :key="lobby.id" class="lobby-box">
        <h2>{{ lobby.name }}</h2>
        <p>{{ lobby.players.length }}/{{ lobby.maxPlayers }}</p>
        <button
            @click="joinLobby(lobby.id)"
            :disabled="lobby.players.length >= lobby.maxPlayers"
        >
          Join
        </button>
      </div>
    </div>
    <button class="back-btn" @click="$router.push('/')">Leave</button>
  </section>
</template>

<style scoped>
.lobby-overview {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  text-align: center;
  background-color: #faf9fc;
}

.lobby-overview h1 {
  font-size: 2rem;
  margin-bottom: 2rem;
  color: #b10c96;
  text-shadow: 1px 1px 3px rgba(0, 0, 0, 0.1);
}

.lobby-grid {
  display: grid;
  grid-template-columns: repeat(2, 250px);
  grid-template-rows: repeat(2, 200px);
  gap: 2rem;
  justify-content: center;
  align-items: center;
}

.lobby-box {
  border: 3px solid #b10c96;
  border-radius: 16px;
  padding: 1rem;
  text-align: center;
  background: white;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transition: transform 0.2s, box-shadow 0.2s;
}

.lobby-box:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.2);
}

.lobby-box h2 {
  font-size: 1.4rem;
  color: #333;
  margin-bottom: 0.5rem;
}

.lobby-box p {
  font-size: 1rem;
  color: #555;
}

button {
  margin-top: 0.75rem;
  background: #b10c96;
  color: white;
  border: none;
  border-radius: 8px;
  padding: 0.5rem 1.5rem;
  cursor: pointer;
  transition: background 0.2s;
}

button:hover:not(:disabled) {
  background: #87067c;
}

button:disabled {
  background: grey;
  cursor: not-allowed;
}
</style>
