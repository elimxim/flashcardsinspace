import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth-store.ts'
import { storeToRefs } from 'pinia'
import HomeView from '@/views/HomeView.vue'
import ControlPanel from '@/views/ControlPanel.vue'
import LeitnerView from '@/views/LeitnerView.vue'
import SupportView from '@/views/SupportView.vue'
import UserView from '@/views/UserView.vue'
import SignupPage from '@/views/auth/SignupPage.vue'
import LoginPage from '@/views/auth/LoginPage.vue'
import LogoutPage from '@/views/auth/LogoutPage.vue'
import PasswordResetView from '@/views/auth/PasswordResetView.vue'
import ReviewPage from '@/views/ReviewPage.vue'
import { toStage } from '@/core-logic/stage-logic.ts'

export const routeNames = {
  base: 'base',
  home: 'home',
  controlPanel: 'controlPanel',
  leitner: 'leitner',
  user: 'user',
  support: 'support',
  signup: 'signup',
  login: 'login',
  logout: 'logout',
  passwordReset: 'passwordReset',
  review: 'review',
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
    component: HomeView
  },
  {
    path: '/leitner',
    name: routeNames.leitner,
    component: LeitnerView
  },
  {
    path: '/support',
    name: routeNames.support,
    component: SupportView
  },
  {
    path: '/user',
    name: routeNames.user,
    component: UserView,
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
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, _, next) => {
  const authStore = useAuthStore()
  const { isAuthenticated } = storeToRefs(authStore)

  if (to.meta.requiresAuth && !isAuthenticated.value) {
    next({ name: routeNames.signup })
  } else {
    next()
  }
})

export default router
