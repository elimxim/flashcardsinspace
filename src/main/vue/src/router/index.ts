import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth-state.ts'
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
import SignupView from '@/views/auth/SignupView.vue'
// @ts-ignore
import LoginView from '@/views/auth/LoginView.vue'
// @ts-ignore
import LogoutView from '@/views/auth/LogoutView.vue'
// @ts-ignore
import PasswordResetView from '@/views/auth/PasswordResetView.vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'home',
    component: HomeView
  },
  {
    path: '/flashcards',
    name: 'flashcards',
    component: FlashcardsView,
    meta: {
      requiresAuth: true,
    }
  },
  {
    path: '/leitner',
    name: 'leitner',
    component: LeitnerView
  },
  {
    path: '/support',
    name: 'support',
    component: SupportView
  },
  {
    path: '/signup',
    name: 'signup',
    component: SignupView,
  },
  {
    path: '/login',
    name: 'login',
    component: LoginView,
  },
  {
    path: '/logout',
    name: 'logout',
    component: LogoutView,
  },
  {
    path: '/password-reset',
    name: 'passwordReset',
    component: PasswordResetView,
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()
  const { isAuthenticated } = storeToRefs(authStore)

  if (to.meta.requiresAuth && !isAuthenticated.value) {
    next({ name: 'signup' })
  } else {
    next()
  }
})

export default router
