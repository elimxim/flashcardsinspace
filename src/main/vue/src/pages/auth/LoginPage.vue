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
      ref="lilrocket"
      class="lilrocket"
      @click="onClickRocket"
    >
      <SmartPicture
        alt="Lilrocket"
        :base="currRocketImg"
        :formats="['webp']"
        fallback-ext="png"
        :dimensions="{width: 128, height: 128}"
        non-interactive
        dpr
      />
    </div>
    <div
      class="auth-container"
      :class="{ 'auth-container--error': loginFailed }"
    >
      <form class="auth-form" novalidate @submit.prevent="login">
        <SmartInput
          id="username"
          v-model="userEmail"
          type="email"
          name="username"
          autocomplete="username"
          :invalid="userEmailInvalid"
          placeholder="Email"
        />
        <ErrorText
          :when="userEmailFormatWrong"
          text="This email seems to be lost in a cosmic dust cloud. Please check the format"
        />
        <SmartInput
          id="password"
          v-model="userPassword"
          type="password"
          name="password"
          autocomplete="current-password"
          :invalid="userPasswordInvalid"
          placeholder="Password"
        />
        <ErrorText
          :when="userPasswordMaxLengthInvalid"
          text="This secret is expanding faster than the universe! Please keep it under 64 characters"
        />
        <SmartButton
          class="calm-button"
          text="Log in"
          type="submit"
          :disabled="formInvalid"
          auto-blur
          fill-width
        />
        <p class="auth-link">
          New to the galaxy?
          <router-link to="/signup">Signup</router-link>
        </p>
      </form>
    </div>
  </div>
  <SpaceToast/>
</template>

<script setup lang="ts">
import SmartInput from '@/components/SmartInput.vue'
import SmartButton from '@/components/SmartButton.vue'
import SpaceToast from '@/components/SpaceToast.vue'
import SmartPicture from '@/components/SmartPicture.vue'
import ErrorText from '@/components/ErrorText.vue'
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { useVuelidate } from '@vuelidate/core'
import { email, maxLength, required } from '@vuelidate/validators'
import { useRouter } from 'vue-router'
import { useAuthStore } from "@/stores/auth-store.ts"
import { routeNames } from "@/router"
import { sendLoginRequest } from '@/api/auth-client.ts'
import { useSpaceToaster } from '@/stores/toast-store.ts'
import { saveUserSignedUpToCookies } from '@/utils/cookies.ts'
import { Log, LogTag } from '@/utils/logger.ts'
import { userApiErrors } from '@/api/user-api-error.ts'

const router = useRouter()
const authStore = useAuthStore()
const toaster = useSpaceToaster()

const userEmail = ref('')
const userPassword = ref('')
const loginFailed = ref(false)
const rocketVerticalAngle = ref(50)
const rocketVerticalAngleDeg = computed(() => `${rocketVerticalAngle.value}deg`)
const rocketVerticalAnglePlusOneDeg = computed(() => `${rocketVerticalAngle.value + 1}deg`)
const rocketVerticalAngleMinusOneDeg = computed(() => `${rocketVerticalAngle.value - 1}deg`)
const rocketHorizontalAngle = ref(-40)
const rocketHorizontalAngleDeg = computed(() => `${rocketHorizontalAngle.value}deg`)

const $v = useVuelidate({
  userEmail: { required, email },
  userPassword: {
    required,
    maxLength: maxLength(64),
  }
}, {
  userEmail: userEmail,
  userPassword: userPassword,
})

const formInvalid = computed(() => $v.value.$errors.length > 0)
const userEmailInvalid = computed(() => $v.value.userEmail.$errors.length > 0)
const userEmailFormatWrong = computed(() =>
  $v.value.userEmail.$errors.find(v => v.$validator === 'email') !== undefined
)
const userPasswordInvalid = computed(() => $v.value.userPassword.$errors.length > 0)
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
  Log.log(LogTag.LOGIC, 'Logging in...')
  toaster.dismiss()
  loginFailed.value = false
  $v.value.$touch()
  if (formInvalid.value) {
    Log.error(LogTag.LOGIC, 'Form is invalid')
    return
  }

  await sendLoginRequest(userEmail.value, userPassword.value).then(async (response) => {
    Log.log(LogTag.LOGIC, 'Successfully logged in: ', authStore.user?.id)
    authStore.setUser(response.data)
    saveUserSignedUpToCookies(true)
    await router.push({ name: routeNames.controlPanel })
  }).catch(error => {
    loginFailed.value = true

    if (error.response?.status === 400) {
      toaster.bakeError(userApiErrors.AUTH_BAD_DATA, error.response?.data)
    } else if (error.response?.status === 401) {
      toaster.bakeError(userApiErrors.USER__UNAUTHORIZED, error.response?.data)
    } else if (error.response?.status === 404) {
      toaster.bakeError(userApiErrors.USER__NOT_FOUND, error.response?.data)
    } else {
      toaster.bakeError(userApiErrors.AUTH_FAILED, error.response?.data)
    }

    Log.error(LogTag.LOGIC, 'Failed to log in: ', error)
  })
}

