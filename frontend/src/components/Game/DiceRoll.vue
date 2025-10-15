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
import { ref } from "vue";
import { rollDice as mockRollDice, pickDie as mockPickDie, getGameState } from "../../api/mockAPI.js";

export default {
  name: "DiceRoll",
  setup() {
    const dice = ref([]);
    const gameState = ref({ collectedDice: [] });

    async function refreshGameState() {
      gameState.value = await getGameState();
    }

    async function rollDice() {
      const rolled = await mockRollDice();
      dice.value = rolled.map(d => ({
        value: d,
        img: d !== "SPECIAL" ? `/assets/dice/dice-${d}.png` : `/assets/dice/dice-special.png`
      }));
      await refreshGameState();
    }

    async function pickDie(die) {
      const result = await mockPickDie(die.value);
      dice.value = result.rolledDice.map(d => ({
        value: d,
        img: d !== "SPECIAL" ? `/assets/dice/dice-${d}.png` : `/assets/dice/dice-special.png`
      }));
      await refreshGameState();
    }

    refreshGameState();

    return {
      dice,
      gameState,
      rollDice,
      pickDie
    };
  }
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
  background-color: rgba(255, 255, 255, 0); /* transparant */
}

.dice.collected {
  background-color: rgba(238, 238, 238, 0); /* transparant */
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
