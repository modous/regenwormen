<template>
  <div class="game">
    <h1>Regenwormen</h1>

    <p>Current player: {{ players[currentPlayer].name }}</p>

    <DiceRoll
        :rolled="rolledDice"
        @roll="rollDice"
        @choose="chooseValue"
    />

    <DiceCollected :dice="collectedDice" />

    <TilesTable
        :tiles="tilesOnTable"
        :total="diceTotal"
        :hasWorm="collectedDice.includes(6)"
        @takeTile="takeTile"
    />

    <TilesCollected :tiles="players[0].tiles" />

    <TilesOtherPlayer :players="players.slice(1)" />

    <button @click="endTurn">Stop Turn</button>

    <GameLog :log="log" />
  </div>
</template>

<script setup>
import { ref, computed } from "vue"
import DiceRoll from "./DiceRoll.vue"
import DiceCollected from "./DiceCollected.vue"
import TilesCollected from "./TilesCollected.vue"
import TilesOtherPlayer from "./TilesOtherPlayer.vue"
import TilesTable from "./TilesTable.vue"
import GameLog from "./GameLog.vue"

const remainingDice = ref(8)
const rolledDice = ref([])
const collectedDice = ref([])

const tilesOnTable = ref(Array.from({length:16},(_,i)=>21+i))
const players = ref([
  { id: 0, name: "You", tiles: [] },
  { id: 1, name: "P1", tiles: [26] },
  { id: 2, name: "P2", tiles: [] }
])
const currentPlayer = ref(0)
const log = ref([])

const diceTotal = computed(() =>
    collectedDice.value.reduce((sum,d)=> sum + (d===6 ? 5 : d), 0)
)

function rollDice() {
  if (remainingDice.value <= 0) {
    log.value.push("No dice left")
    return
  }
  rolledDice.value = []
  for (let i = 0; i < remainingDice.value; i++) {
    rolledDice.value.push(Math.ceil(Math.random() * 6))
  }
  log.value.push(players.value[currentPlayer.value].name + " rolled: " + rolledDice.value.join(", "))
  checkBusted()
}

function chooseValue(value) {
  if (collectedDice.value.includes(value)) {
    log.value.push("Already taken value " + value)
    return
  }
  const chosen = rolledDice.value.filter(d => d === value)
  if (chosen.length === 0) {
    log.value.push("No dice of value " + value)
    return
  }
  collectedDice.value.push(...chosen)
  remainingDice.value -= chosen.length
  log.value.push(players.value[currentPlayer.value].name + " chose " + chosen.length + "×" + (value===6?"Worm":value))
  rolledDice.value = []
  if (remainingDice.value === 0) {
    log.value.push("No dice left, must end turn")
    endTurn()
  }
}

function takeTile(tile) {
  const me = players.value[currentPlayer.value]

  if (!collectedDice.value.includes(6)) {
    log.value.push("Need a worm to take a tile")
    return
  }
  if (diceTotal.value < tile) {
    log.value.push("Total too low for tile " + tile)
    return
  }

  // check steal from others
  for (const p of players.value) {
    if (p.id !== me.id && p.tiles[p.tiles.length-1] === tile) {
      me.tiles.push(p.tiles.pop())
      log.value.push(me.name + " stole tile " + tile + " from " + p.name)
      resetTurn()
      nextPlayer()
      return
    }
  }

  // take from table
  if (tilesOnTable.value.includes(tile)) {
    tilesOnTable.value = tilesOnTable.value.filter(t => t !== tile)
    me.tiles.push(tile)
    log.value.push(me.name + " took tile " + tile + " from table")
    resetTurn()
    nextPlayer()
    return
  }

  log.value.push("Cannot take tile " + tile)
}

function endTurn() {
  const me = players.value[currentPlayer.value]
  if (!collectedDice.value.includes(6)) {
    busted("No worm collected")
    return
  }
  const candidate = tilesOnTable.value.filter(t => t <= diceTotal.value)
  if (candidate.length === 0) {
    busted("No tile available for total " + diceTotal.value)
    return
  }
  log.value.push(me.name + " choose a tile by clicking it")
}

function busted(reason) {
  const me = players.value[currentPlayer.value]
  log.value.push(me.name + " busted: " + reason)

  // return top tile if any
  if (me.tiles.length > 0) {
    const lost = me.tiles.pop()
    tilesOnTable.value.push(lost)
    tilesOnTable.value.sort((a,b)=>a-b)
    log.value.push(me.name + " returned tile " + lost)
  }

  // remove highest from table
  if (tilesOnTable.value.length > 0) {
    const removed = Math.max(...tilesOnTable.value)
    tilesOnTable.value = tilesOnTable.value.filter(t => t !== removed)
    log.value.push("Removed top tile " + removed)
  }

  resetTurn()
  nextPlayer()
}

function resetTurn() {
  remainingDice.value = 8
  rolledDice.value = []
  collectedDice.value = []
}

function nextPlayer() {
  currentPlayer.value = (currentPlayer.value + 1) % players.value.length
  log.value.push("Next turn: " + players.value[currentPlayer.value].name)
}
</script>
