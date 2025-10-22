<template>
  <div v-if="visible" class="rules-overlay" @click.self="close">
    <div class="rules-content">
      <button class="close-button" @click="close">✖</button>
      <img src="../../assets/Gamerules.png" alt="Game Rules" class="rules-image" />
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  visible: { type: Boolean, required: true },
});

const emits = defineEmits(["close"]);

function close() {
  emits("close");
}
</script>

<style scoped>
.rules-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-color: rgba(200, 200, 200, 0.6);
  backdrop-filter: blur(0.5px);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.rules-content {
  position: relative;
}

.close-button {
  position: absolute;
  top: 10px;
  right: 10px;
  background-color: rgba(255,255,255,0.8);
  border: none;
  border-radius: 50%;
  width: 32px;
  height: 32px;
  font-size: 18px;
  cursor: pointer;
  box-shadow: 0 4px 10px rgba(0,0,0,0.3);
}

.rules-image {
  height: 75vh;
  width: auto;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.4);
  object-fit: contain;
}
</style>
