<template>
  <div class="players-container">
    <button class="players-button" @click="open = !open">
      <span class="button-content">
        👥 Spelers ({{ players.length }})
        <span class="chevron">{{ open ? '▲' : '▼' }}</span>
      </span>
    </button>
    <div v-show="open" class="players-panel">
      <ul class="players-list">
        <li v-for="player in players" :key="player.id" class="player-item">
          <div class="player-info">
            <span :class="['status-dot', player.status === 'CONNECTED' ? 'connected' : 'disconnected']"></span>
            <span class="player-name">{{ player.name }}</span>
          </div>
          <span v-if="player.status === 'DISCONNECTED'" class="status-badge">offline</span>
        </li>
      </ul>
    </div>
  </div>
</template>

<script>
export default {
  name: 'PlayerStatusList',
  props: {
    players: {
      type: Array,
      required: true
    }
  },
  data() {
    return { open: false }
  }
}
</script>

<style scoped>
.players-container {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 100;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.players-button {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 12px;
  padding: 12px 16px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  color: white;
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
  transition: all 0.3s ease;
  min-width: 140px;
}

.players-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 28px rgba(102, 126, 234, 0.5);
}

.players-button:active {
  transform: translateY(0);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.button-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.chevron {
  display: inline-block;
  transition: transform 0.3s ease;
  font-size: 12px;
}

.players-panel {
  position: absolute;
  top: 100%;
  right: 0;
  margin-top: 12px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
  min-width: 220px;
  max-height: 300px;
  overflow-y: auto;
  animation: slideDown 0.2s ease;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.players-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.player-item {
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: background-color 0.2s ease;
}

.player-item:last-child {
  border-bottom: none;
}

.player-item:hover {
  background-color: #f8f9ff;
}

.player-info {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
}

.status-dot {
  display: inline-block;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}

.status-dot.connected {
  background-color: #10b981;
  box-shadow: 0 0 8px rgba(16, 185, 129, 0.5);
}

.status-dot.disconnected {
  background-color: #ef4444;
  box-shadow: 0 0 8px rgba(239, 68, 68, 0.5);
}

.player-name {
  color: #1f2937;
  font-weight: 500;
  font-size: 14px;
  word-break: break-word;
}

.status-badge {
  background: #fee2e2;
  color: #991b1b;
  padding: 2px 8px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
  flex-shrink: 0;
}

/* Scrollbar styling */
.players-panel::-webkit-scrollbar {
  width: 6px;
}

.players-panel::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 10px;
}

.players-panel::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 10px;
}

.players-panel::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}

/* Responsive design */
@media (max-width: 768px) {
  .players-container {
    top: 10px;
    right: 10px;
  }

  .players-button {
    padding: 10px 12px;
    font-size: 14px;
    min-width: 120px;
  }

  .players-panel {
    min-width: 180px;
  }

  .player-item {
    padding: 10px 12px;
  }
}
</style>
