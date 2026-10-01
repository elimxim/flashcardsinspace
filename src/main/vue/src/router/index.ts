import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth-store.ts'
import { storeToRefs } from 'pinia'
import HomePage from '@/pages/HomePage.vue'
import ControlPanel from '@/pages/ControlPanel.vue'
import GetHelpPage from '@/pages/GetHelpPage.vue'
import EmotionalSupportPage from '@/pages/EmotionalSupportPage.vue'
import WebsiteGuidePage from '@/pages/WebsiteGuidePage.vue'
import UserPage from '@/pages/UserPage.vue'
import SignupPage from '@/pages/auth/SignupPage.vue'
import LoginPage from '@/pages/auth/LoginPage.vue'
import LogoutPage from '@/pages/auth/LogoutPage.vue'
import PasswordResetPage from '@/pages/auth/PasswordResetPage.vue'
import EmailConfirmationPage from '@/pages/auth/EmailConfirmationPage.vue'
import ReviewRoutingPage from '@/pages/review/ReviewRoutingPage.vue'
import CodeVerificationPage from '@/pages/CodeVerificationPage.vue'
import { toLearningStages } from '@/core-logic/stage-logic.ts'
import { loadUserSignedUpFromCookies } from '@/utils/cookies.ts'
import { parseNumber } from '@/utils/utils.ts'
import { parseVerificationType } from '@/core-logic/user-logic.ts'

export const routeNames = {
  base: 'base',
  home: 'home',
  controlPanel: 'controlPanel',
  user: 'user',
  getHelp: 'getHelp',
  signup: 'signup',
  login: 'login',
  logout: 'logout',
  emailConfirmation: 'emailConfirmation',
  passwordReset: 'passwordReset',
  review: 'review',
  emotionalSupport: 'emotionalSupport',
  websiteGuide: 'websiteGuide',
  codeVerification: 'codeVerification',
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
    component: HomePage,
  },
  {
    path: '/get-help',
    name: routeNames.getHelp,
    component: GetHelpPage,
  },
  {
    path: '/website-guide',
    name: routeNames.websiteGuide,
    component: WebsiteGuidePage,
  },
  {
    path: '/emotional-support',
    name: routeNames.emotionalSupport,
    component: EmotionalSupportPage,
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
    component: PasswordResetPage,
  },
  {
    path: '/email-confirmation',
    name: routeNames.emailConfirmation,
    component: EmailConfirmationPage,
  },
  {
    path: '/review',
    name: routeNames.review,
    component: ReviewRoutingPage,
    props: (route) => ({
      sessionType: route.query.sessionType,
      sessionId: parseNumber(route.query.sessionId),
      stages: toLearningStages(route.query.stages),
      reversed: route.query.reversed === 'true',
    }),
    meta: {
      requiresAuth: true,
      requiresEmailVerified: true,
    },
  },
  {
    path: '/code-verification',
    name: routeNames.codeVerification,
    component: CodeVerificationPage,
    props: (route) => ({
      type: parseVerificationType(route.query.type),
    }),
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  const authStore = useAuthStore()
  const { isAuthenticated, isEmailVerified } = storeToRefs(authStore)

  if (to.meta.requiresAuth) {
    if (!isAuthenticated.value) {
      return loadUserSignedUpFromCookies()
        ? { name: routeNames.login }
        : { name: routeNames.signup }
    }
    if (to.meta.requiresEmailVerified && !isEmailVerified.value) {
      return { name: routeNames.codeVerification }
    }
    return
  }

  // auth is not required below

  // goes directly to login/signup pages
  if (to.name === routeNames.login || to.name === routeNames.signup) {
    if (!isAuthenticated.value) return
    return isEmailVerified.value ? { name: routeNames.user } : { name: routeNames.codeVerification }
  }

  // goes directly to code verification page
  if (to.name === routeNames.codeVerification && isEmailVerified.value) {
    return { name: routeNames.user }
  }

  // other pages are accessible
})

export default router

function isOnAuthPage() {
  return (
    router.currentRoute.value.name === routeNames.login ||
    router.currentRoute.value.name === routeNames.signup
  )
}

export async function redirectToLoginPage() {
  if (!isOnAuthPage()) {
    await router.push({ name: routeNames.login })
  }
}
