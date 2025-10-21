<template>
  <div class="game-container">
    <h1 class="game-title">Special Game</h1>

    <!-- TEGELS OP TAFEL NU BOVENAAN -->
    <TilesTable
        :tiles="tilesOnTable"
        @pickTile="pickTile"
        class="tiles-on-top"
    />

    <DiceRoll
        :rolledDice="rolledDice"
        @update:rolledDice="rolledDice = $event"
        @update:collectedDice="collectedDice = $event"
    />

    <DiceCollected :collectedDice="collectedDice" />

    <div class="players-container">
      <TilesCollected
          v-for="player in players"
          :key="player.id"
          :tiles="player.tiles"
          :isCurrentPlayer="player.id === currentPlayerId"
      />
      <TilesOtherPlayer
          v-for="player in otherPlayers"
          :key="player.id"
          :tiles="player.tiles"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import DiceRoll from "./components/Game/DiceRoll.vue";
import DiceCollected from "./components/Game/DiceCollected.vue";
import TilesCollected from "./components/Game/TilesCollected.vue";
import TilesOtherPlayer from "./components/Game/TilesOtherPlayer.vue";
import TilesTable from "./components/Game/TilesTable.vue";

import { getGameState, pickTile } from "@/api/gameApi.js";

const gameId = 1; // tijdelijk hardcoded
const playerId = 1; // idem

const rolledDice = ref([]);
const collectedDice = ref([]);
const tilesOnTable = ref([]);
const players = ref([]);
const currentPlayerId = ref(playerId);

const otherPlayers = computed(() =>
    players.value.filter((p) => p.id !== currentPlayerId.value)
);

async function loadGame() {
  const state = await getGameState(gameId, playerId);
  rolledDice.value = state.rolledDice || [];
  collectedDice.value = state.collectedDice || [];
  tilesOnTable.value = state.tilesOnTable || [];
  players.value = state.players || [];
}

async function pickTileHandler(tile) {
  const result = await pickTile(gameId, playerId, tile.value);
  await loadGame();
}

onMounted(loadGame);
</script>

