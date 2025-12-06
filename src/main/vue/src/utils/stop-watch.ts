import { ref, type Ref } from 'vue'

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
      elapsedTime.value += delta
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
