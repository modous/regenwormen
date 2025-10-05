<template>
  <div class="tiles-table">
    <h2>Tegels op tafel</h2>
    <div class="tiles">
      <div
          v-for="tile in tiles"
          :key="tile.value"
          class="tile"
          @click="$emit('pickTile', tile)"
      >
        {{ tile.value }} ({{ tile.worms }} 🪱)
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  tiles: { type: Array, required: true }
});
</script>
