<template>
  <div class="mh-page">
    <header class="mh-header">
      <div class="mh-header-left">
        <button class="mh-back" @click="goHome">
          ← Back to home
        </button>

        <div>
          <h1 class="mh-title">Match history</h1>
          <p class="mh-subtitle">All games you played, with result.</p>
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

    <section class="mh-list">
      <div v-if="filtered.length === 0" class="mh-empty">
        No matches found.
      </div>

      <article v-for="m in filtered" :key="m.id" class="mh-card">
        <!-- MAIN ROW -->
        <div class="mh-row">
          <div class="mh-card-left">
            <div class="mh-date">
              <div class="mh-date-main">{{ formatDate(m.finishedAt) }}</div>
              <div class="mh-date-sub">{{ formatTime(m.finishedAt) }}</div>
            </div>

            <div class="mh-meta">
              <div class="mh-gameid">
                Game: <span>{{ m.gameId }}</span>
              </div>
              <div class="mh-small">
                Players: <strong>{{ m.playerCount }}</strong>
                · Duration: <strong>{{ m.durationMin }}m</strong>
              </div>
            </div>
          </div>

          <div class="mh-card-right">
            <span :class="['mh-badge', m.result === 'WIN' ? 'win' : 'loss']">
              {{ m.result }}
            </span>

            <div class="mh-score">
              Score: <strong>{{ m.score }}</strong>
            </div>

            <button class="mh-btn" @click="toggleDetails(m.id)">
              {{ openMatchId === m.id ? "Hide details" : "Details" }}
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
</template>

<script setup lang="ts">
import { useRouter } from "vue-router";
import { computed, onMounted, ref } from "vue";


const router = useRouter();

function goHome() {
  router.push("/"); // of "/start" of "/home" afhankelijk van je router
}

type MatchResult = "WIN" | "LOSS";

type MatchHistoryItem = {
  id: string;
  gameId: string;
  finishedAt: string;
  result: MatchResult;
  score: number;
  playerCount: number;
  durationMin: number;
  players: string[];
  winner: string | null;
};

const history = ref<MatchHistoryItem[]>([]);
const resultFilter = ref<"all" | "win" | "loss">("all");
const query = ref("");
const openMatchId = ref<string | null>(null);

const user = JSON.parse(localStorage.getItem("user") || "{}");
const username = user.username;


onMounted(async () => {
  const res = await fetch(`http://localhost:8080/api/history/${username}`);
  const data = await res.json();

  history.value = data.map((r: any) => ({
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
        q.length === 0 || m.gameId.toLowerCase().includes(q);

    const matchesResult =
        resultFilter.value === "all" ||
        (resultFilter.value === "win" && m.result === "WIN") ||
        (resultFilter.value === "loss" && m.result === "LOSS");

    return matchesQuery && matchesResult;
  });
});

function toggleDetails(id: string) {
  openMatchId.value = openMatchId.value === id ? null : id;
}

function formatDate(iso: string) {
  return new Date(iso).toLocaleDateString(undefined, {
    year: "numeric",
    month: "short",
    day: "2-digit",
  });
}

function formatTime(iso: string) {
  return new Date(iso).toLocaleTimeString(undefined, {
    hour: "2-digit",
    minute: "2-digit",
  });
}
</script>


<style scoped>
/* ---------- PAGE ---------- */
.mh-page {
  max-width: 980px;
  margin: 0 auto;
  padding: 24px 16px 48px;
  background: #f6f7f9;
  min-height: 100vh;
  color: #111827;
}

/* ---------- HEADER ---------- */

.mh-header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.mh-back {
  border: 1px solid #d1d5db;
  background: #ffffff;
  color: #111827;
  padding: 8px 12px;
  border-radius: 10px;
  cursor: pointer;
  font-weight: 500;
}

.mh-back:hover {
  background: #f3f4f6;
}

.mh-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
}

.mh-title {
  font-size: 28px;
  margin: 0;
}

.mh-subtitle {
  margin: 6px 0 0;
  color: #6b7280;
}

/* ---------- CONTROLS ---------- */
.mh-controls {
  display: flex;
  gap: 10px;
}

.mh-select,
.mh-input {
  border: 1px solid #d1d5db;
  background: #ffffff;
  color: #111827;
  padding: 10px 12px;
  border-radius: 12px;
}

/* ---------- LIST ---------- */
.mh-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* ---------- CARD ---------- */
.mh-card {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  padding: 14px 16px;
}

/* ---------- ROW ---------- */
.mh-row {
  display: flex;
  justify-content: space-between;
  gap: 16px;
}

/* ---------- LEFT ---------- */
.mh-card-left {
  display: flex;
  gap: 16px;
  align-items: center;
}

.mh-date {
  min-width: 130px;
}

.mh-date-sub,
.mh-small {
  font-size: 13px;
  color: #6b7280;
}

/* ---------- RIGHT ---------- */
.mh-card-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* ---------- BADGES ---------- */
.mh-badge.win {
  background: #dcfce7;
  color: #166534;
  border: 1px solid #86efac;
  padding: 6px 12px;
  border-radius: 999px;
  font-weight: 800;
}

.mh-badge.loss {
  background: #fee2e2;
  color: #991b1b;
  border: 1px solid #fecaca;
  padding: 6px 12px;
  border-radius: 999px;
  font-weight: 800;
}

/* ---------- BUTTON ---------- */
.mh-btn {
  border: 1px solid #d1d5db;
  background: #f9fafb;
  padding: 8px 12px;
  border-radius: 10px;
  cursor: pointer;
}

/* ---------- DETAILS ---------- */
.mh-details {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #e5e7eb;
}

.mh-details-title {
  font-weight: 600;
  margin-bottom: 6px;
}

.mh-player-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.mh-player-list li {
  padding: 4px 0;
}

.mh-player-list li.winner {
  font-weight: 700;
  color: #166534;
}

/* ---------- EMPTY ---------- */
.mh-empty {
  padding: 24px;
  border-radius: 16px;
  border: 1px dashed #d1d5db;
  color: #6b7280;
}
</style>
