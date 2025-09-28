<template>
  <div>
    <h2>Roll</h2>
    <button @click="$emit('roll')">Roll ({{ rolled.length }})</button>
    <div v-if="rolled.length > 0" style="display:flex;gap:5px;">
      <button
          v-for="(d,i) in rolled"
          :key="i"
          @click="$emit('choose', d)"
          style="padding:5px;border:1px solid black;"
      >
        {{ d === 6 ? "Worm" : d }}
      </button>
    </div>
  </div>
</template>

<script setup>
defineProps({ rolled: Array })
</script>
