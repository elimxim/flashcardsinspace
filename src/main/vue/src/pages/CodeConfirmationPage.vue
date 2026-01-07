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
    <p v-if="verificationResult === CodeVerificationResult.LIMITED" class="code-instructions">
      You have reached the maximum number of confirmation codes per hour.
      Please try again later.
    </p>
    <p v-else-if="verificationResult === CodeVerificationResult.LOCKED" class="code-instructions">
      You have reached the maximum number of attempts.
      Press <em>↻</em> request a new one.
    </p>
    <p v-else-if="verificationResult === CodeVerificationResult.EXPIRED" class="code-instructions">
      The confirmation code has expired.
      Press <em>↻</em> request a new one.
    </p>
    <p v-else-if="verificationResult === CodeVerificationResult.NOT_FOUND"
       class="code-instructions">
      No valid confirmation code found.
      Press <em>↻</em> request a new one.
    </p>
    <p v-else-if="verificationResult === CodeVerificationResult.INVALID" class="code-instructions">
      Invalid confirmation code.
      Please try again.
    </p>
    <p v-else-if="verificationResult === CodeVerificationResult.TESTED" class="code-instructions">
      Welcome back!
      Enter the confirmation code sent to your email.
      Haven't received it? Press <em>↻</em> to resend.
    </p>
    <p v-else-if="verificationResult === CodeVerificationResult.SUCCESS" class="code-instructions">
      Your email has been verified.
    </p>
    <p v-else-if="codeResent" class="code-instructions">
      A new confirmation code has been sent to your email.
      Please check your inbox and enter the code below.
    </p>
    <p v-else class="code-instructions">
      Enter the confirmation code sent to your email.
      Haven't received it? Press <em>↻</em> to resend.
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
import SpaceToast from '@/components/SpaceToast.vue'
import { onMounted, ref } from 'vue'
import {
  sendConfirmationCodeRequest,
  sendConfirmationCodeVerificationRequest,
  sendConfirmationCodeTestRequest,
} from '@/api/auth-client.ts'
import { useAuthStore } from '@/stores/auth-store.ts'
import { storeToRefs } from 'pinia'
import { Log, LogTag } from '@/utils/logger.ts'
import {
  CodeVerificationResult,
  confirmationCodePurposes,
  parseCodeVerificationResult
} from '@/core-logic/user-logic.ts'
import { useSpaceToaster } from '@/stores/toast-store.ts'
import { userApiErrors } from '@/api/user-api-error.ts'
import { ConfirmationCodeResponse } from '@/api/communication.ts'
import { useRouter } from 'vue-router'
import { routeNames } from '@/router';

const router = useRouter()
const toaster = useSpaceToaster()
const authStore = useAuthStore()

const { user } = storeToRefs(authStore)

const attempts = ref(0)
const codeResent = ref(false)
const verificationResult = ref<CodeVerificationResult | undefined>()
const ccd = ref<InstanceType<typeof CodeConfirmationDevice>>()

async function verifyCode(code: string): Promise<void> {
  if (!user.value) {
    Log.error(LogTag.SYSTEM, 'User is undefined, cannot verify confirmation code')
    return
  }

  await sendConfirmationCodeVerificationRequest(user.value.email, code, confirmationCodePurposes.EMAIL_VERIFICATION)
    .then(async (response) => {
      await processVerificationResponse(response.data)
    })
    .catch((error) => {
      toaster.bakeError(userApiErrors.CONFIRMATION_CODE__VERIFICATION_FAILED, error.response?.data)
      Log.error(LogTag.SYSTEM, 'Failed to verify confirmation code', error)
    })
}

async function resendCode(): Promise<void> {
  if (!user.value) {
    Log.error(LogTag.SYSTEM, 'User is undefined, cannot resend confirmation code')
    return
  }

  await sendConfirmationCodeRequest(user.value.email, confirmationCodePurposes.EMAIL_VERIFICATION)
    .then(() => {
      attempts.value = 0
      ccd.value?.triggerIdle()
      codeResent.value = true
      verificationResult.value = undefined
    })
    .catch((error) => {
      toaster.bakeError(userApiErrors.CONFIRMATION_CODE__RESENDING_FAILED, error.response?.data)
      Log.error(LogTag.SYSTEM, 'Failed to resend confirmation code', error)
    })
}

async function processVerificationResponse(response: ConfirmationCodeResponse) {
  const result = parseCodeVerificationResult(response.result)
  switch (result) {
    case CodeVerificationResult.LIMITED:
    case CodeVerificationResult.LOCKED:
    case CodeVerificationResult.EXPIRED:
    case CodeVerificationResult.NOT_FOUND:
      await ccd.value?.triggerFailure()
      ccd.value?.switchOff()
      verificationResult.value = result
      attempts.value = response.attempts ?? 3
      break
    case CodeVerificationResult.INVALID:
      await ccd.value?.triggerFailure()
      verificationResult.value = result
      attempts.value = response.attempts ?? 3
      break
    default:
      ccd.value?.triggerIdle()
      break
    case CodeVerificationResult.SUCCESS:
      await ccd.value?.triggerSuccess()
      verificationResult.value = result
      attempts.value = 0
      authStore.setEmailVerified()
      await router.push({ name: routeNames.user })
      break
  }
}

onMounted(async () => {
  if (user.value) {
    await sendConfirmationCodeTestRequest(user.value.email, confirmationCodePurposes.EMAIL_VERIFICATION)
      .then(async (response) => {
        await processVerificationResponse(response.data)
      })
      .catch((error) => {
        Log.error(LogTag.SYSTEM, 'Failed to resend confirmation code', error)
      })
  }
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

.code-instructions {
  text-align: center;
  font-size: clamp(0.8rem, 2vw, 1rem);
  letter-spacing: 0.02rem;
  text-wrap: balance;
  color: #fdfbff;
  padding: 0.5rem;
  margin: 0;
}

.code-instructions em {
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
