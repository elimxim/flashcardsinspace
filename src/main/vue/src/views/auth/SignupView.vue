<template>
  <div class="auth-container">
    <form @submit.prevent="signup">
      <input v-model="name" type="text" placeholder="Name" required/>
      <input v-model="email" type="email" placeholder="Email" required/>
      <!-- todo ability to see the password -->
      <input v-model="password" type="password" placeholder="Password" required/>
      <input v-model="confirmPassword" type="password" placeholder="Confirm Password" required/>
      <button type="submit">Sign Up</button>
    </form>
    <p class="auth-option">
      Already signed up?
      <router-link to="/login">Login</router-link>
    </p>
  </div>
</template>

<script setup lang="ts">
import '@/assets/auth.css'
import apiClient from '@/api/api-client.ts'
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from "@/stores/auth-state.js"
import { routeNames } from "@/router/index.js"
import { type User } from '@/models/user.ts'

const router = useRouter()
const authStore = useAuthStore()

// todo validate inputs and show errors
const name = ref('')
const email = ref('')
const password = ref('')
// todo validate if the inputs have the same email
const confirmPassword = ref('')
// todo password strength

async function signup() {
  try {
    const response = await apiClient.post('/auth/signup', {
      email: email.value,
      secret: password.value,
      name: name.value,
    })

    const user: User = {
      id: response.data.id,
      email: response.data.email,
      name: response.data.name,
      registeredAt: response.data.registeredAt,
    }

    authStore.setUser(user)

    console.log('Successfully signed up: ', response.data.id)
    await router.push({ name: routeNames.flashcards })
  } catch (error) {
    // todo show the error to the user
    console.error('Failed to sign up: ', error)
  }
}
</script>

<style scoped>
</style>
