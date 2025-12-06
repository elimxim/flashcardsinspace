import { ref, watch } from 'vue'
import {
  loadSidebarExpandedFromCookies,
  setSidebarExpandedToCookies
} from '@/utils/cookies.ts'

const sidebarExpandedCookie = ref(loadSidebarExpandedFromCookies())

watch(sidebarExpandedCookie, (newVal) => {
  setSidebarExpandedToCookies(newVal)
})

export {
  sidebarExpandedCookie,
}
