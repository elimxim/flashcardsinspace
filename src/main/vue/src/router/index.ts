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
import { toLearningStages } from '@/core-logic/stage-logic.ts'
import { loadUserSignedUpFromCookies } from '@/utils/cookies.ts'
import { parseNumber } from '@/utils/utils.ts'

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
    redirect: () => {
      const authStore = useAuthStore()
      return authStore.isAuthenticated
        ? { name: routeNames.controlPanel }
        : { name: routeNames.home }
    },
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
      sessionType: route.query.sessionType,
      sessionId: parseNumber(route.query.sessionId),
      stages: toLearningStages(route.query.stages),
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
    const isUserSignedUp = loadUserSignedUpFromCookies()
    if (isUserSignedUp) {
      next({ name: routeNames.login })
    } else {
      next({ name: routeNames.signup })
    }
  } else if (to.name === routeNames.login || to.name === routeNames.signup) {
    if (isAuthenticated.value) {
      next({ name: routeNames.user })
    } else {
      next()
    }
  } else {
    next()
  }
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
