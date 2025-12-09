<!--<template>-->
<!--  <div class="tiles-table">-->
<!--    <div-->
<!--        v-for="tile in tiles"-->
<!--        :key="tile.value"-->
<!--        class="tile"-->
<!--    >-->
<!--      <span class="value">{{ tile.value }}</span>-->
<!--      <span class="worms">{{ tile.worms }}🪱</span>-->
<!--    </div>-->

<!--    <div v-if="topTile" class="tile">-->
<!--      <span class="value">{{ topTile.value }}</span>-->
<!--      <span class="worms">{{ topTile.worms }}🪱</span>-->
<!--    </div>-->
<!--  </div>-->
<!--</template>-->

<!--<script setup>-->
<!--defineProps({-->
<!--  tiles: { type: Array, required: true },-->
<!--  topTile: { type: Object, default: null }-->
<!--})-->
<!--</script>-->

<!--<style scoped>-->
<!--.tiles-table {-->
<!--  display: flex;-->
<!--  justify-content: center;-->
<!--  flex-wrap: wrap;-->
<!--  gap: 0.8rem;-->
<!--  margin-top: 1rem;-->
<!--}-->

<!--.tile {-->
<!--  background: #fff;-->
<!--  border: 2px solid #4caf50;-->
<!--  border-radius: 10px;-->
<!--  padding: 10px 15px;-->
<!--  cursor: pointer;-->
<!--  font-weight: bold;-->
<!--  transition: all 0.2s ease;-->
<!--}-->

<!--.tile:hover {-->
<!--  background: #e8f5e9;-->
<!--  transform: scale(1.05);-->
<!--}-->

<!--.tile.disabled {-->
<!--  border-color: #aaa;-->
<!--  background: #f3f3f3;-->
<!--  color: #999;-->
<!--  cursor: not-allowed;-->
<!--  transform: none;-->
<!--}-->

<!--.worms {-->
<!--  display: block;-->
<!--  font-size: 0.8rem;-->
<!--  color: #555;-->
<!--}-->



<!--</style>-->
<!--<template>-->
<!--  <div class="tiles-box">-->
<!--    <h3>Tiles van speler</h3>-->
<!--  <div class="tiles-list">-->
<!--    <div-->
<!--        v-for="(t, index) in tiles"-->
<!--        :key="t.value"-->
<!--        class="tile-item other-player"-->
<!--        :class="{ last: index === tiles.length - 1 }"-->
<!--        @click="$emit('steal', t)"-->
<!--    >-->
<!--      <span class="value">{{ t.value }}</span>-->
<!--      <span class="worms">🪱 x{{ t.points || 1 }}</span>-->
<!--    </div>-->

<!--    <div v-if="topTile" class="tile-item other-player top-tile" @click="$emit('steal', topTile)">-->
<!--      <span class="value">{{ topTile.value }}</span>-->
<!--      <span class="worms">🪱 x{{ topTile.points || 1 }}</span>-->
<!--    </div>-->
<!--  </div>-->
<!--  </div>-->
<!--</template>-->

<!--<script setup>-->
<!--defineProps({-->
<!--  tiles: { type: Array, required: true },-->
<!--  topTile: { type: Object, default: null }-->
<!--})-->
<!--</script>-->

<template>
  <div class="tiles-box">
    <h3>{{ playerName }}</h3>
    <div class="tiles-list">
      <div
          v-for="(t, index) in tiles"
          :key="t.value"
          class="tile-item"
          :class="{ last: index === tiles.length - 1 }"
          @click="$emit('steal', t)"
      >
        <span class="value">{{ t.value }}</span>
        <span class="worms">🪱 x{{ t.points || 1 }}</span>
      </div>

      <div v-if="topTile" class="tile-item top-tile" @click="$emit('steal', topTile)">
        <span class="value">{{ topTile.value }}</span>
        <span class="worms">🪱 x{{ topTile.points || 1 }}</span>
      </div>
    </div>
    <p class="my-score">Totale punten: <strong>{{ totalScore }}</strong></p>
  </div>
</template>

<script setup>
import { computed } from "vue"

const props = defineProps({
  tiles: { type: Array, default: () => [] },
  topTile: { type: Object, default: null },
  playerName: { type: String, default: "Speler" },
})

const emits = defineEmits(["steal"])

const totalScore = computed(() => props.tiles.reduce((sum, t) => sum + (t.points || 0), 0))
</script>

<style scoped>
.tile-item {
  background: #fefefe;
  border: 2px solid #2196f3; /* zelfde als collected tiles */
  border-radius: 8px;
  padding: 8px 12px;
  font-weight: 600;
  box-shadow: 0 2px 4px rgba(0,0,0,0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.tile-item:hover {
  transform: scale(1.05);
  background: #e8f5e9;
}

.tiles-box {
  background: #fff;
  border: 2px solid #2196f3;
  border-radius: 12px;
  padding: 1rem;
  box-shadow: 0 3px 8px rgba(0,0,0,0.15);
  min-width: 200px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.tiles-box h3 {
  font-size: 1.1rem;
  margin-bottom: 0.5rem;
  color: #111;
}

.tiles-list {
  display: flex;
  flex-wrap: wrap;
  gap: 0.6rem;
  justify-content: center;
}

.my-score {
  font-weight: 700;
  color: #1565c0;
  margin-top: 0.5rem;
}
</style>
