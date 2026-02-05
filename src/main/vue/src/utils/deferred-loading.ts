import { ref } from 'vue'

export interface DeferredLoadingOptions {
  delayEntry?: number,
  minDuration?: number,
}

export function useDeferredLoading(options?: DeferredLoadingOptions) {
  const delayEntry = options?.delayEntry ?? 200
  const minDuration = options?.minDuration ?? 800

  const loadingStarted = ref(false)
  const isDelaying = ref(true)
  const resolvedLoading = ref(false)

  let startTimestamp = 0
  let entryTimeout: ReturnType<typeof setTimeout> | null = null

  const startLoading = () => {
    loadingStarted.value = true
    entryTimeout = setTimeout(() => {
      isDelaying.value = false
      resolvedLoading.value = true
      startTimestamp = performance.now()
      entryTimeout = null
    }, delayEntry)
  }

  const stopLoading = async () => {
    loadingStarted.value = false
    if (entryTimeout) {
      clearTimeout(entryTimeout)
    }

    if (resolvedLoading.value) {
      const elapsed = performance.now() - startTimestamp
      const remaining = Math.max(0, minDuration - elapsed)

      if (remaining > 0) {
        await new Promise(resolved => setTimeout(resolved, remaining))
      }

      resolvedLoading.value = false
    }

    isDelaying.value = false
  }

  const resetLoading = () => {
    isDelaying.value = true
    loadingStarted.value = false
    if (entryTimeout) {
      clearTimeout(entryTimeout)
      entryTimeout = null
    }
    resolvedLoading.value = false
  }

  return {
    isDelaying,
    loadingStarted,
    resolvedLoading,
    startLoading,
    stopLoading,
    resetLoading
  }
}
