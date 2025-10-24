<template>
  <div class="tiles-container tiles-scroll">
    <div v-for="tile in tilesOnTable" :key="tile.value" class="tile">
      <div class="tile-value">{{ tile.value }}</div>
      <div class="tile-worms">🐛 x{{ tile.worms }}</div>
    </div>
  </div>

</template>

<script setup>
defineProps({
  tiles: Array,
  isCurrentPlayer: Boolean
});
</script>
