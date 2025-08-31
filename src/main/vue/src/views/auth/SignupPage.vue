<template>
  <div class="page-center-container">
    <div class="auth-container">
      <form @submit.prevent="signup">
        <input v-model="name" type="text" placeholder="Name" required/>
        <input v-model="email" type="email" placeholder="Email" required/>
        <AwesomeContainer icon="fa-solid fa-globe" class="awesome-container">
          <FuzzySelect
            class="awesome-fuzzy-select"
            :options="languages"
            v-model="language"
            :optionLabel="(lang) => lang.name"
            optionPlaceholder="Language"
            searchPlaceholder="Search..."
          />
        </AwesomeContainer>
        <!-- todo ability to see the password -->
        <SecretInput v-model="password" placeholder="Password" required/>
        <SecretInput v-model="confirmPassword" placeholder="Confirm Password" required/>
        <button type="submit">Sign Up</button>
        <p class="auth-alternative">
          Already signed up?
          <router-link to="/login">Login</router-link>
        </p>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import '@/assets/css/container.css'
import '@/assets/css/auth-container.css'
import FuzzySelect from '@/components/FuzzySelect.vue'
import AwesomeContainer from '@/components/AwesomeContainer.vue'
import SecretInput from '@/components/SecretInput.vue'
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from "@/stores/auth-store.ts"
import { routeNames } from "@/router/index.js"
import type { Language } from '@/model/language.ts'
import { useLanguageStore } from '@/stores/language-store.ts'
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
.awesome-container {
  --icon-color: var(--icon--fa-globe--color);
}

.awesome-fuzzy-select {
  border: var(--input--border);
  border-radius: var(--border-radius);
  background-color: var(--input-background-color);
  padding: clamp(0.5rem, 0.75rem, 1.25rem);
  font-size: clamp(1rem, 1vw, 1.1rem);
  --drop-down--border-color: var(--input_focus--border-color);
  --drop-down--hover--background-color: rgb(from var(--input_focus--border-color) r g b / 0.1);
  --drop-down_scrollbar--color: rgb(from var(--input_focus--border-color) r g b / 0.6);
  --drop-down_scrollbar_hover-collor: rgb(from var(--input_focus--border-color) r g b / 0.8);
  --drop-down--color: var(--input--color);
}

.awesome-fuzzy-select:focus-within {
  border: var(--input_focus--border);
  outline: none;
}
</style>
