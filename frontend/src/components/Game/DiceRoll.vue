<template>
  <div class="dice-roll-container">
    <h2 class="section-title">Gooi de dobbelstenen</h2>
    <button @click="rollDiceHandler" class="btn-roll">Gooi</button>

    <div class="dice-container">
      <div
          v-for="(die, index) in dice"
          :key="index"
          class="dice"
          @click="pickDie(die)"
      >
        <img
            v-if="die.img"
            :src="die.img"
            :alt="die.value"
            class="dice-img"
            @error="die.img = null"
        />
        <span v-else class="dice-number">{{ die.value }}</span>
      </div>
    </div>

    <h2 class="section-title">Verzamelde dobbelstenen</h2>
    <div class="collected-dice">
      <div
          v-for="(die, index) in gameState.collectedDice"
          :key="index"
          class="dice collected"
      >
        <img
            v-if="die.img"
            :src="die.img"
            :alt="die.value"
            class="dice-img"
            @error="die.img = null"
        />
        <span v-else class="dice-number">{{ die.value }}</span>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from "vue";
import { startRoll, listGames, createGame, joinGame, getGame, startGame, listPlayers } from "@/api/gameApi.js";

export default {
  name: "DiceRoll",
  setup() {
    const dice = ref([]);
    const gameState = ref({ collectedDice: [] });
    const currentGameId = ref(null);
    const currentPlayerId = ref("player1");

    async function refreshGameState() {
      if (!currentGameId.value) return;
      const game = await getGame(currentGameId.value);
      gameState.value = game;
    }

    async function initGame() {
      const games = await listGames();
      if (games.length === 0) {
        const created = await createGame("Room 1", 4);
        currentGameId.value = created.id;
      } else {
        currentGameId.value = games[0].id;
      }

      const players = await listPlayers();
      let player = players.find(p => p.username === "player1");
      if (!player) player = players[players.length - 1];
      currentPlayerId.value = player.id;

      await joinGame(currentGameId.value, currentPlayerId.value);

      // Start de game zodat rollen mogelijk is
      await startGame(currentGameId.value);

      await refreshGameState();
    }

    async function rollDiceHandler() {
      const result = await startRoll(currentGameId.value, currentPlayerId.value);
      dice.value = result.rolledDice.map((d) => ({
        value: d,
        img: `/assets/dice/dice-${d}.png`,
      }));
      await refreshGameState();
    }

    async function pickDie(die) {
      gameState.value.collectedDice.push(die);
      dice.value = dice.value.filter((d) => d.value !== die.value);
    }

    onMounted(initGame);

    return {
      dice,
      gameState,
      rollDiceHandler,
      pickDie,
    };
  },
};
</script>
