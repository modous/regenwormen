<template>
  <section class="register-page">
    <div class="register-container">
      <h1>Register</h1>

      <form @submit.prevent="handleRegister">
        <div class="form-group">
          <label for="email">Email</label>
          <input 
            id="email" 
            type="email" 
            v-model="email" 
            placeholder="Type your email" 
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

        <div class="form-group">
          <label for="confirmPassword">Confirm Password</label>
          <input 
            id="confirmPassword" 
            type="password" 
            v-model="confirmPassword" 
            placeholder="Confirm your password" 
            required 
          />
        </div>

        <button type="submit" class="register-btn">Register</button>
      </form>

      <p class="login-link">
        Already have an account?
        <router-link to="/login">Login</router-link>
      </p>
    </div>
  </section>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const email = ref('')
const password = ref('')
const confirmPassword = ref('')
const router = useRouter()

async function handleRegister() {
  if (password.value !== confirmPassword.value) {
    alert("Passwords do not match!");
    return;
  }

  try {
    const response = await fetch('http://localhost:8080/api/auth/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        email: email.value,
        password: password.value
      }),
    });

    const data = await response.json();

    if (!response.ok) {
      alert(data.error || "Registration failed.");
      return;
    }

    alert("Registration successful!");
    router.push('/login');
  } catch (err) {
    alert("Could not reach server.");
    console.error(err);
  }
}

</script>

<style scoped>
.register-page {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
}

.register-container {
  background: white;
  padding: 2rem 3rem;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
  width: 100%;
  max-width: 400px;
  text-align: center;
}

.register-container h1 {
  margin-bottom: 1.5rem;
  color: #000;
}

.form-group {
  display: flex;
  flex-direction: column;
  margin-bottom: 1rem;
  text-align: left;
}

.form-group label {
  margin-bottom: 0.5rem;
  font-weight: bold;
  color: #000;
}

.form-group input {
  padding: 0.5rem 1rem;
  border-radius: 6px;
  border: 1px solid #ccc;
  font-size: 1rem;
}

.form-group input::placeholder {
  color: #aaa;
  font-style: italic;
}

.register-btn {
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

.register-btn:hover {
  background-color: #770494;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0,0,0,0.2);
}

.login-link {
  margin-top: 1rem;
  font-size: 0.9rem;
  color: black;
}
</style>
