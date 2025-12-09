<template>
  <div v-if="visible" class="game-end-overlay">
    <div class="game-end-popup" :class="{ won: won, lost: !won }">
      <h1>{{ won ? "Gefeliciteerd, je hebt gewonnen!" : "Spel verloren" }}</h1>
      <button class="continue-btn" @click="$emit('close')">Doorgaan</button>
    </div>
    <canvas v-if="won" ref="confettiCanvas" class="confetti-canvas"></canvas>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, ref, watch } from "vue"

const props = defineProps({
  visible: { type: Boolean, default: false },
  won: { type: Boolean, default: false }
})
const emit = defineEmits(["close"])

const confettiCanvas = ref(null)
let confettiInterval = null

// Simpele confetti implementatie
function startConfetti() {
  if (!confettiCanvas.value) return
  const ctx = confettiCanvas.value.getContext("2d")
  confettiCanvas.value.width = window.innerWidth
  confettiCanvas.value.height = window.innerHeight

  const confettiPieces = []
  for (let i = 0; i < 150; i++) {
    confettiPieces.push({
      x: Math.random() * window.innerWidth,
      y: Math.random() * window.innerHeight,
      size: Math.random() * 7 + 3,
      speed: Math.random() * 3 + 2,
      color: `hsl(${Math.random() * 360}, 100%, 50%)`
    })
  }

  confettiInterval = setInterval(() => {
    ctx.clearRect(0, 0, window.innerWidth, window.innerHeight)
    confettiPieces.forEach(p => {
      ctx.fillStyle = p.color
      ctx.fillRect(p.x, p.y, p.size, p.size)
      p.y += p.speed
      if (p.y > window.innerHeight) p.y = -p.size
    })
  }, 16)
}

function stopConfetti() {
  clearInterval(confettiInterval)
  confettiInterval = null
}

watch(() => props.visible, (newVal) => {
  if (newVal && props.won) startConfetti()
  else stopConfetti()
})

onUnmounted(() => stopConfetti())
</script>

<style scoped>
.game-end-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  animation: fadeInOverlay 0.5s ease forwards;
}

.game-end-popup {
  background: white;
  border-radius: 16px;
  padding: 3rem 4rem;
  text-align: center;
  transform: scale(0.7);
  opacity: 0;
  animation: popIn 0.5s ease forwards;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.5);
  position: relative;
  z-index: 10000;
}

.game-end-popup.won h1 {
  color: #4caf50;
}

.game-end-popup.lost h1 {
  color: #f44336;
}

.continue-btn {
  margin-top: 1.5rem;
  padding: 0.75rem 1.5rem;
  font-size: 1rem;
  background-color: #1976d2;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.2s, background-color 0.2s;
}
.continue-btn:hover {
  background-color: #1565c0;
  transform: scale(1.05);
}

/* Animaties */
@keyframes fadeInOverlay {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes popIn {
  0% { transform: scale(0.7); opacity: 0; }
  60% { transform: scale(1.1); opacity: 1; }
  100% { transform: scale(1); opacity: 1; }
}

.confetti-canvas {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  pointer-events: none;
  z-index: 9998;
}
</style>
