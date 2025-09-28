<template>
  <div>
    <h2>Collected Dice</h2>
    <div style="display:flex;gap:5px;">
      <div v-for="(d,i) in dice" :key="i" style="background:lightblue;padding:5px;">
        {{ d === 6 ? "Worm" : d }}
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({ dice: Array })
</script>
