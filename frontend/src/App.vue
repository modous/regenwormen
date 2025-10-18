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

import { getGameState, pickTile as apiPickTile } from "@/api/mockApi.js";

const rolledDice = ref([]);
const collectedDice = ref([]);
const tilesOnTable = ref([]);
const players = ref([]);
const currentPlayerId = ref(1);

const otherPlayers = computed(() =>
    players.value.filter((p) => p.id !== currentPlayerId.value)
);

async function loadGame() {
  const state = await getGameState();
  rolledDice.value = state.rolledDice;
  collectedDice.value = state.collectedDice;
  tilesOnTable.value = state.tilesOnTable;
  players.value = state.players;
  currentPlayerId.value = state.currentPlayerId;
}

async function pickTile(tile) {
  const result = await apiPickTile(tile);
  const playerIndex = players.value.findIndex(
      (p) => p.id === currentPlayerId.value
  );
  players.value[playerIndex] = result.player;
  tilesOnTable.value = result.tilesOnTable;
}

onMounted(loadGame);
</script>
