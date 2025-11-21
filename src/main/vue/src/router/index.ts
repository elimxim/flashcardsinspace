import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth-store.ts'
import { storeToRefs } from 'pinia'
import HomePage from '@/pages/HomePage.vue'
import ControlPanel from '@/pages/ControlPanel.vue'
import SupportPage from '@/pages/SupportPage.vue'
import UserPage from '@/pages/UserPage.vue'
import SignupPage from '@/pages/auth/SignupPage.vue'
import LoginPage from '@/pages/auth/LoginPage.vue'
import LogoutPage from '@/pages/auth/LogoutPage.vue'
import PasswordResetView from '@/pages/auth/PasswordResetView.vue'
import ReviewPage from '@/pages/ReviewPage.vue'
import LightspeedSchedulePage from '@/pages/LightspeedSchedulePage.vue'
import { toStage } from '@/core-logic/stage-logic.ts'
import { loadUserSignedUp } from '@/shared/cookies.ts'

export const routeNames = {
  base: 'base',
  home: 'home',
  controlPanel: 'controlPanel',
  user: 'user',
  support: 'support',
  signup: 'signup',
  login: 'login',
  logout: 'logout',
  passwordReset: 'passwordReset',
  review: 'review',
  lightspeedSchedule: 'lightspeedSchedule',
}

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: routeNames.base,
    redirect: { name: routeNames.home },
  },
  {
    path: '/control-panel',
    name: routeNames.controlPanel,
    component: ControlPanel,
    meta: {
      requiresAuth: true,
    },
  },
  {
    path: '/home',
    name: routeNames.home,
    component: HomePage
  },
  {
    path: '/support',
    name: routeNames.support,
    component: SupportPage,
  },
  {
    path: '/user',
    name: routeNames.user,
    component: UserPage,
    meta: {
      requiresAuth: true,
    },
  },
  {
    path: '/signup',
    name: routeNames.signup,
    component: SignupPage,
  },
  {
    path: '/login',
    name: routeNames.login,
    component: LoginPage,
  },
  {
    path: '/logout',
    name: routeNames.logout,
    component: LogoutPage,
  },
  {
    path: '/password-reset',
    name: routeNames.passwordReset,
    component: PasswordResetView,
  },
  {
    path: '/review',
    name: routeNames.review,
    component: ReviewPage,
    props: (route) => ({
      stage: toStage(route.query.stage),
    }),
    meta: {
      requiresAuth: true,
    },
  },
  {
    path: '/lightspeed-schedule',
    name: routeNames.lightspeedSchedule,
    component: LightspeedSchedulePage,
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, _, next) => {
  const authStore = useAuthStore()
  const { isAuthenticated } = storeToRefs(authStore)

  if (to.meta.requiresAuth && !isAuthenticated.value) {
    const isUserSignedUp = loadUserSignedUp()
    if (isUserSignedUp) {
      next({ name: routeNames.login })
    } else {
      next({ name: routeNames.signup })
    }
  } else {
    next()
  }
})

router.beforeEach(async (to, _, next) => {
  const authStore = useAuthStore()
  const { isAuthenticated } = storeToRefs(authStore)

  if (to.name === routeNames.login || to.name === routeNames.signup) {
    if (isAuthenticated.value) {
      next({ name: routeNames.user })
    } else {
      next()
    }
  }
  next()
})

export default router

function isOnAuthPage() {
  return router.currentRoute.value.name === routeNames.login || router.currentRoute.value.name === routeNames.signup
}

export async function redirectToLoginPage() {
  if (!isOnAuthPage()) {
    await router.push({ name: routeNames.login })
  }
}
