<template>
  <div class="game-ui">
    <!-- Bovenste rij tegels -->
    <TopRow />

    <div class="board">
      <!-- Linksonder worpen -->
      <Throws />

      <!-- Midden dobbelstenen -->
      <DiceArea />

      <!-- Rechtsonder verzamelde tegels -->
      <CollectedTiles />
    </div>
  </div>
</template>

<script>
import TopRowComponent from "./TopRowComponent.vue";
import ThrowsComponent from "./ThrowsComponent.vue";
import DiceAreaComponent from "./DiceAreaComponent.vue";
import CollectedTilesComponent from "./CollectedTilesComponent.vue";

export default {
  name: "GameUI",
  components: { TopRow: TopRowComponent, Throws: ThrowsComponent, DiceArea: DiceAreaComponent, CollectedTiles: CollectedTilesComponent }
};
</script>

<style scoped>
.game-ui {
  display: flex;
  flex-direction: column;
  height: 100vh;
}
.board {
  display: grid;
  grid-template-columns: 1fr 2fr 1fr;
  grid-template-rows: 1fr;
  flex: 1;
}
</style>
