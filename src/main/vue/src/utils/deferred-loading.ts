import { ref, watch } from 'vue'

export interface DeferredLoadingOptions {
  trigger?: () => boolean,
  minDuration?: number,
  delayEntry?: number,
}

export function useDeferredLoading(options?: DeferredLoadingOptions) {
  const trigger = options?.trigger ?? (() => false)
  const minDuration = options?.minDuration ?? 600
  const delayEntry = options?.delayEntry ?? 200

  const resolvedLoading = ref(false)
  let startTimestamp = 0
  let entryTimeout: ReturnType<typeof setTimeout> | null = null

  watch(trigger, async (newVal) => {
    if (newVal) {
      startLoading()
    } else {
      await stopLoading()
    }
  })

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

  return { resolvedLoading, startLoading, stopLoading }
}
