<template>
  <div class="tiles-box" :class="{ 'active-player': isCurrentPlayer }">
    <h3>{{ playerName }}</h3>

    <!-- ENEMY: toon ALLEEN de topTile -->
    <div class="tiles-list">
      <transition-group name="tile">
        <div
            v-if="topTile"
            :key="topTile.value"
            class="tile"
            @click="$emit('steal', playerName)"
        >
          <span>{{ topTile.value }}</span>
          <span class="worms">🪱 x{{ topTile.points || 1 }}</span>
        </div>
      </transition-group>
      <div v-if="!topTile" class="no-tile">
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
  isCurrentPlayer: { type: Boolean, default: false },
})

defineEmits(["steal"])

// ✅ score blijft correct, ook na steal
const totalScore = computed(() =>
    props.tiles.reduce((sum, t) => sum + (t.points || 0), 0)
)
</script>
