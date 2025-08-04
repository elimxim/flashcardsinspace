<template>
  <div class="auth-container">
    <form @submit.prevent="signup">
      <input v-model="name" type="text" placeholder="Name" required/>
      <input v-model="email" type="email" placeholder="Email" required/>
      <div class="auth-select-container">
        <font-awesome-icon icon="fa-solid fa-globe" class="auth-select-container-icon"/>
        <select v-model="language">
          <option v-for="i in languages" :key="i.id" :value="i">
            {{ i.name }}
          </option>
        </select>
      </div>
      <!-- todo ability to see the password -->
      <input v-model="password" type="password" placeholder="Password" required/>
      <input v-model="confirmPassword" type="password" placeholder="Confirm Password" required/>
      <button type="submit">Sign Up</button>
    </form>
    <p class="auth-alternative">
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
import { useAuthStore } from "@/stores/auth.ts"
import { routeNames } from "@/router/index.js"
import { type User } from '@/model/user.ts'
import type { Language } from '@/model/language.ts'
import { useLanguageStore } from '@/stores/language.ts'
import { storeToRefs } from 'pinia'

const router = useRouter()
const authStore = useAuthStore()
const languageStore = useLanguageStore()

const { languages } = storeToRefs(languageStore)

// todo validate inputs and show errors
const name = ref('')
const email = ref('')
const language = ref(null as Language | null)
const password = ref('')
// todo validate if the inputs have the same email
const confirmPassword = ref('')

// todo password strength

async function signup() {
  if (language.value === null) {
    throw new Error('Language is not set')
  }

  try {
    await authStore.signup(name.value, email.value, password.value, language.value?.id)
    console.log('Successfully signed up: ', authStore.user?.id)
    await router.push({ name: routeNames.flashcards })
  } catch (error) {
    // todo show the error to the user
    console.error('Failed to sign up: ', error)
  }
}
</script>

<style scoped>
</style>
