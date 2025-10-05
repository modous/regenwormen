<template>
  <div class="tiles-other">
    <h3>Tegenspelers</h3>
    <div class="tiles">
      <div v-for="tile in tiles" :key="tile.value" class="tile">
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
