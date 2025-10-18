<template>
  <div class="dice-area">
    <div v-for="n in 8" :key="n" class="die">🎲</div>
  </div>
</template>

<script>
export default { name: "DiceArea" };
</script>

<style scoped>
.dice-area {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  align-items: center;
}
.die {
  width: 40px;
  height: 40px;
  margin: 6px;
  border: 1px solid #000;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}
</style>
