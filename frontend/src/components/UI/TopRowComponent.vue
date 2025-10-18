<template>
  <div class="top-row">
    <div v-for="n in 15" :key="n" class="tile">Tegel {{ n }}</div>
  </div>
</template>

<script>
export default { name: "TopRow" };
</script>

<style scoped>
.top-row {
  display: flex;
  justify-content: center;
  gap: 4px;
  padding: 8px;
}
.tile {
  width: 40px;
  height: 60px;
  border: 1px solid #000;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
}
</style>
