import { ref, watch } from 'vue'
import {
  loadSidebarExpandedFromCookies,
  setSidebarExpandedToCookies,
  loadQuizSessionIdFromCookies,
  saveQuizSessionIdToCookies,
  removeQuizSessionIdFromCookies,
} from '@/utils/cookies.ts'

const sidebarExpandedCookie = ref(loadSidebarExpandedFromCookies())
const quizSessionIdCookie = ref(loadQuizSessionIdFromCookies())

watch(sidebarExpandedCookie, (newVal) => {
  setSidebarExpandedToCookies(newVal)
})

watch(quizSessionIdCookie, (newVal) => {
  if (newVal) {
    saveQuizSessionIdToCookies(newVal)
  } else {
    removeQuizSessionIdFromCookies()
  }
})

export {
  sidebarExpandedCookie,
  quizSessionIdCookie,
}
