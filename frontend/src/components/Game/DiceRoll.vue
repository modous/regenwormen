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

    <div v-if="hasStartedRoll" class="dice-left-text" style="margin: 10px 0; font-weight: bold;">
      Dice left: {{ 8 - collectedCount }}
    </div>

    <div class="dice-area">
      <div
          v-for="(face, idx) in rolledDice"
          :key="idx"
          :class="[
            'die',
            { disabled: disabledFaces.includes(face), chosen: chosenFaces.includes(face), rolling: rolling }
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
  canRoll: { type: Boolean, default: true },
  blocked: { type: Boolean, default: false },
  collectedCount: { type: Number, default: 0 }
});

defineEmits(['roll', 'selectDie']);

function faceEmoji(face) {
  const map = { ONE: "1️⃣", TWO: "2️⃣", THREE: "3️⃣", FOUR: "4️⃣", FIVE: "5️⃣", SPECIAL: "🪱" };
  return map[face] || face;
}
</script>
