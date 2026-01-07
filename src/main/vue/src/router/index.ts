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
import ReviewRouter from '@/components/review/ReviewRouter.vue'
import LightspeedSchedulePage from '@/pages/LightspeedSchedulePage.vue'
import CodeConfirmationPage from '@/pages/CodeConfirmationPage.vue'
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
  codeConfirmation: 'codeConfirmation',
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
      requiresEmailVerified: true,
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
      requiresEmailVerified: true,
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
    component: ReviewRouter,
    props: (route) => ({
      sessionType: route.query.sessionType,
      sessionId: parseNumber(route.query.sessionId),
      stages: toLearningStages(route.query.stages),
    }),
    meta: {
      requiresAuth: true,
      requiresEmailVerified: true,
    },
  },
  {
    path: '/lightspeed-schedule',
    name: routeNames.lightspeedSchedule,
    component: LightspeedSchedulePage,
  },
  {
    path: '/code-confirmation',
    name: routeNames.codeConfirmation,
    component: CodeConfirmationPage,
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, _, next) => {
  const authStore = useAuthStore()
  const { isAuthenticated, isEmailVerified } = storeToRefs(authStore)

  if (to.meta.requiresAuth) {
    const isSignedUp = loadUserSignedUpFromCookies()
    if (isAuthenticated.value) {
      if (to.meta.requiresEmailVerified && !isEmailVerified.value) {
        next({ name: routeNames.codeConfirmation })
      } else {
        next()
      }
    } else if (isSignedUp) {
      next({ name: routeNames.login })
    } else {
      next({ name: routeNames.signup })
    }
    return
  }

  // auth is not required below

  // goes directly to login/signup pages
  if (to.name === routeNames.login || to.name === routeNames.signup) {
    if (isAuthenticated.value) {
      if (isEmailVerified.value) {
        next({ name: routeNames.user })
      } else {
        next({ name: routeNames.codeConfirmation })
      }
    } else {
      next()
    }
    return
  }

  // goes directly to code confirmation page
  if (to.name === routeNames.codeConfirmation) {
    if (isEmailVerified.value) {
      next({ name: routeNames.user })
    } else {
      next()
    }
    return
  }

  // other pages are accessible
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
