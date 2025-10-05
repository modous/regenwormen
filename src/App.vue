<script setup>
import houtBg from './assets/hout.jpg'

</script>

<template>
  <div class="app" :style="{ backgroundImage: `url(${houtBg})` }">
    <router-view />
  </div>
</template>

<style>
.app {
  height: 100vh;
  width: 100vw;
  margin: 0;
  padding: 0;
  background-size: cover;
  background-position: center;
}
</style>
