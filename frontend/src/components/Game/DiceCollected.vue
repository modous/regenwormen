<!--<template>-->
<!--  <div class="dice-collected">-->
<!--    <h2>Verzameld</h2>-->
<!--    <div class="dice">-->
<!--      <span v-for="(die, index) in collectedDice" :key="index" class="die">-->
<!--        ✅ {{ die }}-->
<!--      </span>-->
<!--    </div>-->
<!--  </div>-->
<!--</template>-->

<!--<script setup>-->
<!--defineProps({-->
<!--  collectedDice: { type: Array, required: true }-->
<!--});-->
<!--</script>-->

<template>
  <div class="dice-collected-wrapper">
    <p v-if="hasStartedRoll" class="points">
      🎯 Points this round: <strong>{{ roundPoints }}</strong>
    </p>

    <div class="dice-area">
       <span v-for="(face, index) in collectedDice" :key="index" class="die die-collected">
         {{ faceEmoji(face) }}
       </span>
    </div>
  </div>
</template>

<script setup>
defineProps({
  collectedDice: { type: Array, required: true },
  roundPoints: { type: Number, required: true },
  hasStartedRoll: Boolean
});

function faceEmoji(face) {
  const map = { ONE: "1️⃣", TWO: "2️⃣", THREE: "3️⃣", FOUR: "4️⃣", FIVE: "5️⃣", SPECIAL: "🪱" };
  return map[face] || face;
}
</script>