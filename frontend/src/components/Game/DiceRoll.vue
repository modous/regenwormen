<!--<template>-->
<!--  <div class="dice-roll">-->
<!--    <h2>Dobbelstenen</h2>-->
<!--    <button @click="$emit('roll')">Gooien</button>-->
<!--    <div class="dice">-->
<!--      <span-->
<!--          v-for="(die, index) in rolledDice"-->
<!--          :key="index"-->
<!--          class="die"-->
<!--          @click="$emit('selectDie', die)"-->
<!--      >-->
<!--        🎲 {{ die }}-->
<!--      </span>-->
<!--    </div>-->
<!--  </div>-->
<!--</template>-->

<!--<script setup>-->
<!--defineProps({-->
<!--  rolledDice: { type: Array, required: true }-->
<!--});-->
<!--</script>-->

<template>
  <div class="dice-roll-section">
    <button
        v-if="isCurrentPlayer && !isBusted"
        class="roll-btn"
        @click="$emit('roll')"
        :disabled="rolling || (!canRoll && isCurrentPlayer)"
    >
      {{ hasStartedRoll ? "Roll Again" : "🎲 Roll Dice" }}
    </button>

    <div class="dice-area">
      <div
          v-for="(face, idx) in rolledDice"
          :key="idx"
          :class="[
            'die',
            { disabled: disabledFaces.includes(face), chosen: chosenFaces.includes(face) }
          ]"
          @click="$emit('selectDie', face)"
      >
        {{ faceEmoji(face) }}
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  rolledDice: { type: Array, default: () => [] },
  disabledFaces: { type: Array, default: () => [] },
  chosenFaces: { type: Array, default: () => [] },
  isCurrentPlayer: Boolean,
  isBusted: Boolean,
  rolling: Boolean,
  hasStartedRoll: Boolean,
  canRoll: { type: Boolean, default: true } // Voor extra checks (bijv. timer)
});

defineEmits(['roll', 'selectDie']);

function faceEmoji(face) {
  const map = { ONE: "1️⃣", TWO: "2️⃣", THREE: "3️⃣", FOUR: "4️⃣", FIVE: "5️⃣", SPECIAL: "🪱" };
  return map[face] || face;
}
</script>