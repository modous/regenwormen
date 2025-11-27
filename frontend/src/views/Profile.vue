<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user.js'
import { useRouter } from 'vue-router'

const router = useRouter()
const userStore = useUserStore()
const user = ref(userStore.user ? { ...userStore.user } : null)
const location = ref(user.value?.location || '')
const newPassword = ref('')
const confirmPassword = ref('')
const message = ref('')
const uploading = ref(false)

async function loadProfile() {
  if (!user.value?.id) return
  try {
    const res = await fetch(`http://localhost:8080/api/users/${user.value.id}`)
    if (!res.ok) throw new Error('Failed to load profile')
    const data = await res.json()
    user.value = data
    userStore.login(data)
  } catch (err) {
    console.error('Failed to load profile:', err)
  }
}

async function uploadPhoto(e) {
  const file = e.target.files[0]
  if (!file || !user.value?.id) return
  const formData = new FormData()
  formData.append('file', file)

  uploading.value = true
  try {
    const res = await fetch(`http://localhost:8080/api/users/${user.value.id}/photo`, {
      method: 'POST',
      body: formData
    })

    if (res.ok) {
      const photoUrl = await res.text()
      user.value.profilePictureUrl = photoUrl
      userStore.login(user.value)
      message.value = '✅ Profile photo uploaded successfully!'
    } else {
      message.value = '❌ Upload failed.'
    }
  } catch (err) {
    console.error('Upload error:', err)
    message.value = '❌ Error while uploading photo.'
  } finally {
    uploading.value = false
  }
}

async function getLocation() {
  if (!navigator.geolocation) {
    message.value = '❌ Geolocation is not supported by your browser.'
    return
  }

  navigator.geolocation.getCurrentPosition(
      async (pos) => {
        const { latitude, longitude } = pos.coords

        try {
          const res = await fetch(
              `https://nominatim.openstreetmap.org/reverse?lat=${latitude}&lon=${longitude}&format=json`
          )
          const data = await res.json()

          const city =
              data.address.city ||
              data.address.town ||
              data.address.village ||
              data.address.state ||
              'Unknown location'

          const country = data.address.country || ''
          location.value = `${city}${country ? ', ' + country : ''}`

          await saveLocation()
          message.value = `✅ Location saved: ${location.value}`
        } catch (err) {
          console.error('Reverse geocoding failed:', err)
          location.value = `${latitude.toFixed(4)}, ${longitude.toFixed(4)}`
          await saveLocation()
          message.value = '✅ Coordinates saved (city lookup failed).'
        }
      },
      (err) => {
        console.error(err)
        message.value = '❌ Could not retrieve location.'
      }
  )
}

async function saveLocation() {
  if (!user.value?.id) return
  try {
    await fetch(`http://localhost:8080/api/users/${user.value.id}/location`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ location: location.value })
    })
    user.value.location = location.value
    userStore.login(user.value)
    message.value = '✅ Location saved successfully.'
  } catch (err) {
    console.error(err)
    message.value = '❌ Failed to save location.'
  }
}

async function changePassword() {
  if (!user.value?.id) return
  if (newPassword.value !== confirmPassword.value) {
    message.value = '❌ Passwords do not match.'
    return
  }

  const res = await fetch(`http://localhost:8080/api/users/${user.value.id}/password`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ password: newPassword.value })
  })

  message.value = res.ok
      ? '✅ Password updated successfully.'
      : '❌ Failed to update password.'
  newPassword.value = ''
  confirmPassword.value = ''
}

function leaveProfile() {
  router.push('/')
}

onMounted(loadProfile)
</script>

<template>
  <section class="profile">
    <h1>{{ user?.username }}</h1>

    <div class="profile-card">
      <div class="photo-section">
        <img
            v-if="user?.profilePictureUrl"
            :src="user.profilePictureUrl"
            alt="Profile photo"
            class="profile-photo"
        />
        <img
            v-else
            src="../assets/default-avatar.png"
            alt="Default avatar"
            class="profile-photo"
        />

        <label class="upload-btn" :class="{ disabled: uploading }">
          {{ uploading ? "⏳ Uploading..." : "📸 Change image" }}
          <input type="file" accept="image/*" @change="uploadPhoto" hidden :disabled="uploading" />
        </label>
      </div>

      <div class="info">
        <p><strong>Email:</strong> {{ user?.email }}</p>

        <div class="location">
          <label><strong>Location:</strong></label>
          <input
              v-model="location"
              placeholder="No location set"
              class="input-field"
          />
          <div class="btn-row">
            <button @click="getLocation" class="btn blue">📍 Use GPS</button>
            <button @click="saveLocation" class="btn purple">💾 Save</button>
          </div>
        </div>
      </div>

      <div class="password-change">
        <h3>🔒 Change Password</h3>
        <input
            type="password"
            v-model="newPassword"
            placeholder="New password"
            class="input-field"
        />
        <input
            type="password"
            v-model="confirmPassword"
            placeholder="Confirm password"
            class="input-field"
        />
        <button @click="changePassword" class="btn purple">Update Password</button>
      </div>
    </div>

    <p v-if="message" class="msg">{{ message }}</p>

    <!-- ⬇️ Leave button below profile card -->
    <button class="back-btn" @click="leaveProfile">Leave</button>
  </section>
</template>

<style scoped>
.profile {
  padding-top: 20px;
  max-width: 650px;
  margin: auto;
  text-align: center;
  color: #333;
}

h1 {
  color: #b10c96;
  margin-bottom: 1.5rem;
}

.profile-card {
  background: #fff;
  border-radius: 18px;
  padding: 2rem;
  box-shadow: 0 4px 20px rgba(0,0,0,0.1);
}

.photo-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 1.5rem;
}

.profile-photo {
  width: 130px;
  height: 130px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #b10c96;
  margin-bottom: 0.75rem;
}

.upload-btn {
  background: #b10c96;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  transition: 0.3s;
}
.upload-btn:hover {
  background: #770494;
}
.upload-btn.disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.info {
  margin-top: 1rem;
}
.info p {
  margin: 0.3rem 0;
  font-size: 1.1rem;
}

.location {
  margin-top: 1.5rem;
}
.input-field {
  width: 80%;
  max-width: 300px;
  padding: 0.5rem;
  border: 1px solid #ccc;
  border-radius: 8px;
  margin-top: 0.4rem;
}
.btn-row {
  display: flex;
  justify-content: center;
  gap: 0.6rem;
  margin-top: 0.8rem;
}

.password-change {
  margin-top: 0.5rem;
  border-top: 1px solid #eee;
  padding-top: 1.5rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.8rem;
}
.password-change h3 {
  color: #770494;

}
.btn {
  border: none;
  border-radius: 8px;
  padding: 0.5rem 1.4rem;
  color: white;
  font-size: 1rem;
  cursor: pointer;
  transition: 0.3s;
}
.btn.blue {
  background: #0077cc;
}
.btn.blue:hover {
  background: #005fa3;
}
.btn.purple {
  background: #b10c96;
}
.btn.purple:hover {
  background: #770494;
}
.msg {
  margin-top: 1.5rem;
  font-weight: bold;
  color: #333;
}

.back-btn {
  margin-top: 2rem;
  background: #b10c96;
  color: white;
  border: none;
  border-radius: 8px;
  padding: 0.5rem 1.5rem;
  cursor: pointer;
  transition: background 0.2s;
}
.back-btn:hover {
  background: #87067c;
}
</style>
