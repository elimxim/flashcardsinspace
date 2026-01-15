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
    <p v-if="verificationResult === VerificationResult.SUCCESS" class="instructions">
      Your email has been verified.
    </p>
    <p v-else-if="verificationResult === VerificationResult.FOUND" class="instructions">
      Enter the confirmation code sent to your email.
      Haven't received it? Press <strong>↻</strong>> to resend.
    </p>
    <p v-else-if="verificationResult === VerificationResult.NOT_FOUND" class="instructions">
      No valid confirmation code found.
      Press <strong>↻</strong> to request a new one.
    </p>
    <p v-else-if="verificationResult === VerificationResult.SESSION_EXPIRED" class="instructions">
      Your session has expired.
      Press <strong>↻</strong> to request a new confirmation code.
    </p>
    <p v-else-if="verificationResult === VerificationResult.EXPIRED" class="instructions">
      The confirmation code has expired.
      Press <strong>↻</strong> to request a new one.
    </p>
    <p v-else-if="verificationResult === VerificationResult.INVALID" class="instructions">
      Invalid confirmation code.
      Please try again.
    </p>
    <p v-else-if="verificationResult === VerificationResult.USED" class="instructions">
      The confirmation code has been used.
      Press <strong>↻</strong> to request a new one.
    </p>
    <p v-else-if="verificationResult === VerificationResult.LOCKED" class="instructions">
      You have reached the maximum number of attempts.
      Press <strong>↻</strong> to request a new one.
    </p>
    <p v-else-if="verificationResult === VerificationResult.LIMITED" class="instructions">
      You have reached the maximum number of confirmation codes per hour.
      Please try again later.
    </p>
    <p v-else-if="codeResent" class="instructions">
      A new confirmation code has been sent to your email.
      Please check your inbox and enter the code below.
    </p>
    <p v-else class="instructions">
      Enter the confirmation code sent to your email.
      Haven't received it? Press <strong>↻</strong> to resend.
    </p>

    <div class="control-device">
      <CodeConfirmationDevice
        ref="ccd"
        :attempts="attempts"
        :verify-code="verifyCode"
        :resend-code="resendCode"
      />
    </div>
  </div>
  <SpaceToast/>
</template>

<script setup lang="ts">
import CodeConfirmationDevice from '@/components/CodeConfirmationDevice.vue'
import { onMounted, ref } from 'vue'
import {
  sendCodeConfirmationRequest,
  sendCodeContextRequest,
  sendCodeVerificationRequest,
} from '@/api/auth-client.ts'
import { useAuthStore } from '@/stores/auth-store.ts'
import { Log, LogTag } from '@/utils/logger.ts'
import {
  parseVerificationIntent,
  toVerificationCodeResult,
  VerificationResult,
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

const intent = ref(props.intent)
const attempts = ref(0)
const codeResent = ref(false)
const verificationResult = ref<VerificationResult | undefined>()
const ccd = ref<InstanceType<typeof CodeConfirmationDevice>>()

async function verifyCode(code: string): Promise<void> {
  await sendCodeVerificationRequest(code)
    .then(async (response) => {
      await processVerificationResponse(response.data)
    })
    .catch((error) => {
      toaster.bakeError(userApiErrors.CONFIRMATION_CODE__VERIFICATION_FAILED, error.response?.data)
      Log.error(LogTag.LOGIC, 'Failed to verify confirmation code', error)
    })
}

async function resendCode(): Promise<void> {
  await sendCodeConfirmationRequest()
    .then(() => {
      attempts.value = 0
      ccd.value?.triggerIdle()
      codeResent.value = true
      verificationResult.value = undefined
    })
    .catch((error) => {
      Log.error(LogTag.LOGIC, 'Failed to resend confirmation code', error)
      toaster.bakeError(userApiErrors.CONFIRMATION_CODE__RESENDING_FAILED, error.response?.data)
    })
}

async function processVerificationResponse(response: VerificationCodeResponse) {
  if (response.purpose) {
    intent.value = parseVerificationIntent(response.purpose)
  }

  const result = toVerificationCodeResult(response.result)
  switch (result) {
    case VerificationResult.LIMITED:
    case VerificationResult.LOCKED:
    case VerificationResult.SESSION_EXPIRED:
    case VerificationResult.EXPIRED:
    case VerificationResult.USED:
    case VerificationResult.NOT_FOUND:
      await ccd.value?.triggerFailure()
      ccd.value?.switchOff()
      verificationResult.value = result
      attempts.value = response.attempts ?? 3
      break
    case VerificationResult.INVALID:
      await ccd.value?.triggerFailure()
      verificationResult.value = result
      attempts.value = response.attempts ?? 3
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
  await sendCodeContextRequest()
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
  padding: 0.5rem;
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
