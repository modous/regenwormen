<script setup>
import wormImage from '../assets/regenwormSpel.png'
import { useUserStore } from '@/stores/user.js'

const userStore = useUserStore()
console.log('User store test:', userStore)
</script>

<template>
  <section class="start-screen">
    <a class="title-link" href="/"><h1 class="title">Regenwormen</h1></a>

    <router-link
        v-if="userStore.isAuthenticated"
        to="/lobbies"
        class="start-btn"
    >
      Start Game
    </router-link>

    <nav class="nav-links">
      <router-link to="/how-to-play" class="nav-link">How to Play</router-link>
      <router-link to="/credits" class="nav-link">Credits</router-link>

      <router-link
          v-if="!userStore.isAuthenticated"
          to="/login"
          class="nav-link"
      >
        Login
      </router-link>

      <router-link
          v-if="!userStore.isAuthenticated"
          to="/register"
          class="nav-link"
      >
        Register
      </router-link>

      <a
          v-if="userStore.isAuthenticated"
          href="#"
          class="nav-link"
          @click.prevent="userStore.logout"
      >
        Logout
      </a>

      <router-link to="/exit" class="nav-link">Exit</router-link>
    </nav>
  </section>
</template>

<style scoped>
.start-screen {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;   /* move content a bit higher */
  min-height: 100vh;
  padding: 4rem 2rem 2rem;       /* extra top padding */
  background: #faf9fc;
  position: relative;
}

/* Title styling */
.title-link {
  text-decoration: none;
}
.title {
  font-size: 4rem;
  color: #b10c96;
  margin-bottom: 3rem;
  text-shadow: 2px 2px 4px rgba(0,0,0,0.1);
  transition: transform 0.3s ease, text-shadow 0.3s ease;
}
.title:hover {
  transform: translateY(-3px);
  text-shadow: 2px 4px 6px rgba(0,0,0,0.2);
}

/* Start button */
.start-btn {
  display: block;
  margin: 0 auto 3rem auto;      /* perfectly centered horizontally + spaced below title */
  padding: 0.75rem 2.5rem;
  font-size: 1.4rem;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  background-color: #b10c96;
  color: white;
  text-decoration: none;
  transition: all 0.3s;
}
.start-btn:hover {
  background-color: #770494;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
  transform: translateY(-3px);
}

/* Navigation links */
.nav-links {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.75rem;
}
.nav-link {
  font-size: 1.3rem;
  color: #3b2c20;
  font-weight: 600;
  text-decoration: none;
  transition: all 0.3s;
}
.nav-link:hover {
  color: white;
  background-color: #770494;
  padding: 0.3rem 1rem;
  border-radius: 6px;
}

/* Optional image placement */
.worm-img {
  margin-top: 2rem;
  width: 250px;
  max-width: 80%;
  height: auto;
}

/* Responsive fallback */
@media (min-width: 1200px) {
  .start-screen {
    flex-direction: column;
    align-items: center;
    justify-content: flex-start;
  }
}
</style>
