<template>
  <div class="game-container">
    <!-- Spelveld met waarden en punten -->
    <div class="grid">
      <div
          v-for="tile in tiles"
          :key="tile.value"
          class="tile"
          :class="{ collected: tile.collected }"
          @click="collectTile(tile)"
      >
        <!-- Toon tegelwaarde & punten -->
        <div class="tile-content">
          <span v-if="!tile.collected">
            {{ tile.value }} (+{{ tile.points }})
          </span>
          <span v-else>
            ✨ {{ tile.value }} ({{ tile.points }})
          </span>
        </div>
      </div>
    </div>

    <!-- Spelers inventaris met duidelijke info -->
    <div class="player-inventory">
      <h3>Ingezamelde Tegels:</h3>
      <ul class="inventory-list">
        <li
            v-for="(item, index) in collectedTiles"
            :key="index"
            class="inventory-item"
        >
          <div class="item-info">
            <span class="item-value">Tegel {{ item.value }}</span>
            <span class="item-points">+{{ item.points }}</span>
          </div>
        </li>
      </ul>
    </div>

    <!-- Aangehouden score -->
    <div class="score">
      <h2>Totaal score: {{ score }}</h2>
    </div>
  </div>
</template>

<script>
export default {
  name: "Regenwormen_tiles",
  data() {
    return {
      tiles: [], // Tegels met waarde en punten
      score: 0,   // Totaal aantal punten
      collectedTiles: [] // Ingezamelde tegels
    };
  },
  created() {
    this.createTiles(); // Genereer tegels van 21-36 met bijbehorende punten
  },
  methods: {
    // Genereer tegels met waarde en bijbehorende punten
    createTiles() {
      for (let i = 21; i <= 36; i++) {
        this.tiles.push({
          value: i,
          points: this.getPoints(i), // Reken punten direct uit
          collected: false
        });
      }
    },

    // Bereken punten op basis van waarde:
    // 21-24: 1 punt
    // 25-28: 2 punten
    // 29-32: 3 punten
    // 33-36: 4 punten
    getPoints(value) {
      return Math.floor((value - 21) / 4) + 1;
    },

    // Verzameltegels logica
    collectTile(tile) {
      if (!tile.collected) {
        // Markeer als verzameld
        tile.collected = true;

        // Voeg toe aan inventory
        this.collectedTiles.push({
          value: tile.value,
          points: tile.points
        });

        // Update totaalscore
        this.score += tile.points;

        // Geef score update door
        this.$emit('score-changed', this.score);
      }
    }
  }
};
</script>

<style scoped>
.game-container {
  max-width: 1000px;
  margin: 2rem auto;
  padding: 0 1rem;
}

/* Tegel styling */
.grid {
  display: flex;
  overflow-x: auto;
  padding: 1rem 0;
  scroll-behavior: smooth;
}

.tile {
  width: 120px;
  height: 120px;
  background-color: #f0c048;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: bold;
  color: white;
  border-radius: 12px;
  margin: 0 10px;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  text-align: center;
}

.tile-content {
  z-index: 1;
}

.tile[collected="true"] {
  background-color: #666;
  color: #444;
  cursor: default;
}

.tile:hover:not([collected]) {
  background-color: #faa61a;
  transform: translateY(-2px);
}

.tile:active:not([collected]) {
  transform: translateY(2px);
}

/* Punten visueel */
.tile .item-points {
  display: block;
  margin-top: 8px;
  font-size: 18px;
  color: #ffe81f;
  font-weight: normal;
  letter-spacing: 1px;
}

/* Speler inventaris */
.player-inventory {
  margin-top: 2rem;
  padding: 1rem;
  background-color: #2a2a2a;
  border-radius: 12px;
  min-height: 150px;
}

.inventory-list {
  list-style: none;
  padding: 0;
  margin: 0;
  max-height: 300px;
  overflow-y: auto;
  scroll-behavior: smooth;
}

.inventory-list::-webkit-scrollbar {
  width: 8px;
}

.inventory-list::-webkit-scrollbar-thumb {
  background-color: #555;
  border-radius: 4px;
}

.inventory-item {
  padding: 10px 15px;
  border-bottom: 1px solid #3c3c3c;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #222;
  border-radius: 8px;
  margin-bottom: 6px;
  transition: transform 0.2s;
}

.inventory-item:last-child {
  border-bottom: none;
}

.inventory-item .item-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.item-value {
  font-weight: bold;
  color: white;
}

.item-points {
  font-size: 16px;
  color: #ffe81f;
  margin-top: 2px;
}

/* Scoreweergave */
.score {
  position: fixed;
  bottom: 20px;
  right: 20px;
  background-color: #1a1a1a;
  color: white;
  padding: 1rem 2rem;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.4);
  font-size: 18px;
  font-family: 'Segoe UI', sans-serif;
  transform: translateX(20px);
  transition: transform 0.3s ease;
}

.game-container:hover .score {
  transform: translateX(0px);
}
</style>
