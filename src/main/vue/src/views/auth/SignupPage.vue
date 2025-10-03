<template>
  <div
    :class="[
      'page',
      'page--x-centered',
      'page--y-centered',
      'page--auto-padded',
    ]"
  >
    <div
      class="auth-container transition--border-color"
      :class="{ 'auth-container--error': signupFailed }"
    >
      <form @submit.prevent="signup" class="auth-form" novalidate>
        <SmartInput
          v-model="username"
          id="name"
          type="text"
          name="name"
          autocomplete="name"
          :invalid="usernameNotSet"
          :placeholder="usernameNotSet ? 'Name is required' : 'Name'"
        />
        <span v-if="usernameRegexMismatch" class="text-error">
          Please use only letters, numbers, dashes, underscores, and spaces
        </span>
        <span v-else-if="usernameMaxLengthInvalid" class="text-error">
            This username is expanding faster than the universe! Please keep it under 64 characters
        </span>
        <SmartInput
          v-model="userEmail"
          id="username"
          type="email"
          name="username"
          autocomplete="username"
          :invalid="userEmailInvalid"
          :placeholder="userEmailNotSet ? 'Email is required' : 'Email'"
        />
        <span v-if="userEmailWrongFormat" class="text-error">
          This email seems to be lost in a cosmic dust cloud. Please check the format
        </span>
        <AwesomeContainer icon="fa-solid fa-globe" class="awesome-globe">
          <FuzzySelect
            :options="languages"
            v-model="language"
            id="language"
            :optionLabel="(lang) => lang.name"
            :invalid="languageNotSet"
            :optionPlaceholder="languageNotSet ? 'Language is required' : 'Language'"
            searchPlaceholder="Search..."
          />
        </AwesomeContainer>
        <SmartInput
          v-model="userPassword"
          id="password"
          type="password"
          name="new-password"
          autocomplete="new-password"
          :invalid="userPasswordInvalid"
          :placeholder="userPasswordNotSet ? 'Password is required' : 'Password'"
        />
        <span v-if="userPasswordMinLengthInvalid" class="text-error">
          Your password must be stronger than a piece of space junk. Please use 6 or more characters
        </span>
        <span v-else-if="userPasswordMaxLengthInvalid" class="text-error">
          This secret is expanding faster than the universe! Please keep it under 64 characters
        </span>
        <SmartInput
          v-model="confirmedPassword"
          id="confirm-password"
          type="password"
          name="new-password"
          autocomplete="new-password"
          :invalid="confirmPasswordInvalid"
          :placeholder="confirmPasswordNotSet ? 'Password confirmation is required' : 'Confirm Password'"
        />
        <span v-if="confirmPasswordMismatch" class="text-error">
          The passwords do not match. Please try again
        </span>
        <SmartButton
          class="auth-button"
          text="Sign Up"
          type="submit"
          :disabled="formInvalid"
          autoBlur
          fillWidth
        />
        <p class="auth-link">
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
import SmartInput from '@/components/SmartInput.vue'
import SmartButton from '@/components/SmartButton.vue'
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
import { useSpaceToaster } from '@/stores/toast-store.ts'

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

const passwordConfirmed = helpers.withParams(
  { type: 'confirmed' },
  (value) => {
    if (!value) return true
    return value === userPassword.value
  }
)

const $v = useVuelidate({
  username: {
    required,
    maxLength: maxLength(64),
    regex: helpers.regex(/^[A-Za-z0-9 _-]+$/),
  },
  userEmail: { required, email },
  language: { required },
  userPassword: {
    required,
    minLength: minLength(6),
    maxLength: maxLength(64),
  },
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
const usernameNotSet = computed(() =>
  $v.value.username.$errors.find(v => v.$validator === 'required') !== undefined
)
const usernameRegexMismatch = computed(() =>
  $v.value.username.$errors.find(v => v.$validator === 'regex') !== undefined
)
const usernameMaxLengthInvalid = computed(() =>
  $v.value.username.$errors.find(v => v.$validator === 'maxLength') !== undefined
)
const userEmailInvalid = computed(() => $v.value.userEmail.$errors.length > 0)
const userEmailNotSet = computed(() =>
  $v.value.userEmail.$errors.find(v => v.$validator === 'required') !== undefined
)
const userEmailWrongFormat = computed(() =>
  $v.value.userEmail.$errors.find(v => v.$validator === 'email') !== undefined
)
const languageInvalid = computed(() => $v.value.language.$errors.length > 0)
const languageNotSet = computed(() =>
  $v.value.language.$errors.find(v => v.$validator === 'required') !== undefined
)
const userPasswordInvalid = computed(() => $v.value.userPassword.$errors.length > 0)
const userPasswordNotSet = computed(() =>
  $v.value.userPassword.$errors.find(v => v.$validator === 'required') !== undefined
)
const userPasswordMinLengthInvalid = computed(() =>
  $v.value.userPassword.$errors.find(v => v.$validator === 'minLength') !== undefined
)
const userPasswordMaxLengthInvalid = computed(() =>
  $v.value.userPassword.$errors.find(v => v.$validator === 'maxLength') !== undefined
)
const confirmPasswordInvalid = computed(() => $v.value.confirmPassword.$errors.length > 0)
const confirmPasswordNotSet = computed(() =>
  $v.value.confirmPassword.$errors.find(v => v.$validator === 'required') !== undefined
)
const confirmPasswordMismatch = computed(() =>
  $v.value.confirmPassword.$errors.find(v => v.$validator === 'passwordConfirmed') !== undefined
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
  if (userPasswordInvalid.value) {
    $v.value.userPassword.$reset()
  }
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
  console.log('Signing up...')
  signupFailed.value = false
  $v.value.$touch()
  if (formInvalid.value) {
    console.error('The form is invalid')
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
      toaster.bakeError('Anomaly detected', error.response?.data)
    } else {
      toaster.bakeError('System error', error.response?.data)
    }

    console.error('Failed to sign up: ', error)
  })
}
</script>

<style scoped>
</style>
