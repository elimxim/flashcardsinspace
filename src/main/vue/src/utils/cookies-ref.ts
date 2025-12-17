import { ref, watch } from 'vue'
import {
  loadSidebarExpandedFromCookies,
  setSidebarExpandedToCookies,
  loadQuizSessionIdFromCookies,
  saveQuizSessionIdToCookies,
  removeQuizSessionIdFromCookies,
  loadLoggingEnabledFromCookies,
  saveLoggingEnabledToCookies,
} from '@/utils/cookies.ts'

const sidebarExpandedCookie = ref(loadSidebarExpandedFromCookies())
const quizSessionIdCookie = ref(loadQuizSessionIdFromCookies())
const loggingEnabledCookie = ref(loadLoggingEnabledFromCookies())

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

watch(loggingEnabledCookie, (newVal) => {
  saveLoggingEnabledToCookies(newVal)
})

export {
  sidebarExpandedCookie,
  quizSessionIdCookie,
  loggingEnabledCookie,
}
