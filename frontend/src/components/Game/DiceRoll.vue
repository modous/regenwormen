<template>
  <div class="dice-roll-container">
    <h2 class="section-title">Gooi de dobbelstenen</h2>
    <button @click="rollDice" class="btn-roll">Gooi</button>

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
import { listGames, createGame, getGame, joinGame, startGame } from "../../api/gameApi.js";

export default {
  name: "DiceRoll",
  setup() {
    const dice = ref([]);
    const gameState = ref({ collectedDice: [] });
    const currentGameId = ref(null);
    const currentPlayerId = ref("player1");

    async function initGame() {
      const games = await listGames();
      if (games.length === 0) {
        const created = await createGame("Room 1", 4);
        currentGameId.value = created.id;
      } else {
        currentGameId.value = games[0].id;
      }
      await joinGame(currentGameId.value, currentPlayerId.value);
      await refreshGameState();
    }

    async function refreshGameState() {
      if (!currentGameId.value) return;
      const game = await getGame(currentGameId.value);
      gameState.value = game;
    }

    // placeholder-functies, want backend heeft nog geen dice-logica
    async function rollDice() {
      // tijdelijk lokale simulatie tot backend dit ondersteunt
      const rolled = Array.from({ length: 5 }, () =>
          Math.ceil(Math.random() * 6)
      );
      dice.value = rolled.map((d) => ({
        value: d,
        img: `/assets/dice/dice-${d}.png`,
      }));
      await refreshGameState();
    }

    async function pickDie(die) {
      // zelfde: geen backend-logica, enkel UI update
      gameState.value.collectedDice.push({
        value: die.value,
        img: die.img,
      });
      dice.value = dice.value.filter((d) => d.value !== die.value);
    }

    onMounted(initGame);

    return {
      dice,
      gameState,
      rollDice,
      pickDie,
    };
  },
};
</script>

<style>
.dice-roll-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
}

.dice-container,
.collected-dice {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  justify-content: center;
}

.dice {
  width: 50px;
  height: 50px;
  cursor: pointer;
  display: flex;
  justify-content: center;
  align-items: center;
  border: 1px solid #333;
  border-radius: 4px;
  background-color: rgba(255, 255, 255, 0);
}

.dice.collected {
  background-color: rgba(238, 238, 238, 0);
}

.dice-img {
  width: 100%;
  height: 100%;
}

.dice-number {
  font-weight: bold;
  font-size: 1.2rem;
  color: black;
}

.btn-roll {
  padding: 0.5rem 1rem;
  cursor: pointer;
  font-weight: bold;
}
</style>
