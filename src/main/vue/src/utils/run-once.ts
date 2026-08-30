import { ref } from 'vue'
import { Log, LogTag } from '@/utils/logger.ts'

/**
 * Ensures task runs exactly once.
 * Subsequent calls are ignored and resolve with the first call's result,
 * so arguments only matter on the first call.
 */
export function useRunOnce<A extends unknown[], R>(task: (...args: A) => R | Promise<R>) {
  const executed = ref(false)
  const isPending = ref(false)

  let runPromise: Promise<R | undefined> | null = null

  const runOnce = async (...args: A): Promise<R | undefined> => {
    if (executed.value) return runPromise ?? undefined

    executed.value = true
    isPending.value = true

    runPromise = (async () => {
      try {
        return await task(...args)
      } catch (error) {
        Log.error(LogTag.SYSTEM, 'Task failed:', error)
        return undefined
      } finally {
        isPending.value = false
      }
    })()

    return runPromise
  }

  return {
    runOnce,
    executed,
    isPending,
  }
}
