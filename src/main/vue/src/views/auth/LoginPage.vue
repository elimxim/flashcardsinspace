<template>
  <div class="page-center-container">
    <div
      ref="lilrocket"
      class="lilrocket non-selectable non-draggable"
      :class="{ 'initial-bounce': isBouncing }"
      @mousedown="onMouseDown"
      @mouseup="onMouseUp"
      @mouseleave="onMouseUp"
    >
      <SmartPicture
        alt="Lilrocket"
        :base="currRocketImg"
        :formats="['webp']"
        fallbackExt="png"
        :dimensions="{width: 128, height: 128}"
        dpr
      />
    </div>
    <div
      class="auth-container transition--border-color"
      :class="{ 'auth-container--error': loginFailed }"
    >
      <form @submit.prevent="login" class="auth-container__form" novalidate>
        <input
          class="input auth-container__form__input transition--border-color"
          :class="{ 'input--error': userEmailInvalid }"
          v-model="userEmail"
          type="email"
          name="username"
          autocomplete="username"
          :placeholder="userEmailNotSet ? 'Email is required' : 'Email'"
        />
        <span v-if="userEmailFormatWrong" class="auth-container__form__error">
          This email seems to be lost in a cosmic dust cloud. Please check the format
        </span>
        <SecretInput
          v-model="userPassword"
          class="auth-container__form__input"
          :class="{ 'input--error': userPasswordInvalid }"
          name="password"
          automoplete="current-password"
          :placeholder="userPasswordNotSet ? 'Password is required' : 'Password'"
        />
        <span v-if="userPasswordMaxLengthInvalid"
              class="auth-container__form__error transition--opacity">
          This secret is expanding faster than the universe! Please keep it under 64 characters
        </span>
        <button
          ref="loginButton"
          type="submit"
          class="button button--login auth-container__form__button transition--bg-color"
          :disabled="formInvalid"
          :class="{ 'button--disabled': formInvalid }"
        >
          Log In
        </button>
        <p class="auth-container__form__link">
          New to the galaxy?
          <router-link to="/signup">Signup</router-link>
        </p>
      </form>
    </div>
  </div>
  <SpaceToast/>
</template>

<script setup lang="ts">
import SecretInput from '@/components/SecretInput.vue'
import SpaceToast from '@/components/SpaceToast.vue'
import SmartPicture from '@/components/SmartPicture.vue'
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { useVuelidate } from '@vuelidate/core'
import { email, maxLength, required } from '@vuelidate/validators'
import { useRouter } from 'vue-router'
import { useAuthStore } from "@/stores/auth-store.ts"
import { routeNames } from "@/router/index.js"
import { sendLoginRequest } from '@/api/auth-client.ts'
import { useSpaceToaster } from '@/stores/toast-store.ts'

const router = useRouter()
const authStore = useAuthStore()
const toaster = useSpaceToaster()

const userEmail = ref('')
const userPassword = ref('')
const isBouncing = ref(true)
const loginFailed = ref(false)

const loginButton = ref<HTMLDivElement>()

const $v = useVuelidate({
  userEmail: { required, email },
  userPassword: { required, maxLength: maxLength(64) }
}, {
  userEmail: userEmail,
  userPassword: userPassword,
})

const formInvalid = computed(() => $v.value.$errors.length > 0)

const userEmailInvalid = computed(() => $v.value.userEmail.$errors.length > 0)
const userEmailNotSet = computed(() =>
  $v.value.userEmail.$errors.find(v => v.$validator === 'required') !== undefined
)
const userEmailFormatWrong = computed(() =>
  $v.value.userEmail.$errors.find(v => v.$validator === 'email') !== undefined
)
const userPasswordInvalid = computed(() => $v.value.userPassword.$errors.length > 0)
const userPasswordNotSet = computed(() =>
  $v.value.userPassword.$errors.find(v => v.$validator === 'required') !== undefined
)
const userPasswordMaxLengthInvalid = computed(() =>
  $v.value.userPassword.$errors.find(v => v.$validator === 'maxLength') !== undefined
)

watch(userEmail, () => {
  loginFailed.value = false
  if (userEmailInvalid.value) {
    $v.value.userEmail.$reset()
  }
})

watch(userPassword, () => {
  loginFailed.value = false
  if (userPasswordInvalid.value) {
    $v.value.userPassword.$reset()
  }
})

async function login() {
  console.log('Logging in...')
  toaster.dismiss()
  loginFailed.value = false
  $v.value.$touch()
  if ($v.value.$invalid) {
    console.log('Form is invalid')
    loginButton.value?.blur()
    return
  }

  await sendLoginRequest(userEmail.value, userPassword.value).then(async (response) => {
    console.log('Successfully logged in: ', authStore.user?.id)
    authStore.setUser(response.data)
    await router.push({ name: routeNames.flashcards })
  }).catch(error => {
    loginFailed.value = true

    if (error.response?.status === 400) {
      toaster.bakeError('Anomaly detected', error.response?.data)
    } else if (error.response?.status === 401) {
      toaster.bakeError('We couldn\'t recognize you', error.response?.data)
    } else if (error.response?.status === 404) {
      toaster.bakeError('We couldn\'t find you in our system', error.response?.data)
    } else {
      toaster.bakeError('System error', error.response?.data)
    }

    console.error('Failed to log in: ', error)
  })
}

// lilrocket>

