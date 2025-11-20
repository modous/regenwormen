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
  <div class="my-section">
    <h3>Mijn Tegels</h3>
    <div class="my-tiles-list">
      <div v-for="t in tiles" :key="t.value" class="my-tile">
        {{ t.value }} <small>🪱 x{{ t.points }}</small>
      </div>
    </div>
    <p class="my-score">Totale punten: <strong>{{ totalScore }}</strong></p>
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  tiles: { type: Array, default: () => [] }
});

const totalScore = computed(() =>
    props.tiles.reduce((sum, t) => sum + (t.points || 0), 0)
);
</script>