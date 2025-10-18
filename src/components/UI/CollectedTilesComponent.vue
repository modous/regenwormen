<template>
  <div class="collected">
    <h3>Mijn Tegels</h3>
    <div class="tiles">
      <div v-for="n in 5" :key="n" class="tile">T{{ n }}</div>
    </div>
  </div>
</template>

<script>
export default { name: "CollectedTiles" };
</script>

<style scoped>
.collected {
  padding: 8px;
}
.tiles {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}
.tile {
  width: 40px;
  height: 60px;
  border: 1px solid #000;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