const rockets = [
  "clay",
  "crochet",
  "original",
  "toy",
  "chromium",
  "diamond",
  "bluegem",
  "glass",
]

const lilrocket = ref<HTMLElement>()
const currRocketIdx = ref(0)
const currRocketImg = computed(() => `/assets/rocket/${rockets[currRocketIdx.value]}`)
let pressTimer: number | undefined

function setRandomLilrocket() {
  currRocketIdx.value = Math.floor(Math.random() * rockets.length)
}

function setNextLilrocket() {
  let nextIdx = currRocketIdx.value + 1
  if (nextIdx >= rockets.length) {
    nextIdx = 0
  }
  currRocketIdx.value = nextIdx
}

function onMouseDown() {
  pressTimer = window.setTimeout(() => {
    if (!lilrocket.value) return
    lilrocket.value.classList.add('fly-away')
    setTimeout(() => {
      if (lilrocket.value) {
        lilrocket.value.classList.remove('fly-away')
      }
      nextTick(() => {
        setNextLilrocket()
        setBouncingTimeout()
      })
    }, 4000)
  }, 500)
}

function onMouseUp() {
  if (lilrocket.value) {
    lilrocket.value.classList.remove('powering-up')
  }
  clearTimeout(pressTimer)
}

function setBouncingTimeout() {
  isBouncing.value = true
  setTimeout(() => {
    isBouncing.value = false
  }, 1000)
}

onMounted(() => {
  setRandomLilrocket()
  setBouncingTimeout()
})

// <lilrocket

</script>

<style scoped>
.lilrocket {
  position: relative;
  width: fit-content;
  height: fit-content;
  cursor: pointer;
  animation: shake 4s infinite ease-in-out;
  z-index: 1010;
}

.lilrocket.initial-bounce {
  animation: bounce-in 1s ease-in-out,
  shake 4s infinite 1s ease-in-out;
}

@keyframes bounce-in {
  0% {
    transform: scale(0);
  }
  50% {
    transform: scale(1.1);
  }
  100% {
    transform: scale(1);
  }
}

.lilrocket:hover {
  animation: none ease-in-out;
}

@keyframes shake {
  0% {
    transform: translate(2px, 3px) rotate(1deg);
  }
  10% {
    transform: translate(-3px, -5px) rotate(-3deg);
  }
  20% {
    transform: translate(-5px, 2px) rotate(2deg);
  }
  30% {
    transform: translate(5px, 4px) rotate(-1deg);
  }
  40% {
    transform: translate(3px, -3px) rotate(3deg);
  }
  50% {
    transform: translate(-3px, 5px) rotate(-2deg);
  }
  60% {
    transform: translate(-5px, 3px) rotate(1deg);
  }
  70% {
    transform: translate(5px, 3px) rotate(-3deg);
  }
  80% {
    transform: translate(-3px, -3px) rotate(2deg);
  }
  90% {
    transform: translate(3px, 5px) rotate(-1deg);
  }
  100% {
    transform: translate(2px, 3px) rotate(1deg);
  }
}

.lilrocket.fly-away {
  animation: fly-away 4s ease-in-out forwards;
}

@keyframes fly-away {
  /* 1. Smoothly goes to the vertical position */
  0% {
    transform: rotate(0deg);
  }
  10% {
    transform: rotate(70deg);
  }

  /* 2. Powering-up */
  12% {
    transform: translate(1px, 1px) rotate(71deg) scale(1.0);
  }
  14% {
    transform: translate(-1px, -2px) rotate(69deg) scale(1.02);
  }
  16% {
    transform: translate(-3px, 0px) rotate(71deg) scale(1.04);
  }
  18% {
    transform: translate(3px, 2px) rotate(70deg) scale(1.06);
  }
  20% {
    transform: translate(1px, -1px) rotate(71deg) scale(1.08);
  }
  22% {
    transform: translate(-1px, 2px) rotate(69deg) scale(1.1);
  }
  24% {
    transform: translate(-3px, 1px) rotate(70deg) scale(1.08);
  }
  26% {
    transform: translate(3px, 1px) rotate(69deg) scale(1.06);
  }
  28% {
    transform: translate(-1px, -1px) rotate(71deg) scale(1.04);
  }
  30% {
    transform: translate(1px, 2px) rotate(70deg) scale(1.02);
  }
  32% {
    transform: translate(1px, -2px) rotate(69deg) scale(1.0);
  }
  34% {
    transform: translate(3px, 1px) rotate(69deg) scale(1.06);
  }
  36% {
    transform: translate(-1px, -1px) rotate(71deg) scale(1.04);
  }
  38% {
    transform: translate(1px, 2px) rotate(69deg) scale(1.02);
  }
  40% {
    transform: translate(1px, -2px) rotate(70deg) scale(1.0);
  }

  /* 3. Flies to the top of the screen */
  55% {
    transform: translateY(-55vh) rotate(70deg) scale(1.0);
  }

  /* 5. Teleportation to the right and awaiting */
  55.01% {
    transform: translateX(65vw) translateY(0vh) rotate(-20deg) scale(1.0);
  }
  65% {
    transform: translateX(65vw) translateY(0vh) rotate(-20deg) scale(1.0);
  }

  /* 4. Flies from the right to the left */
  100% {
    transform: translateX(-65vw) rotate(-20deg) scale(1.0);
  }
}

</style>
