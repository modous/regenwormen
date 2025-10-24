<template>
  <div class="dice-collected-container">
    <h2 class="section-title">Verzamelde dobbelstenen</h2>
    <div class="dice-container">
      <div
          v-for="(die, index) in collectedDice"
          :key="index"
          class="dice-wrapper"
      >
        <img
            v-if="imageExists(getDiceImage(die))"
            :src="getDiceImage(die)"
            class="dice-face"
            @error="handleImageError(index)"
        />
        <div v-else class="dice-fallback">{{ die }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";

const props = defineProps({ collectedDice: Array });
const missingImages = ref({});

function getDiceImage(face) {
  if (face === "SPECIAL") return "/dice/special.png";
  return `/dice/dice${face}.png`;
}

function imageExists(path) {
  return !missingImages.value[path];
}

function handleImageError(index) {
  const die = props.collectedDice[index];
  const path = getDiceImage(die);
  missingImages.value[path] = true;
}
</script>

<style scoped>
.dice-collected-container {
  margin-top: 20px;
  text-align: center;
}

.dice-container {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 8px;
}

.dice-fallback {
  width: 50px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #444;
  color: #fff;
  font-weight: bold;
  font-size: 18px;
  border-radius: 8px;
}
</style>
