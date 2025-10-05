<template>
  <div class="tiles-collected" :class="{ current: isCurrentPlayer }">
    <h3>Mijn tegels</h3>
    <div class="tiles">
      <div v-for="tile in tiles" :key="tile.value" class="tile">
        {{ tile.value }} ({{ tile.worms }} 🪱)
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  tiles: { type: Array, required: true },
  isCurrentPlayer: { type: Boolean, default: false }
});
</script>
