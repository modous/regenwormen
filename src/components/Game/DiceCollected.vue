<template>
  <div>
    <h2>Your Tiles</h2>
    <div style="display:flex;gap:5px;">
      <div v-for="tile in tiles" :key="tile" style="background:lightgreen;padding:5px;">
        {{ tile }}
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({ tiles: Array })
</script>
