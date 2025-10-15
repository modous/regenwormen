<template>
  <div class="tiles-table">
    <h2 class="section-title">Tegels op tafel</h2>
    <div class="tiles">
      <div v-for="tile in tiles" :key="tile.value" class="tile" @click="$emit('pickTile', tile)">
        <div class="tile-value">{{ tile.value }}</div>
        <div class="special-icon">⭐ x{{ tile.special }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({ tiles: Array });
</script>
