<template>
  <div
    :class="[
      'page',
      'flex-column',
      'padding-auto',
      'home-page',
      'scroll-none',
      'touch-none',
    ]"
  >
    <div v-if="cannotProceedPasswordReset" class="dead-end-section">
      <p class="instructions">
        Your session has expired.
        Please try again.
      </p>
      <div class="start-over-button-wrapper">
        <AwesomeButton
          icon="fa-solid fa-door-closed"
          class="start-over-button"
          :on-click="startOverPasswordReset"
          fill-space
          square
        />
      </div>
    </div>
    <div v-else class="verification-section">
      <p v-if="isRegistrationRequest" class="instructions">
        Complete your registration.
      </p>
      <p v-else-if="isPasswordResetRequest" class="instructions">
        Complete your password reset.
      </p>

      <p v-if="verificationResult === VerificationResult.SUCCESS" class="instructions">
        Your email has been verified.
      </p>
      <p v-else-if="verificationResult === VerificationResult.FOUND" class="instructions">
        Enter the verification code sent to your email.
        Haven't received it? Press <strong>↻</strong> to resend.
      </p>
      <p v-else-if="verificationResult === VerificationResult.NOT_FOUND" class="instructions">
        No valid verification code found.
        Press <strong>↻</strong> to request a new one.
      </p>
      <p v-else-if="verificationResult === VerificationResult.SESSION_EXPIRED" class="instructions">
        Your session has expired.
        Press <strong>↻</strong> to request a new verification code.
      </p>
      <p v-else-if="verificationResult === VerificationResult.EXPIRED" class="instructions">
        The verification code has expired.
        Press <strong>↻</strong> to request a new one.
      </p>
      <p v-else-if="verificationResult === VerificationResult.INVALID" class="instructions">
        Invalid verification code.
        Please try again.
      </p>
      <p v-else-if="verificationResult === VerificationResult.USED" class="instructions">
        The verification code has been used.
        Press <strong>↻</strong> to request a new one.
      </p>
      <p v-else-if="verificationResult === VerificationResult.LOCKED" class="instructions">
        You have reached the maximum number of attempts.
        Press <strong>↻</strong> to request a new one.
      </p>
      <p v-else-if="verificationResult === VerificationResult.LIMITED" class="instructions">
        You have reached the maximum number of verification codes per hour.
        Please try again later.
      </p>
      <p v-else-if="codeResent" class="instructions">
        A new verification code has been sent to your email.
        Please check your inbox and enter the code below.
      </p>
      <p v-else class="instructions">
        Enter the verification code sent to your email.
        Haven't received it? Press <strong>↻</strong> to resend.
      </p>

      <div class="control-device">
        <CodeVerificationDevice
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
import SpaceToast from '@/components/SpaceToast.vue'
import CodeVerificationDevice from '@/components/CodeVerificationDevice.vue'
import { computed, onMounted, ref } from 'vue'
import {
  sendVerificationRequest,
  sendVerificationContextRequest,
  sendVerificationCodeRequest,
} from '@/api/auth-client.ts'
import { useAuthStore } from '@/stores/auth-store.ts'
import { Log, LogTag } from '@/utils/logger.ts'
import {
  parseVerificationType,
  toVerificationCodeResult,
  VerificationResult,
  VerificationType,
  whoAmI
} from '@/core-logic/user-logic.ts'
import { useSpaceToaster } from '@/stores/toast-store.ts'
import { userApiErrors } from '@/api/user-api-error.ts'
import { VerificationIntentResponse } from '@/api/communication.ts'
import { useRouter } from 'vue-router'
import { routeNames } from '@/router'
import AwesomeButton from '@/components/AwesomeButton.vue'

const props = withDefaults(defineProps<{
  type?: VerificationType
}>(), {
  type: undefined,
})

const router = useRouter()
const toaster = useSpaceToaster()
const authStore = useAuthStore()

const verificationType = ref(props.type)
const attempts = ref(0)
const codeResent = ref(false)
const verificationResult = ref<VerificationResult | undefined>()
const ccd = ref<InstanceType<typeof CodeVerificationDevice>>()

const isRegistrationRequest = computed(() =>
  verificationType.value === VerificationType.REGISTRATION_REQUEST
)

const isPasswordResetRequest = computed(() =>
  verificationType.value === VerificationType.PASSWORD_RESET_REQUEST
)

const cannotProceedPasswordReset = computed(() => {
  const isSessionExpired = verificationResult.value === VerificationResult.SESSION_EXPIRED
  return (isPasswordResetRequest.value) && isSessionExpired
})

