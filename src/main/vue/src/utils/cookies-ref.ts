import { ref, watch } from 'vue'
import {
  loadSelectedSetIdFromCookies,
  loadSidebarExpandedFromCookies,
  removeSelectedSetIdCookie,
  saveSelectedSetIdToCookies,
  setSidebarExpandedToCookies,
} from '@/utils/cookies.ts'

const sidebarExpandedCookie = ref(loadSidebarExpandedFromCookies())
const selectedSetIdCookie = ref(loadSelectedSetIdFromCookies())

watch(sidebarExpandedCookie, (newVal) => {
  setSidebarExpandedToCookies(newVal)
})

watch(selectedSetIdCookie, (newVal) => {
  if (newVal) {
    saveSelectedSetIdToCookies(newVal)
  } else {
    removeSelectedSetIdCookie()
  }
})

export {
  sidebarExpandedCookie,
  selectedSetIdCookie,
}
