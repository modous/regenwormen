<template>
  <router-view></router-view>
    <MusicButton
        :playlist="playlist"
    />
</template>

<script setup>
import MusicButton from '@/components/MusicButton.vue'
import tune1 from '@/assets/audio/tune1.mp3'
import tune2 from '@/assets/audio/tune2.mp3'
import tune3 from '@/assets/audio/tune3.mp3'

const playlist = [
  { src: tune1, title: 'Tune 1' },
  { src: tune2, title: 'Tune 2' },
  { src: tune3, title: 'Tune 3' },
]
</script>
