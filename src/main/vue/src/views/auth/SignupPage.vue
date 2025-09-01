<template>
  <div class="page-center-container">
    <div class="auth-container">
      <form @submit.prevent="signup">
        <input v-model="username" placeholder="Name"/>
        <p class="error-message" v-for="error of $v.username.$errors" :key="error.$uid">
          <span v-if="error.$validator === 'required'">Please enter your name to begin your journey</span>
        </p>
        <input v-model="userEmail" placeholder="Email"/>
        <p class="error-message" v-for="error of $v.userEmail.$errors" :key="error.$uid">
          <span v-if="error.$validator === 'required'">We'll send mission updates to this email</span>
          <span v-else-if="error.$validator === 'email'">This email seems to be lost in a cosmic dust cloud. Please check the format</span>
        </p>
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
        <p class="error-message" v-for="error of $v.language.$errors" :key="error.$uid">
          <span v-if="error.$validator === 'required'">What language do they speak on your homeworld?</span>
        </p>
        <SecretInput v-model="userPassword" placeholder="Password"/>
        <p class="error-message" v-for="error of $v.userPassword.$errors" :key="error.$uid">
          <span v-if="error.$validator === 'required'">Your account needs a password to prevent unauthorized access</span>
          <span v-else-if="error.$validator === 'minLength'">Your password must be stronger than a piece of space junk. Please use 6 or more characters</span>
        </p>
        <SecretInput v-model="confirmedPassword" placeholder="Confirm Password"/>
        <p class="error-message" v-for="error of $v.confirmPassword.$errors" :key="error.$uid">
          <span v-if="error.$validator === 'required'">Please type your password again to ensure it's correct</span>
          <span v-else-if="error.$validator === 'passwordConfirmed'">The passwords do not align. Please try again</span>
        </p>
        <button type="submit">Sign Up</button>
        <p class="auth-link">
          Been abducted by us before?
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
import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from "@/stores/auth-store.ts"
import { routeNames } from "@/router/index.js"
import type { Language } from '@/model/language.ts'
import { useLanguageStore } from '@/stores/language-store.ts'
import { storeToRefs } from 'pinia'
import { useVuelidate } from '@vuelidate/core'
import { required, email, minLength, helpers } from '@vuelidate/validators'

const router = useRouter()
const authStore = useAuthStore()
const languageStore = useLanguageStore()

const { languages } = storeToRefs(languageStore)

const username = ref('')
const userEmail = ref('')
const language = ref<Language>()
const userPassword = ref('')
const confirmedPassword = ref('')

const passwordConfirmed = helpers.withParams(
  { type: 'confirmed' },
  (value) => {
    if (!value) return true
    return value === userPassword.value
  }
)

const $v = useVuelidate({
  username: { required },
  userEmail: { required, email },
  language: { required },
  userPassword: { required, minLength: minLength(6) },
  confirmPassword: { required, passwordConfirmed },
}, {
  username: username,
  userEmail: userEmail,
  language: language,
  userPassword: userPassword,
  confirmPassword: confirmedPassword,
})

watch(username, () => {
  if ($v.value.username.$invalid) {
    $v.value.username.$reset()
  }
})

watch(userEmail, () => {
  if ($v.value.userEmail.$invalid) {
    $v.value.userEmail.$reset()
  }
})

watch(language, () => {
  if ($v.value.language.$invalid) {
    $v.value.language.$reset()
  }
})

watch(userPassword, () => {
  if ($v.value.userPassword.$invalid) {
    $v.value.userPassword.$reset()
  }
  if ($v.value.confirmPassword.$invalid) {
    $v.value.confirmPassword.$reset()
  }
})

watch(confirmedPassword, () => {
  if ($v.value.confirmPassword.$invalid) {
    $v.value.confirmPassword.$reset()
  }
})

async function signup() {
  $v.value.$touch()
  if ($v.value.$invalid) {
    console.log('Invalid form')
    return
  }

  try {
    await authStore.signup(username.value, userEmail.value, userPassword.value, language.value?.id)
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
