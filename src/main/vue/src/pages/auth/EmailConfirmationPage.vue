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
      :class="{ 'auth-container--error': formInvalid }"
    >
      <form class="auth-form" novalidate @submit.prevent="sendCode">
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
        <SmartButton
          class="calm-button"
          text="Send Code"
          type="submit"
          :disabled="formInvalid"
          auto-blur
          fill-width
        />
      </form>
    </div>
  </div>
  <SpaceToast/>
</template>

<script setup lang="ts">
import SmartInput from '@/components/SmartInput.vue'
import SpaceToast from '@/components/SpaceToast.vue'
import SmartButton from '@/components/SmartButton.vue'
import ErrorText from '@/components/ErrorText.vue'
import { computed, ref } from 'vue'
import { useVuelidate } from '@vuelidate/core'
import { email, required } from '@vuelidate/validators'
import { Log, LogTag } from '@/utils/logger.ts'
import {
  sendVerificationRequestWithBody,
} from '@/api/auth-client.ts'
import { VerificationType } from '@/core-logic/user-logic.ts'
import { useSpaceToaster } from '@/stores/toast-store.ts'
import { userApiErrors } from '@/api/user-api-error.ts'
import { useRouter } from 'vue-router'
import { routeNames } from '@/router'

const router = useRouter()
const toaster = useSpaceToaster()

const userEmail = ref('')

const $v = useVuelidate({
  userEmail: { required, email },
}, {
  userEmail: userEmail,
})

const formInvalid = computed(() => $v.value.$errors.length > 0)
const userEmailInvalid = computed(() => $v.value.userEmail.$errors.length > 0)
const userEmailWrongFormat = computed(() =>
  $v.value.userEmail.$errors.find(v => v.$validator === 'email') !== undefined
)

async function sendCode() {
  $v.value.$touch()
  if (formInvalid.value) {
    Log.error(LogTag.LOGIC, 'The form is invalid')
    return
  }

  await sendVerificationRequestWithBody(userEmail.value, VerificationType.PASSWORD_RESET_REQUEST)
    .then(async () => {
      await router.push({
        name: routeNames.codeVerification,
        query: { type: VerificationType.PASSWORD_RESET_REQUEST }
      })
    })
    .catch((error) => {
      Log.error(LogTag.LOGIC, 'Failed to send verification code', error)
      if (error.response?.status === 429) {
        toaster.bakeError(userApiErrors.VERIFICATION__TOO_MANY_REQUESTS, error.response?.data)
      } else {
        toaster.bakeError(userApiErrors.VERIFICATION__REQUEST_FAILED, error.response?.data)
      }
    })
}

</script>

<style scoped>

</style>