// lilrocket>

const rockets = [
  "authentic",
]

const lilrocket = ref<HTMLElement>()
const currRocketIdx = ref(0)
const currRocketImg = computed(() => `/assets/rocket/${rockets[currRocketIdx.value]}`)
let flyTimeout: number | undefined

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

function onClickRocket() {
  if (flyTimeout !== undefined) return
  flyTimeout = window.setTimeout(() => {
    if (!lilrocket.value) return
    lilrocket.value.classList.add('fly-away')
    setTimeout(() => {
      lilrocket.value?.classList?.remove('fly-away')
      nextTick(() => {
        setNextLilrocket()
        flyTimeout = undefined
      })
    }, 4000)
  }, 1000)
}

onMounted(() => {
  setRandomLilrocket()
})

// <lilrocket

</script>

<style scoped>
.lilrocket {
  position: relative;
  width: fit-content;
  height: fit-content;
  cursor: default;
  animation: shake 4s infinite ease-in-out;
  margin: 0 auto;
  z-index: 100;
  -webkit-tap-highlight-color: transparent;
}

@media (hover: hover) {
  .lilrocket:hover {
    animation: none ease-in-out;
  }
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
    transform: rotate(0);
  }
  10% {
    transform: rotate(v-bind(rocketVerticalAngleDeg));
  }

  /* 2. Powering-up */
  12% {
    transform: translate(1px, 1px) rotate(v-bind(rocketVerticalAnglePlusOneDeg)) scale(1.0);
  }
  14% {
    transform: translate(-1px, -2px) rotate(v-bind(rocketVerticalAngleMinusOneDeg)) scale(1.02);
  }
  16% {
    transform: translate(-3px, 0px) rotate(v-bind(rocketVerticalAnglePlusOneDeg)) scale(1.04);
  }
  18% {
    transform: translate(3px, 2px) rotate(v-bind(rocketVerticalAngleDeg)) scale(1.06);
  }
  20% {
    transform: translate(1px, -1px) rotate(v-bind(rocketVerticalAnglePlusOneDeg)) scale(1.08);
  }
  22% {
    transform: translate(-1px, 2px) rotate(v-bind(rocketVerticalAngleMinusOneDeg)) scale(1.1);
  }
  24% {
    transform: translate(-3px, 1px) rotate(v-bind(rocketVerticalAngleDeg)) scale(1.08);
  }
  26% {
    transform: translate(3px, 1px) rotate(v-bind(rocketVerticalAngleMinusOneDeg)) scale(1.06);
  }
  28% {
    transform: translate(-1px, -1px) rotate(v-bind(rocketVerticalAnglePlusOneDeg)) scale(1.04);
  }
  30% {
    transform: translate(1px, 2px) rotate(v-bind(rocketVerticalAngleDeg)) scale(1.02);
  }
  32% {
    transform: translate(1px, -2px) rotate(v-bind(rocketVerticalAngleMinusOneDeg)) scale(1.0);
  }
  34% {
    transform: translate(3px, 1px) rotate(v-bind(rocketVerticalAngleMinusOneDeg)) scale(1.06);
  }
  36% {
    transform: translate(-1px, -1px) rotate(v-bind(rocketVerticalAnglePlusOneDeg)) scale(1.04);
  }
  38% {
    transform: translate(1px, 2px) rotate(v-bind(rocketVerticalAngleMinusOneDeg)) scale(1.02);
  }
  40% {
    transform: translate(1px, -2px) rotate(v-bind(rocketVerticalAngleDeg)) scale(1.0);
  }

  /* 3. Flies to the top of the screen */
  55% {
    transform: translateY(-60vh) rotate(v-bind(rocketVerticalAngleDeg)) scale(1.0);
  }

  /* 5. Teleportation to the right and awaiting */
  55.01% {
    transform: translateX(70vw) translateY(0vh) rotate(v-bind(rocketHorizontalAngleDeg)) scale(1.0);
  }

  /* 6. Wait for a little bit */
  65% {
    transform: translateX(70vw) translateY(0vh) rotate(v-bind(rocketHorizontalAngleDeg)) scale(1.0);
  }

  /* 7. Flies from the right to the left */
  100% {
    transform: translateX(-70vw) rotate(v-bind(rocketHorizontalAngleDeg)) scale(1.0);
  }
}

</style>
