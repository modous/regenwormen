<template>
  <div
      class="music-badge"
      :class="{ expanded: isExpanded, playing: isPlaying }"
      role="region"
      aria-label="Game music player"
      @mouseenter="onEnter"
      @mouseleave="onLeave"
  >
    <!-- Compacte knop -->
    <button
        class="badge-button"
        :aria-label="isPlaying ? 'Pauzeer muziek' : 'Speel muziek'"
        @click="togglePlay"
    >
      <span class="bars" aria-hidden="true">
        <i></i><i></i><i></i>
      </span>
      <span class="title" v-if="!isExpanded">{{ currentTitleShort }}</span>
    </button>

    <!-- Uitgeklapt paneel -->
    <div class="panel" @click.stop>
      <div class="track">
        <span class="now">♪</span>
        <span class="name" :title="currentTitle">{{ currentTitle }}</span>
      </div>

      <div class="controls">
        <button class="ctrl" @click="togglePlay" :aria-label="isPlaying ? 'Pauzeer' : 'Afspelen'">
          <span v-if="!isPlaying">▶</span>
          <span v-else>⏸</span>
        </button>

        <button class="ctrl" @click="nextTrack" aria-label="Volgende track">⏭</button>

        <div class="volume" role="group" aria-label="Volume">
          <button class="ctrl" @click="quieter" aria-label="Zachter">–</button>
          <input
              class="slider"
              type="range"
              min="0" max="100" step="1"
              v-model="volumePct"
              @input="applyVolume"
              aria-label="Volume schuif"
          />
          <button class="ctrl" @click="louder" aria-label="Harder">＋</button>
        </div>
      </div>

      <p v-if="autoplayBlocked" class="hint">
        Autoplay geblokkeerd — klik ▶ om te starten.
      </p>
    </div>

    <!-- Verborgen audio -->
    <audio
        ref="audioRef"
        :src="currentSrc"
        preload="auto"
        @ended="nextTrack"
        @play="isPlaying = true"
        @pause="isPlaying = false"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'

const props = defineProps({
  playlist: { type: Array, default: () => [] },
  autoPlay: { type: Boolean, default: true },
  initialVolume: { type: Number, default: 0.6 },
  accentColor: { type: String, default: '#FFD166' },
  bgColor: { type: String, default: 'rgba(22,22,28,0.9)' },
  positionOffset: { type: Number, default: 12 },
})
//“Deze emits zitten erin om events door te geven aan de oudercomponent als een nummer start, pauzeert of verandert.
// We gebruiken ze nu nog niet, maar dit maakt het component klaar voor uitbreiding.”
const emit = defineEmits(['track-change', 'play', 'pause'])

const audioRef = ref(null)
const idx = ref(0)
const isPlaying = ref(false)
const isExpanded = ref(false)
const autoplayBlocked = ref(false)
const volumePct = ref(Math.round(props.initialVolume * 100))

let collapseTimer = null

function onEnter() {
  if (collapseTimer) {
    clearTimeout(collapseTimer)
    collapseTimer = null
  }
  isExpanded.value = true
}

function onLeave() {
  collapseTimer = setTimeout(() => {
    isExpanded.value = false
    collapseTimer = null
  }, 300)
}

// Huidige track
const current = computed(() => props.playlist[idx.value])
const currentSrc = computed(() =>
    typeof current.value === 'string' ? current.value : current.value?.src
)
const currentTitle = computed(() => {
  if (!current.value) return '—'
  if (typeof current.value === 'string') {
    const parts = current.value.split('/')
    return parts[parts.length - 1] || 'Track'
  }
  return current.value.title || current.value.src.split('/').pop() || 'Track'
})
const currentTitleShort = computed(() =>
    currentTitle.value.length > 10 ? currentTitle.value.slice(0, 9) + '…' : currentTitle.value
)

// Volume functies
function applyVolume() {
  if (audioRef.value) audioRef.value.volume = volumePct.value / 100
}
function louder() {
  volumePct.value = Math.min(100, volumePct.value + 5)
  applyVolume()
}
function quieter() {
  volumePct.value = Math.max(0, volumePct.value - 5)
  applyVolume()
}

// Afspelen functies
async function safePlay() {
  const el = audioRef.value
  if (!el) return
  try {
    await el.play()
    autoplayBlocked.value = false
    emit('play')
  } catch {
    autoplayBlocked.value = true
    isPlaying.value = false
  }
}

function togglePlay() {
  const el = audioRef.value
  if (!el) return
  if (el.paused) safePlay()
  else {
    el.pause()
    emit('pause')
  }
}

