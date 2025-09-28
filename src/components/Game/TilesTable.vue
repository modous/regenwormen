<template>
  <div>
    <h2>Tiles Table</h2>
    <div style="display:flex;flex-wrap:wrap;gap:5px;">
      <button
          v-for="tile in tiles"
          :key="tile"
          @click="$emit('takeTile', tile)"
          :disabled="tile > total || !hasWorm"
          style="padding:5px;border:1px solid black;"
      >
        {{ tile }}
      </button>
    </div>
  </div>
</template>

<script setup>
defineProps({ tiles: Array, total: Number, hasWorm: Boolean })
</script>
