<template>
  <div class="dice-collected-section" v-if="hasStartedRoll">
    <h3>Gekozen Dobbelstenen</h3>
    <div class="dice-area">
      <div v-for="(face, idx) in collectedDice" :key="idx" class="die die-collected">
        {{ faceEmoji(face) }}
      </div>
    </div>
    <p class="points">Huidige punten: {{ roundPoints }}</p>
  </div>
</template>

<script setup>
defineProps({
  collectedDice: { type: Array, default: () => [] },
  roundPoints: { type: Number, default: 0 },
  hasStartedRoll: Boolean
});

function faceEmoji(face) {
  const map = { ONE: "1️⃣", TWO: "2️⃣", THREE: "3️⃣", FOUR: "4️⃣", FIVE: "5️⃣", SPECIAL: "🪱" };
  return map[face] || face;
}
</script>
