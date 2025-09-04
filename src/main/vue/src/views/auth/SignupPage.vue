<template>
  <div class="page-center-container">
    <div
      class="auth-container transition--border-color"
      :class="{ 'auth-container--error': signupFailed }">
      <form @submit.prevent="signup">
        <input
          class="input transition--border-color"
          v-model="username"
          :class="{ 'input-error': usernameRequired }"
          :placeholder="usernameRequired ? 'Name is required' : 'Name'"/>
        <input
          class="input transition--border-color"
          v-model="userEmail"
          :class="{ 'input-error': userEmailInvalid }"
          :placeholder="userEmailRequired ? 'Email is required' : 'Email'"/>
        <p class="auth-container__p--error" v-for="error of $v.userEmail.$errors" :key="error.$uid">
          <span v-if="error.$validator === 'email'">This email seems to be lost in a cosmic dust cloud. Please check the format</span>
        </p>
        <AwesomeContainer icon="fa-solid fa-globe" class="awesome-container">
          <FuzzySelect
            class="awesome-fuzzy-select"
            :class="{ 'input-error': languageRequired }"
            :options="languages"
            v-model="language"
            :optionLabel="(lang) => lang.name"
            :optionPlaceholder="languageRequired ? 'Language is required' : 'Language'"
            searchPlaceholder="Search..."
          />
        </AwesomeContainer>
        <SecretInput
          v-model="userPassword"
          :class="{ 'input-error': userPasswordInvalid }"
          :placeholder="userPasswordRequired ? 'Password is required' : 'Password'"/>
        <p class="auth-container__p--error" v-for="error of $v.userPassword.$errors" :key="error.$uid">
          <span v-if="error.$validator === 'minLength'">Your password must be stronger than a piece of space junk. Please use 6 or more characters</span>
        </p>
        <SecretInput
          v-model="confirmedPassword"
          :class="{ 'input-error': confirmPasswordInvalid }"
          :placeholder="confirmPasswordRequired ? 'Password confirmation is required' : 'Confirm Password'"/>
        <p class="auth-container__p--error" v-for="error of $v.confirmPassword.$errors" :key="error.$uid">
          <span v-if="error.$validator === 'passwordConfirmed'">The passwords do not align. Please try again</span>
        </p>
        <button type="submit" class="button button--sign-up">Sign Up</button>
        <p class="auth-container__p--link">
          Been abducted by us before?
          <router-link to="/login">Login</router-link>
        </p>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import FuzzySelect from '@/components/FuzzySelect.vue'
import AwesomeContainer from '@/components/AwesomeContainer.vue'
import SecretInput from '@/components/SecretInput.vue'
import { computed, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from "@/stores/auth-store.ts"
import { routeNames } from "@/router/index.js"
import type { Language } from '@/model/language.ts'
import { useLanguageStore } from '@/stores/language-store.ts'
import { storeToRefs } from 'pinia'
import { useVuelidate } from '@vuelidate/core'
import { required, email, minLength, helpers } from '@vuelidate/validators'
import { sendSignupRequest } from '@/api/auth-client.ts'

const router = useRouter()
const authStore = useAuthStore()
const languageStore = useLanguageStore()

const { languages } = storeToRefs(languageStore)

const username = ref('')
const userEmail = ref('')
const language = ref<Language>()
const userPassword = ref('')
const confirmedPassword = ref('')
const signupFailed = ref(false)

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

const usernameInvalid = computed(() => $v.value.username.$errors.length > 0)
const usernameRequired = computed(() =>
  $v.value.username.$errors.find(v => v.$validator === 'required') !== undefined
)
const userEmailInvalid = computed(() => $v.value.userEmail.$errors.length > 0)
const userEmailRequired = computed(() =>
  $v.value.userEmail.$errors.find(v => v.$validator === 'required') !== undefined
)
const languageInvalid = computed(() => $v.value.language.$errors.length > 0)
const languageRequired = computed(() =>
  $v.value.language.$errors.find(v => v.$validator === 'required') !== undefined
)
const userPasswordInvalid = computed(() => $v.value.userPassword.$errors.length > 0)
const userPasswordRequired = computed(() =>
  $v.value.userPassword.$errors.find(v => v.$validator === 'required') !== undefined
)
const confirmPasswordInvalid = computed(() => $v.value.confirmPassword.$errors.length > 0)
const confirmPasswordRequired = computed(() =>
  $v.value.confirmPassword.$errors.find(v => v.$validator === 'required') !== undefined
)

watch(username, () => {
  signupFailed.value = false
  if (usernameInvalid.value) {
    $v.value.username.$reset()
  }
})

watch(userEmail, () => {
  signupFailed.value = false
  if (userEmailInvalid.value) {
    $v.value.userEmail.$reset()
  }
})

watch(language, () => {
  signupFailed.value = false
  if (languageInvalid.value) {
    $v.value.language.$reset()
  }
})

watch(userPassword, () => {
  signupFailed.value = false
  if (usernameInvalid.value) {
    $v.value.userPassword.$reset()
  }
  if (confirmPasswordInvalid.value) {
    $v.value.confirmPassword.$reset()
  }
})

watch(confirmedPassword, () => {
  signupFailed.value = false
  if (confirmPasswordInvalid.value) {
    $v.value.confirmPassword.$reset()
  }
})

async function signup() {
  signupFailed.value = false
  $v.value.$touch()
  if ($v.value.$invalid) {
    console.log('Invalid form')
    return
  }

  await sendSignupRequest(
    username.value,
    userEmail.value,
    userPassword.value,
    language.value?.id
  ).then(async (response) => {
    console.log('Successfully signed up: ', authStore.user?.id)
    authStore.setUser(response.data.user)
    await router.push({ name: routeNames.flashcards })
  }).catch(error => {
    signupFailed.value = true
    // todo show a toast with the error
    console.error('Failed to sign up: ', error)
  })
}
</script>

<style scoped>
.awesome-container {
  --awesome-container__icon--color: var(--fa-icon--color--globe);
}

.awesome-fuzzy-select {
  border-style: solid;
  border-width: 2px;
  border-color: var(--input--border-color);
  border-radius: var(--border-radius);
  background-color: var(--input--bg-color);
  padding: clamp(0.5rem, 0.75rem, 1.25rem);
  font-size: clamp(1rem, 1vw, 1.1rem);
}

.awesome-fuzzy-select:hover,
.awesome-fuzzy-select:focus-within {
  border-color: var(--input--border-color--focus);
  outline: none;
}
</style>
