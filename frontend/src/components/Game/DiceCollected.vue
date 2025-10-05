<template>
  <div class="dice-collected">
    <h2>Verzameld</h2>
    <div class="dice">
      <span v-for="(die, index) in collectedDice" :key="index" class="die">
        ✅ {{ die }}
      </span>
    </div>
  </div>
</template>

<script setup>
defineProps({
  collectedDice: { type: Array, required: true }
});
</script>
