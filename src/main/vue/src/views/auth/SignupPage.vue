<template>
  <div class="page-center-container">
    <div
      class="auth-container transition--border-color"
      :class="{ 'auth-container--error': signupFailed }"
    >
      <form @submit.prevent="signup" class="auth-container__form">
        <input
          class="input auth-container__form__input transition--border-color"
          v-model="username"
          :class="{ 'input--error': usernameRequired }"
          name="name"
          autocomplete="nickname"
          :placeholder="usernameRequired ? 'Name is required' : 'Name'"
        />
        <span v-if="usernameRegex" class="auth-container__form__error">
          Please use only letters, numbers, dashes, and underscores
        </span>
        <span v-else-if="usernameMaxLength" class="auth-container__form__error">
            This username is expanding faster than the universe! Please keep it under 64 characters
        </span>
        <input
          class="input auth-container__form__input transition--border-color"
          v-model="userEmail"
          type="email"
          name="username"
          autocomplete="username"
          :class="{ 'input--error': userEmailInvalid }"
          :placeholder="userEmailRequired ? 'Email is required' : 'Email'"
        />
        <span v-if="userEmailPattern" class="auth-container__form__error">
          This email seems to be lost in a cosmic dust cloud. Please check the format
        </span>
        <AwesomeContainer
          icon="fa-solid fa-globe"
          class="awesome-container"
        >
          <FuzzySelect
            class="fuzzy-select"
            :class="{ 'input--error fuzzy-select--error': languageRequired }"
            :options="languages"
            v-model="language"
            :optionLabel="(lang) => lang.name"
            :optionPlaceholder="languageRequired ? 'Language is required' : 'Language'"
            searchPlaceholder="Search..."
          />
        </AwesomeContainer>
        <SecretInput
          v-model="userPassword"
          class="auth-container__form__input"
          :class="{ 'input--error': userPasswordInvalid }"
          name="new-password"
          autocomplete="new-password"
          :placeholder="userPasswordRequired ? 'Password is required' : 'Password'"
        />
        <span v-if="userPasswordMinLength" class="auth-container__form__error">
          Your password must be stronger than a piece of space junk. Please use 6 or more characters
        </span>
        <span v-else-if="userPasswordMaxLength" class="auth-container__form__error">
          This secret is expanding faster than the universe! Please keep it under 64 characters
        </span>
        <SecretInput
          v-model="confirmedPassword"
          class="auth-container__form__input"
          :class="{ 'input--error': confirmPasswordInvalid }"
          name="confirm-password"
          autocomplete="new-password"
          :placeholder="confirmPasswordRequired ? 'Password confirmation is required' : 'Confirm Password'"
        />
        <span v-if="confirmPasswordConfirmed" class="auth-container__form__error">
          The passwords do not align. Please try again
        </span>
        <button
          ref="signUpButton"
          type="submit"
          class="button button--sign-up auth-container__form__button transition--bg-color"
          :disabled="formInvalid"
          :class="{ 'button--disabled': formInvalid }"
        >
          Sign Up
        </button>
        <p class="auth-container__form__link">
          Been abducted by us before?
          <router-link to="/login">Login</router-link>
        </p>
      </form>
    </div>
  </div>
  <SpaceToast/>
</template>

