<template>
  <div class="page-center-container">
    <div class="rocket-container">
      <img
        ref="lilrocket"
        class="lilrocket non-selectable non-draggable"
        alt="Lilrocket"/>
    </div>
    <div class="auth-container" style="margin-top: 0;">
      <form @submit.prevent="handleLogin">
        <input v-model="email" type="email" placeholder="Email" required/>
        <!-- todo ability to see the password -->
        <SecretInput v-model="password" placeholder="Password" required/>
        <button type="submit">Log In</button>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import '@/assets/css/shared.css'
import '@/assets/css/auth-container.css'
import SecretInput from '@/components/SecretInput.vue'
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from "@/stores/auth-store.ts"
import { routeNames } from "@/router/index.js"

const router = useRouter()
const authStore = useAuthStore()

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
  clayImg,
  toyImg,
  crochetImg
]

const lilrocket = ref<HTMLImageElement>()

function setRandomLilrocket() {
  if (lilrocket.value) {
    const idx = Math.floor(Math.random() * rockets.length)
    lilrocket.value.src = rockets[idx]
  }
}

onMounted(() => {
  setRandomLilrocket()
})

// <lilrocket

</script>

<style scoped>
.rocket-container {
  align-items: center;
  text-align: center;
  justify-content: center;
}

.lilrocket {
  height: 8em;
  margin-top: 5rem;
  animation: bounce-in 1s,
  shake 4s infinite 1s;
  cursor: pointer;
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
  animation: shake 1s infinite;
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

.lilrocket:active {
  animation: shake 0.3s infinite;

}
</style>
