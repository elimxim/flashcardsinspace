<template>
  <div class="page-center-container">
    <img
      alt="Lilrocket"
      ref="lilrocket"
      class="lilrocket non-selectable non-draggable"
      :class="{ 'initial-bounce': isBouncing }"
      @mousedown="onMouseDown"
      @mouseup="onMouseUp"
      @mouseleave="onMouseUp"
    />
    <div
      class="auth-container transition--border-color"
      :class="{ 'auth-container--error': loginFailed }">
      <form @submit.prevent="login">
        <input
          class="input transition--border-color"
          :class="{ 'input-error': userEmailInvalid }"
          v-model="userEmail"
          :placeholder="userEmailRequired ? 'Email is required' : 'Email'"/>
        <p class="auth-container__p--error" v-for="error of $v.userEmail.$errors" :key="error.$uid">
          <span v-if="error.$validator === 'email'">This email seems to be lost in a cosmic dust cloud. Please check the format</span>
        </p>
        <SecretInput
          v-model="userPassword"
          :class="{ 'input-error': userPasswordInvalid }"
          :placeholder="userPasswordRequired ? 'Password is required' : 'Password'"/>
        <button
          type="submit"
          class="button button--login transition--bg-color">
          Log In
        </button>
        <p class="auth-container__p--link">
          New to the galaxy?
          <router-link to="/signup">Signup</router-link>
        </p>
      </form>
    </div>
  </div>
  <SpaceToast/>
</template>

<script setup lang="ts">
import SpaceToast from '@/components/SpaceToast.vue'
import SecretInput from '@/components/SecretInput.vue'
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { useVuelidate } from '@vuelidate/core'
import { email, required } from '@vuelidate/validators'
import { useRouter } from 'vue-router'
import { useAuthStore } from "@/stores/auth-store.ts"
import { routeNames } from "@/router/index.js"
import clayImg from '@/assets/rocket/clay.png'
import crochetImg from '@/assets/rocket/crochet.png'
import originalImg from '@/assets/rocket/original.png'
import toyImg from '@/assets/rocket/toy.png'
import { sendLoginRequest } from '@/api/auth-client.ts'
import { ToastType, useSpaceToaster } from '@/stores/toast-store.ts'

const router = useRouter()
const authStore = useAuthStore()
const toaster = useSpaceToaster()

const userEmail = ref('')
const userPassword = ref('')
const isBouncing = ref(true)
const loginFailed = ref(false)

const $v = useVuelidate({
  userEmail: { required, email },
  userPassword: { required }
}, {
  userEmail: userEmail,
  userPassword: userPassword,
})

const userEmailInvalid = computed(() => $v.value.userEmail.$errors.length > 0)
const userEmailRequired = computed(() =>
  $v.value.userEmail.$errors.find(v => v.$validator === 'required') !== undefined
)
const userPasswordInvalid = computed(() => $v.value.userPassword.$errors.length > 0)
const userPasswordRequired = computed(() =>
  $v.value.userPassword.$errors.find(v => v.$validator === 'required') !== undefined
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
  toaster.dismiss()
  loginFailed.value = false
  $v.value.$touch()
  if ($v.value.$invalid) {
    console.log('Invalid form')
    return
  }

  await sendLoginRequest(userEmail.value, userPassword.value).then(async (response) => {
    console.log('Successfully logged in: ', authStore.user?.id)
    authStore.setUser(response.data.user)
    await router.push({ name: routeNames.flashcards })
  }).catch(error => {
    loginFailed.value = true

    if (error.response?.status === 500) {
      toaster.bake({
        type: ToastType.ERROR,
        title: 'The main server is not filling OK. Please contact the team',
        message: error.response?.data?.message,
        duration: 10000,
      })
    } else if (error.response?.status === 400) {
      toaster.bake({
        type: ToastType.ERROR,
        title: 'Invalid credentials',
        message: error.response?.data?.message,
        duration: 10000,
      })
    } else {
      toaster.bake({
        type: ToastType.ERROR,
        title: 'Failed to log in.',
        duration: 10000,
      })
    }

    console.error('Failed to log in: ', error)
  })
}

// lilrocket>

const rockets = [
  clayImg,
  crochetImg,
  originalImg,
  toyImg,
]

const lilrocket = ref<HTMLImageElement>()
const currRocketIdx = ref(0)
let pressTimer: number | undefined

function setRandomLilrocket() {
  if (!lilrocket.value) return
  const idx = Math.floor(Math.random() * rockets.length)
  currRocketIdx.value = idx
  lilrocket.value.src = rockets[idx]
}

function setNextLilrocket() {
  if (!lilrocket.value) return
  let nextIdx = currRocketIdx.value + 1
  if (nextIdx >= rockets.length) {
    nextIdx = 0
  }
  currRocketIdx.value = nextIdx
  lilrocket.value.src = rockets[nextIdx]
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
  height: 8em;
  animation: shake 4s infinite ease-in-out;
  cursor: pointer;
  z-index: 100;
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
