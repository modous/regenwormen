<template>
  <div>
    <h2>Other Players</h2>
    <div v-for="p in players" :key="p.id" style="margin-bottom:10px;">
      <strong>{{ p.name }}</strong>:
      <div style="display:flex;gap:5px;">
        <div v-for="tile in p.tiles" :key="tile" style="background:lightcoral;padding:5px;">
          {{ tile }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({ players: Array })
</script>
