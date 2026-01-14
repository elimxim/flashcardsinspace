<template>
  <div
    :class="[
      'page',
      'page--bg--light',
      'flex-column',
      'flex-center',
      'padding-auto',
      'scrollbar-hidden',
  ]">
    <div
      class="auth-container"
      :class="{ 'auth-container--error': signupFailed }"
    >
      <form class="auth-form" novalidate @submit.prevent="signup">
        <SmartInput
          id="name"
          v-model="username"
          type="text"
          name="name"
          autocomplete="name"
          :invalid="usernameInvalid"
          placeholder="Name"
        />
        <ErrorText
          :errors="[
            {
              when: usernameRegexMismatch,
              text: 'Please use only letters, numbers, dashes, underscores, and spaces'
            },
            {
              when: usernameMaxLengthInvalid,
              text: 'This username is expanding faster than the universe! Please keep it under 64 characters'
            },
          ]"
        />
        <SmartInput
          id="username"
          v-model="userEmail"
          type="email"
          name="username"
          autocomplete="username"
          :invalid="userEmailInvalid"
          placeholder="Email"
        />
        <ErrorText
          :when="userEmailWrongFormat"
          text="This email seems to be lost in a cosmic dust cloud. Please check the format"
        />
        <AwesomeContainer icon="fa-solid fa-globe" class="awesome-globe">
          <FuzzySelect
            id="language"
            v-model="language"
            :options="languages"
            :option-label="(lang) => lang.name"
            :invalid="languageInvalid"
            option-placeholder="Language"
            search-placeholder="Search..."
          />
        </AwesomeContainer>
        <SmartInput
          id="password"
          v-model="userPassword"
          type="password"
          name="new-password"
          autocomplete="new-password"
          :invalid="userPasswordInvalid"
          placeholder="Password"
        />
        <ErrorText
          :errors="[
            {
              when: userPasswordMinLengthInvalid,
              text: 'Your password must be stronger than a piece of space junk. Please use 6 or more characters'
            },
            {
              when: userPasswordMaxLengthInvalid,
              text: 'This secret is expanding faster than the universe! Please keep it under 64 characters'
            }
          ]"
        />
        <SmartInput
          id="confirm-password"
          v-model="confirmedPassword"
          type="password"
          name="new-password"
          autocomplete="new-password"
          :invalid="confirmPasswordInvalid"
          placeholder="Confirm Password"
        />
        <ErrorText
          :when="confirmPasswordMismatch"
          text="The passwords do not match. Please try again"
        />
        <SmartButton
          class="calm-button"
          text="Sign Up"
          type="submit"
          :disabled="formInvalid"
          auto-blur
          fill-width
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
import ErrorText from '@/components/ErrorText.vue'
import { computed, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from "@/stores/auth-store.ts"
import { routeNames } from "@/router"
import type { Language } from '@/model/language.ts'
import { useLanguageStore } from '@/stores/language-store.ts'
import { storeToRefs } from 'pinia'
import { useVuelidate } from '@vuelidate/core'
import { required, email, minLength, helpers, maxLength } from '@vuelidate/validators'
import { sendSignupRequest } from '@/api/auth-client.ts'
import { useSpaceToaster } from '@/stores/toast-store.ts'
import { saveUserSignedUpToCookies } from '@/utils/cookies.ts'
import { Log, LogTag } from '@/utils/logger.ts'
import { userApiErrors } from '@/api/user-api-error.ts'
import { VerificationIntent } from '@/core-logic/user-logic.ts';

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
const usernameRegexMismatch = computed(() =>
  $v.value.username.$errors.find(v => v.$validator === 'regex') !== undefined
)
const usernameMaxLengthInvalid = computed(() =>
  $v.value.username.$errors.find(v => v.$validator === 'maxLength') !== undefined
)
const userEmailInvalid = computed(() => $v.value.userEmail.$errors.length > 0)
const userEmailWrongFormat = computed(() =>
  $v.value.userEmail.$errors.find(v => v.$validator === 'email') !== undefined
)
const languageInvalid = computed(() => $v.value.language.$errors.length > 0)
const userPasswordInvalid = computed(() => $v.value.userPassword.$errors.length > 0)
const userPasswordMinLengthInvalid = computed(() =>
  $v.value.userPassword.$errors.find(v => v.$validator === 'minLength') !== undefined
)
const userPasswordMaxLengthInvalid = computed(() =>
  $v.value.userPassword.$errors.find(v => v.$validator === 'maxLength') !== undefined
)
const confirmPasswordInvalid = computed(() => $v.value.confirmPassword.$errors.length > 0)
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
  Log.log(LogTag.LOGIC, 'Signing up...')
  signupFailed.value = false
  $v.value.$touch()
  if (formInvalid.value) {
    Log.error(LogTag.LOGIC, 'The form is invalid')
    return
  }

  await sendSignupRequest(
    username.value,
    userEmail.value,
    userPassword.value,
    language.value?.id
  ).then(async (response) => {
    Log.log(LogTag.LOGIC, 'Successfully signed up: ', authStore.user?.id)
    authStore.setUser(response.data)
    saveUserSignedUpToCookies(true)
    await router.push({
      name: routeNames.codeConfirmation,
      query: { intent: VerificationIntent.EMAIL_VERIFICATION },
    })
  }).catch(error => {
    signupFailed.value = true

    if (error.response?.status === 400) {
      toaster.bakeError(userApiErrors.AUTH_BAD_DATA, error.response?.data)
    } else if (error.response?.status === 409) {
      toaster.bakeError(userApiErrors.USER__ALREADY_EXISTS, error.response?.data)
    } else {
      toaster.bakeError(userApiErrors.AUTH_FAILED, error.response?.data)
    }

    Log.error(LogTag.LOGIC, 'Failed to sign up: ', error)
  })
}
</script>

<style scoped>
</style>
