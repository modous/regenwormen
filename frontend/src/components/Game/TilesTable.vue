<template>
  <div class="tiles-table">
    <transition-group name="tile">
      <div
          v-for="tile in tiles"
          :key="tile.value"
          :class="['tile', { disabled: !canClaim(tile) }]"
          @click="canClaim(tile) && $emit('pickTile', tile)"
      >
        <span>{{ tile.value }}</span>
        <span class="worms">🪱 x{{ tile.points || 1 }}</span>
      </div>
    </transition-group>
  </div>
</template>

<script setup>
const props = defineProps({
  tiles: { type: Array, required: true },
  currentPoints: { type: Number, default: 0 },
  hasWormInCurrentThrow: { type: Boolean, required: true }
});

defineEmits(['pickTile']);

function canClaim(tile) {
  return (
      props.currentPoints >= tile.value &&
      props.hasWormInCurrentThrow
  );
}
</script>
