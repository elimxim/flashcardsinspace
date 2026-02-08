import { ref } from 'vue'
import { Log, LogTag } from '@/utils/logger.ts'

/**
 * Ensures task runs exactly once.
 * Subsequent calls are ignored.
 */
export function useRunOnce<T extends (...args: unknown[]) => Promise<unknown> | unknown>(
  task: T,
  ...args: Parameters<T>
) {
  const executed = ref(false)
  const isPending = ref(false)

  let taskPromise: Promise<ReturnType<T> | undefined> | null = null

  const runOnce = async (): Promise<ReturnType<T> | undefined> => {
    // Synchronous Gate: Executed instantly in the current tick
    if (executed.value) return taskPromise ?? undefined

    executed.value = true
    isPending.value = true

    taskPromise = (async () => {
      try {
        return await task(...args) as ReturnType<T>
      } catch (error) {
        Log.error(LogTag.SYSTEM, 'Task failed:', error)
        return undefined
      } finally {
        isPending.value = false
      }
    })()

    return taskPromise
  }

  return {
    runOnce,
    executed,
    isPending,
  }
}
