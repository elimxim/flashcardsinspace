import { onMounted, onUnmounted } from 'vue'

export function usePageHidden(onHidden: () => void) {
  function handleVisibilityChange() {
    if (document.visibilityState === 'hidden') onHidden()
  }

  onMounted(() => document.addEventListener('visibilitychange', handleVisibilityChange))
  onUnmounted(() => document.removeEventListener('visibilitychange', handleVisibilityChange))
}
