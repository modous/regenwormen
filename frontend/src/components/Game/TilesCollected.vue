<template>
  <div class="tiles-collected">
    <div class="tiles-box">
      <h3>Mijn Tegels</h3>
      <div class="tiles-list">
        <div v-for="t in tiles" :key="t.value" class="tile-item">
          {{ t.value }} <span class="worms">🪱 x{{ t.points || 1 }}</span>
        </div>
      </div>
      <p class="my-score">Totale punten: <strong>{{ totalScore }}</strong></p>
    </div>
  </div>
</template>

<script setup>
import { computed } from "vue"

const props = defineProps({
  tiles: { type: Array, default: () => [] },
})

const totalScore = computed(() => props.tiles.reduce((sum, t) => sum + (t.points || 0), 0))
</script>
