import { ref, type Ref } from 'vue'

// A gap between ticks longer than this means the machine slept or the page was suspended
const MAX_TICK_GAP_MS = 10_000

export function useStopWatch(timeRef?: Ref<number>) {
  const elapsedTime = timeRef ?? ref(0)
  let lastTickTime = 0
  let intervalId: ReturnType<typeof setInterval> | null = null

  function startWatch() {
    if (intervalId) return
    lastTickTime = Date.now()
    intervalId = setInterval(() => {
      const now = Date.now()
      const delta = now - lastTickTime
      lastTickTime = now
      if (delta <= MAX_TICK_GAP_MS) elapsedTime.value += delta
    }, 100)
  }

  function stopWatch() {
    if (intervalId) {
      clearInterval(intervalId)
      intervalId = null
    }
  }

  function resetWatch(newTime: number = 0) {
    stopWatch()
    elapsedTime.value = newTime
  }

  return {
    elapsedTime,
    startWatch,
    stopWatch,
    resetWatch,
  }
}
