<template>
  <div
    :class="[
      'page',
      'page--bg--light',
      'flex-column',
      'padding-auto',
      'scrollbar-hidden',
  ]">
    <Progressbar
      class="logout-progressbar"
      :duration="redirectDelay"
      :track-rounded="true"
      :bar-rounded="true"
      height="6px"
    />

    <section class="logout-card" aria-live="polite">
      <h1>Mission complete — you’re safely logged out</h1>
      <p>
        Your session has re-entered the atmosphere and touched down at Mission Control.
        Your progress is stowed securely in the cargo bay, ready for your next launch.
        When you’re ready to explore again, head back to the login pad to lift off.
      </p>
      <span class="logout-redirect">
        Preparing next launch window… redirecting to Login in {{ secondsLeft }}s
      </span>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth-store.ts'
import { routeNames } from '@/router'
import { sendLogoutRequest } from '@/api/auth-client.ts'
import Progressbar from '@/components/Progressbar.vue'

const router = useRouter()
const authStore = useAuthStore()

const redirectDelay = 10000
const secondsLeft = ref(Math.ceil(redirectDelay / 1000))
let redirectTimeout: number | undefined
let tickerInterval: number | undefined

function startCountdown() {
  const start = Date.now()
  tickerInterval = window.setInterval(() => {
    const elapsed = Date.now() - start
    const left = Math.max(0, redirectDelay - elapsed)
    secondsLeft.value = Math.ceil(left / 1000)
    if (left <= 0) {
      clearInterval(tickerInterval)
      tickerInterval = undefined
    }
  }, 200)
}

function scheduleRedirect() {
  redirectTimeout = window.setTimeout(goToLogin, redirectDelay)
}

function clearTimers() {
  if (redirectTimeout) {
    clearTimeout(redirectTimeout)
    redirectTimeout = undefined
  }
  if (tickerInterval) {
    clearInterval(tickerInterval)
    tickerInterval = undefined
  }
}

async function goToLogin() {
  clearTimers()
  await router.push({ name: routeNames.login })
}

onMounted(() => {
  console.info('Logging out...')
  sendLogoutRequest()
    .then(() => {
      console.info('Successfully logged out')
      authStore.resetUser()
    })
    .catch(error => {
      console.error('Failed to log out: ', error)
    })
    .finally(() => {
      startCountdown()
      scheduleRedirect()
    })
})

onBeforeUnmount(clearTimers)
</script>

<style scoped>
.logout-progressbar {
  flex: 1;
  width: 100%;
  --progressbar--from: var(--logout-progressbar--from);
  --progressbar--via: var(--logout-progressbar--via);
  --progressbar--to: var(--logout-progressbar--to);
  --progressbar--bg-color: var(--logout-progressbar--bg-color);
}

.logout-card {
  flex: 99;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  text-align: justify;
  padding: 0.25rem;
  background: transparent;
}

.logout-card h1 {
  font-size: clamp(1rem, 2vw, 1.5rem);
  font-weight: 600;
  letter-spacing: 0.02em;
}

.logout-card p {
  font-size: clamp(0.8rem, 2vw, 1rem);
  font-weight: 400;
  letter-spacing: 0.02em;
}

.logout-redirect {
  font-size: clamp(0.7rem, 1.5vw, 0.9rem);
  margin: 0.5rem 0 0 0;
  font-weight: 500;
  letter-spacing: 0.02em;
}

</style>