function startOverPasswordReset() {
  router.push({ name: routeNames.emailConfirmation })
}

async function verifyCode(code: string): Promise<void> {
  await sendVerificationCodeRequest(code)
    .then(async (response) => {
      await processVerificationResponse(response.data)
    })
    .catch((error) => {
      toaster.bakeError(userApiErrors.VERIFICATION__CODE_FAILED, error.response?.data)
      Log.error(LogTag.LOGIC, 'Failed to test verification code', error)
    })
}

async function resendCode(): Promise<void> {
  await sendVerificationRequest()
    .then(() => {
      attempts.value = 0
      ccd.value?.triggerIdle()
      codeResent.value = true
      verificationResult.value = undefined
    })
    .catch((error) => {
      Log.error(LogTag.LOGIC, 'Failed to resend verification code', error)
      if (error.response?.status === 429) {
        toaster.bakeError(userApiErrors.VERIFICATION__TOO_MANY_REQUESTS, error.response?.data)
      } else {
        toaster.bakeError(userApiErrors.VERIFICATION__REQUEST_FAILED, error.response?.data)
      }
    })
}

async function processVerificationResponse(response: VerificationIntentResponse) {
  Log.log(LogTag.LOGIC, `Verification response: ${JSON.stringify(response)}`)
  if (response.type) {
    verificationType.value = parseVerificationType(response.type)
  }

  const result = toVerificationCodeResult(response.result)
  switch (result) {
    case VerificationResult.LIMITED:
      await ccd.value?.triggerFailure()
      ccd.value?.lock()
      verificationResult.value = result
      attempts.value = 3
      break
    case VerificationResult.LOCKED:
    case VerificationResult.SESSION_EXPIRED:
    case VerificationResult.EXPIRED:
    case VerificationResult.USED:
    case VerificationResult.NOT_FOUND:
      await ccd.value?.triggerFailure()
      ccd.value?.switchOff()
      verificationResult.value = result
      attempts.value = 3
      break
    case VerificationResult.INVALID:
      await ccd.value?.triggerFailure()
      verificationResult.value = result
      attempts.value = 3
      break
    case VerificationResult.SUCCESS:
      await ccd.value?.triggerSuccess()
      verificationResult.value = result
      attempts.value = 0
      await onSuccess()
      break
    case VerificationResult.FOUND:
      verificationResult.value = result
      attempts.value = response.attempts ?? 0
      break
    default:
      ccd.value?.triggerIdle()
      break
  }
}

async function onSuccess() {
  Log.log(LogTag.LOGIC, 'Code confirmed')
  if (verificationType.value === VerificationType.REGISTRATION_REQUEST) {
    if (authStore.isAuthenticated) {
      authStore.setEmailVerified()
      await router.push({ name: routeNames.user })
    } else {
      Log.error(LogTag.LOGIC, 'Email verified but user is not authenticated')
      await whoAmI().then((success) => {
        if (success) {
          authStore.setEmailVerified()
        }
      })
    }
  } else if (verificationType.value === VerificationType.PASSWORD_RESET_REQUEST) {
    await router.push({ name: routeNames.passwordReset })
  } else {
    toaster.bakeError(userApiErrors.VERIFICATION__UNKNOWN_TYPE)
    Log.error(LogTag.LOGIC, `Unknown verification intent: ${verificationType.value}`)
  }
}

onMounted(async () => {
  await sendVerificationContextRequest()
    .then(async (response) => {
      await processVerificationResponse(response.data)
    })
    .catch((error) => {
      toaster.bakeError(userApiErrors.VERIFICATION__CONTEXT_FAILED, error.response?.data)
      Log.error(LogTag.LOGIC, 'Failed to get verification code context', error)
    })
})

</script>

<style scoped>
.verification-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  gap: 0.5rem;
}

.dead-end-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  margin: auto;
}

.start-over-button-wrapper {
  width: 120px;
  height: 120px;
}

.start-over-button {
  --awesome-button--icon--size: 80px;
  --awesome-button--bg: linear-gradient(135deg, rgb(113, 91, 145) 0%, rgb(83, 110, 125) 100%);
  --awesome-button--bg--hover: linear-gradient(135deg, rgb(161, 130, 205) 0%, rgb(137, 180, 204) 100%);
  --awesome-button--border: 1px solid var(--cp--border-color);
  --awesome-button--border-radius: 6px;
}

.instructions {
  text-align: center;
  font-size: clamp(0.8rem, 2vw, 1rem);
  letter-spacing: 0.02rem;
  text-wrap: balance;
  color: #fdfbff;
  margin: 0;
}

.instructions strong {
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
