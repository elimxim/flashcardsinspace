import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth-store.ts'
import { storeToRefs } from 'pinia'
// @ts-ignore
import HomeView from '@/views/HomeView.vue'
// @ts-ignore
import FlashcardsView from '@/views/FlashcardsView.vue'
// @ts-ignore
import LeitnerView from '@/views/LeitnerView.vue'
// @ts-ignore
import SupportView from '@/views/SupportView.vue'
// @ts-ignore
import UserView from '@/views/UserView.vue'
// @ts-ignore
import SignupPage from '@/views/auth/SignupPage.vue'
// @ts-ignore
import LoginPage from '@/views/auth/LoginPage.vue'
// @ts-ignore
import LogoutView from '@/views/auth/LogoutView.vue'
// @ts-ignore
import PasswordResetView from '@/views/auth/PasswordResetView.vue'

export const routeNames = {
  base: 'base',
  home: 'home',
  flashcards: 'flashcards',
  leitner: 'leitner',
  user: 'user',
  support: 'support',
  signup: 'signup',
  login: 'login',
  logout: 'logout',
  passwordReset: 'passwordReset',
}

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: routeNames.base,
    redirect: { name: routeNames.home },
  },
  {
    path: '/flashcards',
    name: routeNames.flashcards,
    component: FlashcardsView,
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
    component: LogoutView,
  },
  {
    path: '/password-reset',
    name: routeNames.passwordReset,
    component: PasswordResetView,
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
