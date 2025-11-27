<template>
  <section class="login-page">
    <div class="login-container">
      <h1>Login</h1>

      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label for="identifier">Email or Username</label>
          <input
              id="identifier"
              type="text"
              v-model="identifier"
              placeholder="Type your email or username"
              required
          />
        </div>

        <div class="form-group">
          <label for="password">Password</label>
          <input
              id="password"
              type="password"
              v-model="password"
              placeholder="Type your password"
              required
          />
        </div>

        <button type="submit" class="login-btn">Login</button>
      </form>

      <p class="register-link">
        Don't have an account?
        <router-link to="/register">Register</router-link>
      </p>
    </div>
  </section>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user.js'

const identifier = ref('')
const password = ref('')
const router = useRouter()
const userStore = useUserStore()

async function handleLogin() {
  try {
    const response = await fetch('http://localhost:8080/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        identifier: identifier.value,
        password: password.value,
      }),
    });

    const data = await response.json();

    if (!response.ok) {
      alert(data.error || 'Invalid credentials');
      return;
    }

    // ✅ Ensure the user object has an ID (needed for Profile.vue)
    if (!data.id) {
      console.error('User ID missing from response:', data);
      alert('Login failed: invalid server response.');
      return;
    }

    // ✅ Save the full user object to Pinia + localStorage
    userStore.login(data);
    localStorage.setItem('user', JSON.stringify(data));

    alert(`Welcome back, ${data.username}!`);
    router.push('/');
  } catch (err) {
    console.error('Login error:', err);
    alert('⚠️ Could not reach server.');
  }
}
</script>

<style scoped>
.login-page {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
}

.login-container {
  background: white;
  padding: 2rem 3rem;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  width: 100%;
  max-width: 400px;
  text-align: center;
}

.login-container h1 {
  margin-bottom: 1.5rem;
  color: black;
}

.form-group {
  display: flex;
  flex-direction: column;
  margin-bottom: 1rem;
  text-align: left;
  color: black;
}

.form-group label {
  margin-bottom: 0.5rem;
  font-weight: bold;
}

.form-group input {
  padding: 0.5rem 1rem;
  border-radius: 6px;
  border: 1px solid #ccc;
  font-size: 1rem;
}

.login-btn {
  width: 100%;
  padding: 0.75rem 1rem;
  margin-top: 1rem;
  border: none;
  border-radius: 8px;
  background-color: #b10c96;
  color: white;
  font-size: 1.1rem;
  cursor: pointer;
  transition: 0.3s;
}

.login-btn:hover {
  background-color: #770494;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
}

.register-link {
  margin-top: 1rem;
  font-size: 0.9rem;
  color: #000;
}
</style>
