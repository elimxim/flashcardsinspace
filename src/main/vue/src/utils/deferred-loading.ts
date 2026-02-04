import { ref } from 'vue'

export interface DeferredLoadingOptions {
  minDuration?: number,
  delayEntry?: number,
}

export function useDeferredLoading(options?: DeferredLoadingOptions) {
  const minDuration = options?.minDuration ?? 600
  const delayEntry = options?.delayEntry ?? 200

  const resolvedLoading = ref(false)
  let startTimestamp = 0
  let entryTimeout: ReturnType<typeof setTimeout> | null = null

  const startLoading = () => {
    entryTimeout = setTimeout(() => {
      resolvedLoading.value = true
      startTimestamp = performance.now()
      entryTimeout = null
    }, delayEntry)
  }

  const stopLoading = async () => {
    if (entryTimeout) {
      clearTimeout(entryTimeout)
    }

    if (resolvedLoading.value) {
      const elapsed = performance.now() - startTimestamp
      const remaining = Math.max(0, minDuration - elapsed)

      if (remaining > 0) {
        await new Promise(r => setTimeout(r, remaining))
      }

      resolvedLoading.value = false
    }
  }

  const resetLoading = () => {
    if (entryTimeout) {
      clearTimeout(entryTimeout)
      entryTimeout = null
    }
    resolvedLoading.value = false
  }

  return {
    resolvedLoading,
    startLoading,
    stopLoading,
    resetLoading
  }
}
