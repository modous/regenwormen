<template>
  <div class="throws">
    <div v-for="n in 3" :key="n" class="throw-row">
      Worp {{ n }}: <span class="score">0</span>
    </div>
  </div>
</template>

<script>
export default { name: "Throws" };
</script>

<style scoped>
.throws {
  display: flex;
  flex-direction: column;
  padding: 8px;
}
.throw-row {
  margin: 4px 0;
}
.score {
  font-weight: bold;
}
</style>
