import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
// @ts-ignore
import HomeView from '@/views/HomeView.vue'
// @ts-ignore
import FlashcardsView from '@/views/FlashcardsView.vue'
// @ts-ignore
import LeitnerView from '@/views/LeitnerView.vue'
// @ts-ignore
import SupportView from '@/views/SupportView.vue'

// Define routes for the pages
const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'home',
    component: HomeView
  },
  {
    path: '/flashcards',
    name: 'flashcards',
    component: FlashcardsView
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
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
