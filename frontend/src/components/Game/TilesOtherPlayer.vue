<template>
  <div class="tiles-table">
    <div
        v-for="tile in tiles"
        :key="tile.value"
        class="tile"
    >
      <span class="value">{{ tile.value }}</span>
      <span class="worms">{{ tile.worms }}🪱</span>
    </div>

    <div v-if="topTile" class="tile">
      <span class="value">{{ topTile.value }}</span>
      <span class="worms">{{ topTile.worms }}🪱</span>
    </div>
  </div>
</template>

<script setup>
defineProps({
  tiles: { type: Array, required: true },
  topTile: { type: Object, default: null }
})
</script>

<style scoped>
.tiles-table {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 0.8rem;
  margin-top: 1rem;
}

.tile {
  background: #fff;
  border: 2px solid #4caf50;
  border-radius: 10px;
  padding: 10px 15px;
  cursor: pointer;
  font-weight: bold;
  transition: all 0.2s ease;
}

.tile:hover {
  background: #e8f5e9;
  transform: scale(1.05);
}

.tile.disabled {
  border-color: #aaa;
  background: #f3f3f3;
  color: #999;
  cursor: not-allowed;
  transform: none;
}

.worms {
  display: block;
  font-size: 0.8rem;
  color: #555;
}



</style>
