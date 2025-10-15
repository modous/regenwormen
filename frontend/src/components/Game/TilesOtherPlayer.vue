<template>
  <div class="tiles-other">
    <h3 class="section-title">Tegenspelers</h3>
    <div class="tiles">
      <div v-for="tile in tiles" :key="tile.value" class="tile">
        <div class="tile-value">{{ tile.value }}</div>
        <div class="special-icon">⭐ x{{ tile.special }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({ tiles: Array });
</script>
