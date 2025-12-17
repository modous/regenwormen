<!--<template>-->
<!--  <div class="tiles-table">-->
<!--    <h2>Tegels op tafel</h2>-->
<!--    <div class="tiles">-->
<!--      <div-->
<!--          v-for="tile in tiles"-->
<!--          :key="tile.value"-->
<!--          class="tile"-->
<!--          @click="$emit('pickTile', tile)"-->
<!--      >-->
<!--        {{ tile.value }} ({{ tile.worms }} 🪱)-->
<!--      </div>-->
<!--    </div>-->
<!--  </div>-->
<!--</template>-->

<!--<script setup>-->
<!--defineProps({-->
<!--  tiles: { type: Array, required: true }-->
<!--});-->
<!--</script>-->

<template>
  <div class="tiles-table">
    <div
        v-for="tile in tiles"
        :key="tile.value"
        :class="['tile', { disabled: !canClaim(tile) }]"
        @click="$emit('pickTile', tile)"
    >
      <span>{{ tile.value }}</span>
      <span class="worms">🪱 x{{ tile.points || 1 }}</span>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  tiles: { type: Array, required: true },
  currentPoints: { type: Number, default: 0 }
});

defineEmits(['pickTile']);

function canClaim(tile) {
  // Een tegel kan gepakt worden als de punten van de speler >= tegelwaarde
  return props.currentPoints >= tile.value;
}
</script>