<script setup lang="ts">
import FuzzySelect from '@/components/FuzzySelect.vue'
import AwesomeContainer from '@/components/AwesomeContainer.vue'
import SecretInput from '@/components/SecretInput.vue'
import SpaceToast from '@/components/SpaceToast.vue'
import { computed, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from "@/stores/auth-store.ts"
import { routeNames } from "@/router/index.js"
import type { Language } from '@/model/language.ts'
import { useLanguageStore } from '@/stores/language-store.ts'
import { storeToRefs } from 'pinia'
import { useVuelidate } from '@vuelidate/core'
import { required, email, minLength, helpers, maxLength } from '@vuelidate/validators'
import { sendSignupRequest } from '@/api/auth-client.ts'
import { ToastType, useSpaceToaster } from '@/stores/toast-store.ts'

const router = useRouter()
const authStore = useAuthStore()
const languageStore = useLanguageStore()
const toaster = useSpaceToaster()

const { languages } = storeToRefs(languageStore)

const username = ref('')
const userEmail = ref('')
const language = ref<Language>()
const userPassword = ref('')
const confirmedPassword = ref('')
const signupFailed = ref(false)

const signUpButton = ref<HTMLButtonElement>()

const passwordConfirmed = helpers.withParams(
  { type: 'confirmed' },
  (value) => {
    if (!value) return true
    return value === userPassword.value
  }
)

const $v = useVuelidate({
  username: { required, regex: /^[A-Za-z0-9_-]+$/, maxLength: maxLength(64) },
  userEmail: { required, email },
  language: { required },
  userPassword: { required, minLength: minLength(6), maxLength: maxLength(64) },
  confirmPassword: { required, passwordConfirmed },
}, {
  username: username,
  userEmail: userEmail,
  language: language,
  userPassword: userPassword,
  confirmPassword: confirmedPassword,
})

const formInvalid = computed(() => $v.value.$errors.length > 0)

const usernameInvalid = computed(() => $v.value.username.$errors.length > 0)
const usernameRequired = computed(() =>
  $v.value.username.$errors.find(v => v.$validator === 'required') !== undefined
)
const usernameRegex = computed(() =>
  $v.value.username.$errors.find(v => v.$validator === 'regex') !== undefined
)
const usernameMaxLength = computed(() =>
  $v.value.username.$errors.find(v => v.$validator === 'maxLength') !== undefined
)
const userEmailInvalid = computed(() => $v.value.userEmail.$errors.length > 0)
const userEmailRequired = computed(() =>
  $v.value.userEmail.$errors.find(v => v.$validator === 'required') !== undefined
)
const userEmailPattern = computed(() =>
  $v.value.userEmail.$errors.find(v => v.$validator === 'email') !== undefined
)
const languageInvalid = computed(() => $v.value.language.$errors.length > 0)
const languageRequired = computed(() =>
  $v.value.language.$errors.find(v => v.$validator === 'required') !== undefined
)
const userPasswordInvalid = computed(() => $v.value.userPassword.$errors.length > 0)
const userPasswordRequired = computed(() =>
  $v.value.userPassword.$errors.find(v => v.$validator === 'required') !== undefined
)
const userPasswordMinLength = computed(() =>
  $v.value.userPassword.$errors.find(v => v.$validator === 'minLength') !== undefined
)
const userPasswordMaxLength = computed(() =>
  $v.value.userPassword.$errors.find(v => v.$validator === 'maxLength') !== undefined
)
const confirmPasswordInvalid = computed(() => $v.value.confirmPassword.$errors.length > 0)
const confirmPasswordRequired = computed(() =>
  $v.value.confirmPassword.$errors.find(v => v.$validator === 'required') !== undefined
)
const confirmPasswordConfirmed = computed(() =>
  $v.value.confirmPassword.$errors.find(v => v.$validator === 'confirmed') !== undefined
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
    signUpButton.value?.blur()
    return
  }

  await sendSignupRequest(
    username.value,
    userEmail.value,
    userPassword.value,
    language.value?.id
  ).then(async (response) => {
    console.log('Successfully signed up: ', authStore.user?.id)
    authStore.setUser(response.data)
    await router.push({ name: routeNames.flashcards })
  }).catch(error => {
    signupFailed.value = true

    if (error.response?.status === 400) {
      toaster.bake({
        type: ToastType.ERROR,
        title: 'We have an anomaly',
        message: error.response?.data?.message,
        duration: 8000,
      })
    }

    console.error('Failed to sign up: ', error)
  })
}
</script>

<style scoped>
.awesome-container {
  --awesome-container__icon--color: var(--fa-icon--color--globe);
}

.fuzzy-select {
  border-style: solid;
  border-width: 2px;
  border-color: var(--input--border-color);
  border-radius: var(--border-radius);
  background-color: var(--input--bg-color);
  padding: clamp(0.5rem, 0.75rem, 1.25rem);
  --fuzzy-select--font-size: clamp(0.9rem, 2vh, 1rem);
}

.fuzzy-select:hover,
.fuzzy-select:focus-within {
  border-color: var(--input--border-color--focus);
  outline: none;
}

.fuzzy-select--error {
  border-color: var(--input--border-color--error);
}

</style>
