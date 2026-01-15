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
      <form class="auth-form" novalidate @submit.prevent="changePassword">
        <SmartInput
          id="password"
          v-model="newPassword"
          type="password"
          name="new-password"
          autocomplete="new-password"
          :invalid="newPasswordInvalid"
          placeholder="Password"
        />
        <ErrorText
          :errors="[
        {
          when: newPasswordMinLengthInvalid,
          text: 'Your password must be stronger than a piece of space junk. Please use 6 or more characters'
        },
        {
          when: newPasswordMaxLengthInvalid,
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
          text="Submit"
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
import SpaceToast from '@/components/SpaceToast.vue'
import SmartInput from '@/components/SmartInput.vue'
import SmartButton from '@/components/SmartButton.vue'
import ErrorText from '@/components/ErrorText.vue'
import { computed, ref } from 'vue'
import { useVuelidate } from '@vuelidate/core'
import { helpers, maxLength, minLength, required } from '@vuelidate/validators'
import { Log, LogTag } from '@/utils/logger.ts'
import { sendPasswordResetRequest } from '@/api/auth-client.ts'
import router, { routeNames } from '@/router'
import { userApiErrors } from '@/api/user-api-error.ts'
import { useSpaceToaster } from '@/stores/toast-store.ts'

const toaster = useSpaceToaster()

const newPassword = ref('')
const confirmedPassword = ref('')

const passwordConfirmed = helpers.withParams(
  { type: 'confirmed' },
  (value) => {
    if (!value) return true
    return value === newPassword.value
  }
)

const $v = useVuelidate({
  newPassword: {
    required,
    minLength: minLength(6),
    maxLength: maxLength(64),
  },
  confirmPassword: { required, passwordConfirmed },
}, {
  newPassword: newPassword,
  confirmPassword: confirmedPassword,
})

const formInvalid = computed(() => $v.value.$errors.length > 0)
const newPasswordInvalid = computed(() => $v.value.newPassword.$errors.length > 0)
const newPasswordMinLengthInvalid = computed(() =>
  $v.value.newPassword.$errors.find(v => v.$validator === 'minLength') !== undefined
)
const newPasswordMaxLengthInvalid = computed(() =>
  $v.value.newPassword.$errors.find(v => v.$validator === 'maxLength') !== undefined
)
const confirmPasswordInvalid = computed(() =>
  $v.value.confirmPassword.$errors.length > 0
)
const confirmPasswordMismatch = computed(() =>
  $v.value.confirmPassword.$errors.find(v => v.$validator === 'passwordConfirmed') !== undefined
)

async function changePassword() {
  $v.value.$touch()
  if (formInvalid.value) {
    Log.error(LogTag.LOGIC, 'The form is invalid')
    return
  }

  await sendPasswordResetRequest(newPassword.value)
    .then(() => {
      router.push({ name: routeNames.login })
    })
    .catch((error) => {
      Log.error(LogTag.LOGIC, 'Failed to reset the password', error)
      toaster.bakeError(userApiErrors.PASSWORD_RESET__FAILED, error.response?.data)
    })
}

</script>
