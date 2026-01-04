<template>
  <div class="mh-page">
    <div class="mh-container"> <button class="mh-back" @click="goHome">
      ← Back
    </button>
      <!-- HEADER -->
      <header class="mh-header">
        <div class="mh-header-left">


          <div>
            <h1 class="mh-title">Match history</h1>
            <p class="mh-subtitle">
              All games you played, with result.
            </p>
          </div>
        </div>

        <div class="mh-controls">
          <select v-model="resultFilter" class="mh-select">
            <option value="all">All</option>
            <option value="win">Wins</option>
            <option value="loss">Losses</option>
          </select>

          <input
              v-model="query"
              class="mh-input"
              type="search"
              placeholder="Search by game id..."
          />
        </div>
      </header>

      <!-- LIST -->
      <section class="mh-list">
        <div v-if="filtered.length === 0" class="mh-empty">
          No matches found.
        </div>

        <article
            v-for="m in filtered"
            :key="m.id"
            class="mh-card"
        >
          <div class="mh-grid">
            <!-- LEFT: GAME INFO -->
            <div class="mh-info">
              <div class="mh-date-main">{{ formatDate(m.finishedAt) }}</div>
              <div class="mh-date-sub">{{ formatTime(m.finishedAt) }}</div>

              <div class="mh-gameid">
                Game <span>#{{ m.gameId }}</span>
              </div>

              <div class="mh-meta">
                Players <strong>{{ m.playerCount }}</strong>
                · Duration <strong>{{ m.durationMin }}m</strong>
              </div>
            </div>

            <!-- CENTER: RESULT -->
            <div class="mh-result">
      <span
          :class="['mh-badge', m.result === 'WIN' ? 'win' : 'loss']"
      >
        {{ m.result }}
      </span>

              <div class="mh-score">
                Score <strong>{{ m.score }}</strong>
              </div>
            </div>

            <!-- RIGHT: ACTION -->
            <div class="mh-actions">
              <button class="mh-btn" @click="toggleDetails(m.id)">
                {{ openMatchId === m.id ? 'Hide details' : 'View details' }}
              </button>
            </div>
          </div>

          <!-- DETAILS -->
          <div v-if="openMatchId === m.id" class="mh-details">
            <div class="mh-details-title">Players</div>

            <ul class="mh-player-list">
              <li
                  v-for="p in m.players"
                  :key="p"
                  :class="{ winner: p === m.winner }"
              >
                {{ p }}
                <span v-if="p === m.winner">🏆</span>
              </li>
            </ul>
          </div>
        </article>

      </section>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from "vue-router";
import { computed, onMounted, ref } from "vue";

const router = useRouter();

function goHome() {
  router.push("/");
}

const history = ref([]);
const resultFilter = ref("all");
const query = ref("");
const openMatchId = ref(null);

const user = JSON.parse(localStorage.getItem("user") || "{}");
const username = user.username;

onMounted(async () => {
  if (!username) return;

  const res = await fetch(
      `${import.meta.env.VITE_API_BASE_URL}/api/history/${username}`
  );
  const data = await res.json();

  history.value = data.map((r) => ({
    id: r.id,
    gameId: r.gameId,
    finishedAt: r.finishedAt,
    result: r.won ? "WIN" : "LOSS",
    score: r.score,
    playerCount: r.playerCount,
    durationMin: r.durationMin,
    players: r.players,
    winner: r.winnerUsername,
  }));
});

const filtered = computed(() => {
  const q = query.value.trim().toLowerCase();

  return history.value.filter((m) => {
    const matchesQuery =
        !q || m.gameId.toLowerCase().includes(q);

    const matchesResult =
        resultFilter.value === "all" ||
        (resultFilter.value === "win" && m.result === "WIN") ||
        (resultFilter.value === "loss" && m.result === "LOSS");

    return matchesQuery && matchesResult;
  });
});

function toggleDetails(id) {
  openMatchId.value = openMatchId.value === id ? null : id;
}

function formatDate(iso) {
  return new Date(iso).toLocaleDateString(undefined, {
    year: "numeric",
    month: "short",
    day: "2-digit",
  });
}

function formatTime(iso) {
  return new Date(iso).toLocaleTimeString(undefined, {
    hour: "2-digit",
    minute: "2-digit",
  });
}
</script>

