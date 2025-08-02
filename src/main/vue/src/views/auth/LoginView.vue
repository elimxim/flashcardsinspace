<template>
  <div class="auth-container">
    <form @submit.prevent="handleLogin">
      <input v-model="email" type="email" placeholder="Email" required/>
      <!-- todo ability to see the password -->
      <input v-model="password" type="password" placeholder="Password" required/>
      <button type="submit">Log In</button>
    </form>
  </div>
</template>

<script setup lang="ts">
import '@/assets/auth.css'
import apiClient from '@/api/api-client.ts'
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from "@/stores/auth-state.js"
import { routeNames } from "@/router/index.js"

const router = useRouter()
const authStore = useAuthStore()

// todo validate inputs and show errors
const email = ref('')
const password = ref('')

async function handleLogin() {
  try {
    const response = await apiClient.post('/auth/login', {
      email: email.value,
      secret: password.value,
    })

    authStore.setUser({
      id: response.data.id,
      email: response.data.email,
      name: response.data.name,
      registeredAt: response.data.registeredAt,
    })

    console.log('Successfully logged in: ', response.data.id)
    await router.push({ name: routeNames.flashcards })
  } catch (error) {
    // todo show the error to the user
    console.log('Failed to log in: ', error)
  }
}
</script>

<style scoped>
</style>
