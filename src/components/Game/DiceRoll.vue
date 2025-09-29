<template>
  <div class="dice-roll">
    <h2>Dobbelstenen</h2>
    <button @click="$emit('roll')">Gooien</button>
    <div class="dice">
      <span
          v-for="(die, index) in rolledDice"
          :key="index"
          class="die"
          @click="$emit('selectDie', die)"
      >
        🎲 {{ die }}
      </span>
    </div>
  </div>
</template>

<script setup>
defineProps({
  rolledDice: { type: Array, required: true }
});
</script>
