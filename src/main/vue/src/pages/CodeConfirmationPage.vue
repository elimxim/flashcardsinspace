<template>
  <div
    :class="[
      'page',
      'flex-column',
      'padding-auto',
      'flex-center',
      'home-page',
      'scroll-none',
      'touch-none',
      'confirmation-code-page',
    ]"
  >
    <p class="instructions">
      {{ instructions }}
    </p>
    <div class="control-device-wrapper">
      <div class="control-device">
        <CodeConfirmationDevice
          ref="ccd"
          :attempts="attempts"
          :verify-code="verifyCode"
          :resend-code="resendCode"
        />
      </div>
    </div>
  </div>
  <SpaceToast/>
</template>

<script setup lang="ts">
import CodeConfirmationDevice from '@/components/CodeConfirmationDevice.vue'
import { computed, onMounted, ref } from 'vue'
import {
  sendVerificationCodeGetRequest,
  sendVerificationCodePostRequest,
  sendVerificationCodePutRequest,
} from '@/api/auth-client.ts'
import { useAuthStore } from '@/stores/auth-store.ts'
import { Log, LogTag } from '@/utils/logger.ts'
import {
  parseVerificationIntent,
  toVerificationCodeResult,
  VerificationCodeResult,
  VerificationIntent,
  whoAmI
} from '@/core-logic/user-logic.ts'
import { useSpaceToaster } from '@/stores/toast-store.ts'
import { userApiErrors } from '@/api/user-api-error.ts'
import { VerificationCodeResponse } from '@/api/communication.ts'
import { useRouter } from 'vue-router'
import { routeNames } from '@/router'

const props = withDefaults(defineProps<{
  intent?: VerificationIntent
}>(), {
  intent: undefined,
})

const router = useRouter()
const toaster = useSpaceToaster()
const authStore = useAuthStore()

const email = ref<string>()
const intent = ref(props.intent)
const attempts = ref(0)
const codeResent = ref(false)
const verificationResult = ref<VerificationCodeResult | undefined>()
const ccd = ref<InstanceType<typeof CodeConfirmationDevice>>()

const instructions = computed(() => {
  switch (verificationResult.value) {
    case VerificationCodeResult.SUCCESS:
      return `Your email has been verified.`
    case VerificationCodeResult.FOUND:
      return `
        Enter the confirmation code sent to your email.
        Haven't received it? Press <em>↻</em>> to resend.
      `
    case VerificationCodeResult.NOT_FOUND:
      return `
        No valid confirmation code found.
        Press <em>↻</em> to request a new one.
      `
    case VerificationCodeResult.EXPIRED:
      return `
        The confirmation code has expired.
        Press <em>↻</em> to request a new one.
      `
    case VerificationCodeResult.INVALID:
      return `
        Invalid confirmation code.
        Please try again.
      `
    case VerificationCodeResult.USED:
      return `
        The confirmation code has been used.
        Press <em>↻</em> to request a new one.
      `
    case VerificationCodeResult.LOCKED:
      return `
        You have reached the maximum number of attempts.
        Press <em>↻</em> to request a new one.
      `
    case VerificationCodeResult.LIMITED:
      return `
        You have reached the maximum number of confirmation codes per hour.
        Please try again later.
      `
  }

  if (codeResent.value) {
    return `
      A new confirmation code has been sent to your email.
      Please check your inbox and enter the code below.
    `
  }

  return `
    Enter the confirmation code sent to your email.
    Haven't received it? Press <em>↻</em> to resend.
  `
})

async function verifyCode(code: string): Promise<void> {
  await sendVerificationCodePutRequest(code)
    .then(async (response) => {
      await processVerificationResponse(response.data)
    })
    .catch((error) => {
      toaster.bakeError(userApiErrors.CONFIRMATION_CODE__VERIFICATION_FAILED, error.response?.data)
      Log.error(LogTag.LOGIC, 'Failed to verify confirmation code', error)
    })
}

async function resendCode(): Promise<void> {
  const email = await tryGetEmail()
  await sendVerificationCodePostRequest(email, intent.value)
    .then(() => {
      attempts.value = 0
      ccd.value?.triggerIdle()
      codeResent.value = true
      verificationResult.value = undefined
    })
    .catch((error) => {
      toaster.bakeError(userApiErrors.CONFIRMATION_CODE__RESENDING_FAILED, error.response?.data)
      Log.error(LogTag.LOGIC, 'Failed to resend confirmation code', error)
    })
}

async function tryGetEmail(): Promise<string | undefined> {
  if (email.value) {
    return email.value
  }

  // in case user is authenticated
  return whoAmI().then(() => {
    return authStore.user?.email
  })
}

async function processVerificationResponse(response: VerificationCodeResponse) {
  if (response.purpose) {
    intent.value = parseVerificationIntent(response.purpose)
  }

  const result = toVerificationCodeResult(response.result)
  switch (result) {
    case VerificationCodeResult.LIMITED:
    case VerificationCodeResult.LOCKED:
    case VerificationCodeResult.EXPIRED:
    case VerificationCodeResult.USED:
    case VerificationCodeResult.NOT_FOUND:
      await ccd.value?.triggerFailure()
      ccd.value?.switchOff()
      verificationResult.value = result
      attempts.value = response.attempts ?? 3
      break
    case VerificationCodeResult.INVALID:
      await ccd.value?.triggerFailure()
      verificationResult.value = result
      attempts.value = response.attempts ?? 3
      break
    case VerificationCodeResult.SUCCESS:
      await ccd.value?.triggerSuccess()
      verificationResult.value = result
      attempts.value = 0
      await onSuccess()
      break
    case VerificationCodeResult.FOUND:
      verificationResult.value = result
      attempts.value = response.attempts ?? 0
      break
    default:
      ccd.value?.triggerIdle()
      break
  }
}

async function onSuccess() {
  if (intent.value === VerificationIntent.EMAIL_VERIFICATION) {
    if (authStore.isAuthenticated) {
      Log.log(LogTag.LOGIC, 'Email verified')
      authStore.setEmailVerified()
      return router.push({ name: routeNames.user })
    } else {
      Log.error(LogTag.LOGIC, 'Email verified but user is not authenticated')
      await whoAmI().then((success) => {
        if (success) {
          authStore.setEmailVerified()
        }
      })
    }
  } else {
    Log.error(LogTag.LOGIC, `Unknown verification intent: ${intent.value}`)
  }
}

onMounted(async () => {
  await sendVerificationCodeGetRequest()
    .then(async (response) => {
      await processVerificationResponse(response.data)
    })
    .catch((error) => {
      toaster.bakeError(userApiErrors.CONFIRMATION_CODE__CONTEXT_FAILED, error.response?.data)
      Log.error(LogTag.LOGIC, 'Failed to get verification code context', error)
    })
})

</script>

<style scoped>
.confirmation-code-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  gap: 0.5rem;
}

.instructions {
  text-align: center;
  font-size: clamp(0.8rem, 2vw, 1rem);
  letter-spacing: 0.02rem;
  text-wrap: balance;
  color: #fdfbff;
  padding: 0.5rem;
  margin: 0;
}

.instructions em {
  color: #ff0000;
  font-style: normal;
  font-weight: 600;
}

.control-device {
  height: 75dvh;
  width: 90vw;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