<style scoped>
/* ===== PAGE BACKGROUND (same as homepage) ===== */
.mh-page {
  min-height: 100vh;
  padding: 4rem 2rem 2rem;
  background: #faf9fc;
}

/* ===== CONTAINER CARD ===== */
.mh-container {
  max-width: 1000px;
  margin: 0 auto;
  background: white;
  border-radius: 18px;
  padding: 2.5rem;
  box-shadow: 0 8px 24px rgba(0,0,0,0.08);
}

/* ===== HEADER ===== */
.mh-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 2.5rem;
  gap: 1rem;
}

.mh-header-left {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.mh-title {
  font-size: 2.4rem;
  font-weight: 800;
  color: #b10c96;
  margin: 0;
}

.mh-subtitle {
  color: #555;
  margin-top: 0.3rem;
}

/* ===== BACK BUTTON ===== */
.mh-back {
  background: #b10c96;
  color: white;
  border: none;
  padding: 0.55rem 1.3rem;
  border-radius: 10px;
  font-weight: 600;
  cursor: pointer;
  transition: 0.25s;
  margin-bottom: 1.5rem;
}
.mh-back:hover {
  background: #770494;
  transform: translateY(-2px);
}

/* ===== CONTROLS ===== */
.mh-controls {
  display: flex;
  gap: 0.75rem;
}

.mh-select,
.mh-input {
  padding: 0.6rem 1rem;
  border-radius: 10px;
  border: 1px solid #ccc;
  background: white;
}

/* ===== LIST ===== */
.mh-list {
  display: flex;
  flex-direction: column;
  gap: 1.4rem;
}

/* ===== CARD ===== */
.mh-card {
  background: white;
  border-radius: 16px;
  padding: 1.6rem 2rem;
  border: 1px solid #eee;
  transition: 0.25s;
  color: #111827;
}
.mh-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 18px rgba(0,0,0,0.1);
}

/* ===== GRID LAYOUT ===== */
.mh-grid {
  display: grid;
  grid-template-columns: 1.6fr 1fr auto;
  align-items: center;
  gap: 2.5rem;
}

/* ===== LEFT INFO ===== */
.mh-info {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}

.mh-date-main {
  font-weight: 800;
  font-size: 1.05rem;
  color: #111827;
}

.mh-date-sub {
  font-size: 0.85rem;
  color: #374151;
}

.mh-gameid {
  margin-top: 0.4rem;
  font-weight: 600;
  color: #111827;
}

.mh-gameid span {
  font-weight: 800;
}

.mh-meta {
  font-size: 0.9rem;
  color: #555;
}

.mh-meta strong {
  color: #111827;
}

/* ===== RESULT COLUMN ===== */
.mh-result {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.45rem;
}

.mh-score {
  font-size: 0.95rem;
  font-weight: 600;
  color: #111827;
}

/* ===== BADGES ===== */
.mh-badge {
  padding: 0.4rem 1rem;
  border-radius: 999px;
  font-weight: 800;
  font-size: 0.8rem;
}
.mh-badge.win {
  background: #dcfce7;
  color: #166534;
}
.mh-badge.loss {
  background: #fee2e2;
  color: #991b1b;
}

/* ===== ACTIONS ===== */
.mh-actions {
  display: flex;
  align-items: center;
}

.mh-btn {
  background: #faf9fc;
  border: 1px solid #ccc;
  border-radius: 10px;
  padding: 0.45rem 1rem;
  cursor: pointer;
  font-weight: 600;
  transition: 0.2s;
}
.mh-btn:hover {
  background: #770494;
  color: white;
}

/* ===== DETAILS ===== */
.mh-details {
  margin-top: 1.4rem;
  padding: 1.1rem 1.5rem;
  background: #faf9fc;
  border-radius: 12px;
  border: 1px solid #eee;
}

.mh-details-title {
  font-weight: 700;
  margin-bottom: 0.6rem;
}

.mh-player-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.mh-player-list li {
  padding: 0.2rem 0;
}

.mh-player-list li.winner {
  color: #b10c96;
  font-weight: 700;
}

/* ===== EMPTY ===== */
.mh-empty {
  text-align: center;
  padding: 2rem;
  border: 2px dashed #ccc;
  border-radius: 14px;
  color: #777;
}
</style>
