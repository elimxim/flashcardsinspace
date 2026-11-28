import { ref, watch } from 'vue'
import {
  loadGallerySidebarExpandedFromCookies,
  loadSelectedSetIdFromCookies,
  loadSidebarExpandedFromCookies,
  removeSelectedSetIdCookie,
  saveSelectedSetIdToCookies,
  setGallerySidebarExpandedToCookies,
  setSidebarExpandedToCookies,
} from '@/utils/cookies.ts'

const controlPanelSidebarExpandedCookie = ref(loadSidebarExpandedFromCookies())
const gallerySidebarExpandedCookie = ref(loadGallerySidebarExpandedFromCookies())
const selectedSetIdCookie = ref(loadSelectedSetIdFromCookies())

watch(controlPanelSidebarExpandedCookie, (newVal) => {
  setSidebarExpandedToCookies(newVal)
})

watch(gallerySidebarExpandedCookie, (newVal) => {
  setGallerySidebarExpandedToCookies(newVal)
})

watch(selectedSetIdCookie, (newVal) => {
  if (newVal) {
    saveSelectedSetIdToCookies(newVal)
  } else {
    removeSelectedSetIdCookie()
  }
})

export { controlPanelSidebarExpandedCookie, gallerySidebarExpandedCookie, selectedSetIdCookie }
