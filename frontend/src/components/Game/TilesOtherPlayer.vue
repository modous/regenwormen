<template>
  <div class="tiles-box">
    <h3>{{ playerName }}</h3>

    <!-- 🔴 ENEMY: toon ALLEEN de topTile -->
    <div class="tiles-list">
      <div
          v-if="topTile"
          class="tile-item top-tile"
          @click="$emit('steal', playerName)"
      >
        <span class="value">{{ topTile.value }}</span>
        <span class="worms">🪱 x{{ topTile.points || 1 }}</span>
      </div>

      <div v-else class="no-tile">
        Geen stealbare tegel
      </div>
    </div>

    <!-- ✅ Score = som van ALLE tiles (ook verborgen) -->
    <p class="my-score">
      Totale punten: <strong>{{ totalScore }}</strong>
    </p>
  </div>
</template>

<script setup>
import { computed } from "vue"

const props = defineProps({
  tiles: { type: Array, default: () => [] }, // ⚠️ alleen voor score
  topTile: { type: Object, default: null },
  playerName: { type: String, default: "Speler" },
})

defineEmits(["steal"])

// ✅ score blijft correct, ook na steal
const totalScore = computed(() =>
    props.tiles.reduce((sum, t) => sum + (t.points || 0), 0)
)
</script>

<style scoped>
.tile-item {
  background: #fefefe;
  border: 2px solid #2196f3;
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
  justify-content: center;
}

.my-score {
  font-weight: 700;
  color: #1565c0;
  margin-top: 0.5rem;
}

.no-tile {
  font-size: 0.9rem;
  color: #777;
}
</style>
