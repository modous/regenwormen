<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

function goHome() {
  router.push('/')
}
</script>

<template>
  <section class="credits">
    <h1>🎮 Credits</h1>

    <div class="credits-card">
      <h2>Developed By</h2>
      <p><strong>Mohamed Taissate</strong></p>
      <p><strong>Bas Boersma</strong></p>
      <p><strong>Timothy Blom</strong></p>
      <p>Frontend Developer & Designer</p>

      <h2>Technology Stack</h2>
      <ul>
        <li>Vue 3 + Vite</li>
        <li>JavaScript (Composition API)</li>
        <li>Spring Boot REST API (Java Backend)</li>
        <li>H2 / MySQL Database</li>
        <li>Fetch API + Async/Await</li>
        <li>Pinia Store for State Management</li>
      </ul>

      <h2>Special Thanks</h2>
      <ul>
        <li>HvA – Software Engineering Program</li>
        <li>O'Reilly Course Material</li>
        <li>Classmates & Teaching Staff</li>
        <li>OpenStreetMap for Geolocation API</li>
      </ul>

      <h2>Project</h2>
      <p><strong>"The Auctioneer Game"</strong></p>
      <p>Frontend connected to a REST backend using modern web technologies.</p>
    </div>

    <button class="back-btn" @click="goHome">🏠 Back to Home</button>
  </section>
</template>

<style scoped>
.credits {
  display: flex;
  flex-direction: column;
  justify-content: center; /* centers vertically */
  align-items: center; /* centers horizontally */
  min-height: 100vh; /* fills the full screen height */
  padding: 2rem 1rem;
  background: #faf9fc; /* same as your lobby background */
  color: #333;
  text-align: center;
}

h1 {
  color: #b10c96;
  margin-bottom: 1.5rem;
  font-size: 2rem;
}

.credits-card {
  background: #fff;
  border-radius: 18px;
  padding: 2rem;
  box-shadow: 0 4px 20px rgba(0,0,0,0.1);
  text-align: left;
  width: 100%;
  max-width: 700px;
}

.credits-card h2 {
  color: #770494;
  margin-top: 1.5rem;
  margin-bottom: 0.5rem;
  font-size: 1.2rem;
}

.credits-card p {
  margin: 0.3rem 0;
  font-size: 1rem;
}

.credits-card ul {
  list-style: none;
  padding-left: 0;
  margin: 0.4rem 0 0.8rem 0;
}

.credits-card li {
  margin: 0.25rem 0;
  padding-left: 0.5rem;
  position: relative;
}

.credits-card li::before {
  content: "•";
  color: #b10c96;
  position: absolute;
  left: -0.5rem;
}

.back-btn {
  margin-top: 2rem;
  background: #b10c96;
  color: white;
  border: none;
  border-radius: 8px;
  padding: 0.6rem 1.5rem;
  cursor: pointer;
  font-size: 1rem;
  transition: background 0.2s;
}

.back-btn:hover {
  background: #87067c;
}
</style>
