<template>
  <div>
    <h2>Log</h2>
    <ul>
      <li v-for="(entry,i) in log" :key="i">{{ entry }}</li>
    </ul>
  </div>
</template>

<script setup>
defineProps({ log: Array })
</script>
