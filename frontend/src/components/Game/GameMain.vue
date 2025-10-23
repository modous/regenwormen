<template>
  <div class="game">
    <h1>Regenwormen</h1>

    <DiceRoll
        :rolledDice="rolledDice"
        @roll="rollDice"
        @selectDie="selectDie"
    />

    <DiceCollected :collectedDice="collectedDice" />

    <TilesTable
        :tiles="tilesOnTable"
        @pickTile="pickTile"
    />

    <div class="players">
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

    <button class="help-button" @click="showRules = true">❓</button>

    <HowToPlayButton :visible="showRules" @close="showRules = false" />

  </div>
</template>

<script setup>
import { ref, computed } from "vue";
import DiceRoll from "./DiceRoll.vue";
import DiceCollected from "./DiceCollected.vue";
import TilesCollected from "./TilesCollected.vue";
import TilesOtherPlayer from "./TilesOtherPlayer.vue";
import TilesTable from "./TilesTable.vue";
import HowToPlayButton from "./HowToPlayButton.vue";

const showRules = ref(false);
const rolledDice = ref([]);
const collectedDice = ref([]);
const tilesOnTable = ref([
  { value: 21, worms: 1 },
  { value: 22, worms: 1 },
  { value: 23, worms: 1 },
  { value: 24, worms: 1 },
]);

const players = ref([
  { id: 1, name: "Jij", tiles: [] },
  { id: 2, name: "Tegenstander 1", tiles: [] },
  { id: 3, name: "Tegenstander 2", tiles: [] },
]);

const currentPlayerId = ref(1);

const otherPlayers = computed(() =>
    players.value.filter(p => p.id !== currentPlayerId.value)
);

function rollDice() {
  // tijdelijk: random dobbelstenen mocken
  rolledDice.value = Array.from({ length: 5 }, () =>
      Math.random() < 0.2 ? "worm" : Math.floor(Math.random() * 6) + 1
  );
}

function selectDie(die) {
  collectedDice.value.push(die);
  rolledDice.value = rolledDice.value.filter(d => d !== die);
}

function pickTile(tile) {
  players.value[0].tiles.push(tile);
  tilesOnTable.value = tilesOnTable.value.filter(t => t.value !== tile.value);
}


</script>


<style scoped>
.help-button {
  position: absolute;
  top: 15px;
  right: 20px;
  background-color: #f0f0f0;
  border: none;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  font-size: 22px;
  cursor: pointer;
  box-shadow: 0 4px 10px rgba(0,0,0,0.2);
  transition: background-color 0.2s ease;
}
.help-button:hover {
  background-color: #e0e0e0;
}
</style>
