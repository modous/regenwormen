<script setup>
import wormImage from '../assets/regenwormSpel.png'
import { useUserStore } from '@/stores/user.js'
import { useRouter } from 'vue-router'

const userStore = useUserStore()
const router = useRouter()

function goToProfile() {
  router.push('/profile')
}
</script>

<template>
  <section class="start-screen">
    <!-- 👤 Profile (rechtsboven) -->
    <div
        v-if="userStore.isAuthenticated"
        class="profile-header"
        @click="goToProfile"
        title="View Profile"
    >
      <img
          v-if="userStore.user?.profilePictureUrl"
          :src="userStore.user.profilePictureUrl"
          alt="Profile Picture"
          class="profile-img"
      />
      <img
          v-else
          src="../assets/default-avatar.png"
          alt="Default Avatar"
          class="profile-img"
      />
      <span class="profile-name">{{ userStore.user?.username || 'Player' }}</span>
    </div>

    <!-- Title -->
    <a class="title-link" href="/">
      <h1 class="title">Regenwormen</h1>
    </a>

    <!-- Main button -->
    <router-link
        v-if="userStore.isAuthenticated"
        to="/lobbies"
        class="start-btn"
    >
      Start Game
    </router-link>

    <!-- Navigation -->
    <nav class="nav-links">
      <router-link to="/how-to-play" class="nav-link">How to Play</router-link>

      <router-link
          v-if="userStore.isAuthenticated"
          to="/match-history"
          class="nav-link"
      >
        Match history
      </router-link>

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
  justify-content: flex-start;
  min-height: 100vh;
  padding: 4rem 2rem 2rem;
  background: #faf9fc;
  position: relative;
}

/* 🧍 Profile top-right */
.profile-header {
  position: absolute;
  top: 1rem;
  right: 1.5rem;
  display: flex;
  align-items: center;
  gap: 0.6rem;
  background: rgba(255, 255, 255, 0.85);
  padding: 0.4rem 0.8rem;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
  cursor: pointer;
  transition: 0.2s;
}
.profile-header:hover {
  background: rgba(255, 255, 255, 1);
  transform: translateY(-2px);
}
.profile-img {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #b10c96;
}
.profile-name {
  font-weight: 600;
  color: #333;
  font-size: 1.05rem;
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
  margin: 0 auto 3rem auto;
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
</style>
