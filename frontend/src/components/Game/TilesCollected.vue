<!--<template>-->
<!--  <div class="tiles-collected" :class="{ current: isCurrentPlayer }">-->
<!--    <div class="tiles">-->
<!--      <div v-for="tile in tiles" :key="tile.value" class="tile">-->
<!--        {{ tile.value }} ({{ tile.worms }} 🪱)-->
<!--      </div>-->
<!--    </div>-->
<!--  </div>-->
<!--</template>-->

<!--<script setup>-->
<!--defineProps({-->
<!--  tiles: { type: Array, required: true },-->
<!--  isCurrentPlayer: { type: Boolean, default: false }-->
<!--});-->
<!--</script>-->

<template>
  <div class="tiles-collected">
    <h3>Mijn Tegels</h3>
    <div class="tiles-list">
      <div v-for="t in tiles" :key="t.value" class="tile-item">
        {{ t.value }} <span class="worms">🪱 x{{ t.points || 1 }}</span>
      </div>
    </div>
    <p class="my-score">Totale punten: <strong>{{ totalScore }}</strong></p>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  tiles: { type: Array, default: () => [] }
})

const totalScore = computed(() =>
    props.tiles.reduce((sum, t) => sum + (t.points || 0), 0)
)
</script>