function nextTrack() {
  if (!props.playlist.length) return
  idx.value = (idx.value + 1) % props.playlist.length
  emit('track-change', idx.value)
  if (audioRef.value) {
    audioRef.value.currentTime = 0
    if (isPlaying.value && !autoplayBlocked.value) safePlay()
  }
}

onMounted(() => {
  const saved = Number(localStorage.getItem('musicBadge.volume'))
  if (!isNaN(saved) && saved >= 0 && saved <= 100) volumePct.value = saved
  applyVolume()

  if (props.autoPlay && props.playlist.length && audioRef.value) {
    safePlay()
  }
})

watch(volumePct, (v) => {
  localStorage.setItem('musicBadge.volume', String(v))
  applyVolume()
})

watch(currentSrc, () => {
  if (audioRef.value) audioRef.value.src = currentSrc.value || ''
})
</script>

<style scoped>
.music-badge {
  position: fixed;
  top: v-bind('props.positionOffset + "px"');
  left: v-bind('props.positionOffset + "px"');
  z-index: 9999;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  user-select: none;
}
.badge-button {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 10px;
  border-radius: 999px;
  border: 1px solid rgba(255,255,255,0.14);
  background: v-bind('props.bgColor');
  color: #fff;
  backdrop-filter: blur(6px);
  cursor: pointer;
  transition: transform 120ms ease, box-shadow 120ms ease, border-color 120ms ease;
  box-shadow: 0 6px 18px rgba(0,0,0,0.25);
}
.music-badge.playing .badge-button { border-color: v-bind('props.accentColor'); }
.badge-button:hover { transform: translateY(-1px); }

.bars {
  position: relative;
  width: 14px; height: 12px;
  display: inline-flex; align-items: end; gap: 2px;
}
.bars i {
  display: block; width: 3px; height: 6px;
  background: v-bind('props.accentColor');
  border-radius: 2px;
  opacity: 0.85;
  animation: eq 1.2s ease-in-out infinite;
}
.bars i:nth-child(2) { animation-delay: 0.2s; }
.bars i:nth-child(3) { animation-delay: 0.4s; }
.music-badge:not(.playing) .bars i { animation: none; height: 6px; opacity: 0.55; }

@keyframes eq {
  0%, 100% { height: 4px }
  50%      { height: 12px }
}

.title {
  font-size: 12px; opacity: 0.9; max-width: 90px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}

.panel {
  pointer-events: none;
  opacity: 0;
  transform: translateY(-4px);
  transition: opacity 150ms ease, transform 150ms ease;
  position: absolute;
  left: 0;
  top: 42px;
  min-width: 260px;
  background: v-bind('props.bgColor');
  color: #fff;
  border: 1px solid rgba(255,255,255,0.14);
  border-radius: 14px;
  padding: 10px;
  box-shadow: 0 14px 30px rgba(0,0,0,0.35);
}
.music-badge.expanded .panel {
  pointer-events: auto;
  opacity: 1;
  transform: translateY(0);
}

.track {
  display: flex; align-items: center; gap: 8px;
  font-size: 13px; line-height: 1.1;
  margin-bottom: 8px;
}
.track .now { color: v-bind('props.accentColor'); }
.track .name { flex: 1; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }

.controls {
  display: flex; align-items: center; gap: 8px;
}
.ctrl {
  padding: 6px 10px;
  border-radius: 10px;
  border: 1px solid rgba(255,255,255,0.12);
  background: rgba(255,255,255,0.06);
  color: #fff; cursor: pointer;
  transition: transform 100ms ease, background 120ms ease, border-color 120ms ease;
}
.ctrl:hover { transform: translateY(-1px); border-color: rgba(255,255,255,0.22); }

.volume {
  display: flex; align-items: center; gap: 6px; margin-left: auto;
}
.slider {
  width: 110px; height: 6px;
  appearance: none; background: rgba(255,255,255,0.18); border-radius: 999px; outline: none;
}
.slider::-webkit-slider-thumb {
  appearance: none; width: 14px; height: 14px; border-radius: 50%;
  background: v-bind('props.accentColor'); border: none; box-shadow: 0 1px 2px rgba(0,0,0,0.35);
}
.slider::-moz-range-thumb {
  width: 14px; height: 14px; border-radius: 50%;
  background: v-bind('props.accentColor'); border: none;
}

.hint {
  margin-top: 8px; font-size: 12px; opacity: 0.8;
}
</style>
