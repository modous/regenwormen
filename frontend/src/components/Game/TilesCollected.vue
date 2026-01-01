<template>
  <div class="tiles-box" :class="{ 'active-player': isCurrentPlayer }">
    <h3>Mijn Tegels</h3>
    <div class="tiles-list">
      <div v-for="t in tiles" :key="t.value" class="tile">
        <span>{{ t.value }}</span>
        <span class="worms">🪱 x{{ t.points || 1 }}</span>
      </div>
    </div>
    <p class="my-score">Totale punten: <strong>{{ totalScore }}</strong></p>
  </div>
</template>

<script setup>
import { computed } from "vue"

const props = defineProps({
  tiles: { type: Array, default: () => [] },
  isCurrentPlayer: { type: Boolean, default: false },
})

const totalScore = computed(() => props.tiles.reduce((sum, t) => sum + (t.points || 0), 0))
</script>
