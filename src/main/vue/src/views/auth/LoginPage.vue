<template>
  <div class="page-center-container">
    <img
      ref="lilrocket"
      class="lilrocket non-selectable non-draggable"
      :class="{ 'initial-bounce': isBouncing }"
      alt="Lilrocket"
      @mousedown="onMouseDown"
      @mouseup="onMouseUp"
      @mouseleave="onMouseUp"
    />
    <div class="auth-container">
      <form @submit.prevent="handleLogin">
        <input v-model="email" type="email" placeholder="Email" required/>
        <SecretInput v-model="password" placeholder="Password" required/>
        <button type="submit">Log In</button>
        <p>
          New to the galaxy?
          <router-link to="/signup">Signup</router-link>
        </p>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import '@/assets/css/shared.css'
import '@/assets/css/auth-container.css'
import SecretInput from '@/components/SecretInput.vue'
import { nextTick, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from "@/stores/auth-store.ts"
import { routeNames } from "@/router/index.js"

const router = useRouter()
const authStore = useAuthStore()
const isBouncing = ref(true)

// todo validate inputs and show errors
const email = ref('')
const password = ref('')

async function handleLogin() {
  try {
    await authStore.login(email.value, password.value)
    console.log('Successfully logged in: ', authStore.user?.id)
    await router.push({ name: routeNames.flashcards })
  } catch (error) {
    // todo show the error to the user
    console.log('Failed to log in: ', error)
  }
}

// lilrocket>

// @ts-ignore
import clayImg from '@/assets/rocket/clay.png'
// @ts-ignore
import crochetImg from '@/assets/rocket/crochet.png'
// @ts-ignore
import originalImg from '@/assets/rocket/original.png'
// @ts-ignore
import toyImg from '@/assets/rocket/toy.png'

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
  if (lilrocket.value) {
    lilrocket.value.classList.add('powering-up')
  }

  pressTimer = window.setTimeout(() => {
    if (!lilrocket.value) return

    lilrocket.value.classList.remove('powering-up')
    lilrocket.value.classList.add('fly-away')

    setTimeout(() => {
      if (lilrocket.value) {
        lilrocket.value.classList.remove('fly-away')
      }
      nextTick(() => {
        setNextLilrocket()
        setBouncingTimeout()
      })
    }, 1200) // Corresponds to the 1.2s duration of fly-away
  }, 800)
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
/* Base class has only the continuous shake animation */
.lilrocket {
  height: 8em;
  animation: shake 4s infinite ease-in-out;
  cursor: pointer;
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
  animation: powering-up 1s infinite ease-in-out;
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

.lilrocket.powering-up {
  animation: powering-up 0.2s infinite;
}

@keyframes powering-up {
  0% {
    transform: translate(1px, 1px) rotate(0deg) scale(1.0);
  }
  10% {
    transform: translate(-1px, -2px) rotate(-1deg) scale(1.02);
  }
  20% {
    transform: translate(-3px, 0px) rotate(1deg) scale(1.04);
  }
  30% {
    transform: translate(3px, 2px) rotate(0deg) scale(1.06);
  }
  40% {
    transform: translate(1px, -1px) rotate(1deg) scale(1.08);
  }
  50% {
    transform: translate(-1px, 2px) rotate(-1deg) scale(1.1);
  }
  60% {
    transform: translate(-3px, 1px) rotate(0deg) scale(1.08);
  }
  70% {
    transform: translate(3px, 1px) rotate(-1deg) scale(1.06);
  }
  80% {
    transform: translate(-1px, -1px) rotate(1deg) scale(1.04);
  }
  90% {
    transform: translate(1px, 2px) rotate(0deg) scale(1.02);
  }
  100% {
    transform: translate(1px, -2px) rotate(-1deg) scale(1.0);
  }
}

.lilrocket.fly-away {
  animation: fly-away 1.2s ease-in forwards;
}

@keyframes fly-away {
  0% {
    transform: translate(3px, 2px) rotate(0deg) scale(1.2);
    opacity: 1;
  }
  10% {
    transform: translateY(-10vh) translateX(5vw) rotate(10deg) scale(1.1);
  }
  100% {
    transform: translateY(-200vh) translateX(20vw) rotate(180deg) scale(0);
    opacity: 0;
  }
}

</style>
