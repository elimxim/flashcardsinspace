import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { useStopWatch } from '@/utils/stop-watch.ts'

describe('useStopWatch', () => {
  beforeEach(() => {
    vi.useFakeTimers()
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it('counts the time between ticks', () => {
    const { elapsedTime, startWatch, stopWatch } = useStopWatch()

    startWatch()
    vi.advanceTimersByTime(3000)
    stopWatch()

    expect(elapsedTime.value).toBe(3000)
  })

  it('skips a gap left by a sleeping machine', () => {
    const { elapsedTime, startWatch, stopWatch } = useStopWatch()

    startWatch()
    vi.advanceTimersByTime(1000)
    vi.setSystemTime(Date.now() + 2 * 60 * 60 * 1000)
    vi.advanceTimersByTime(1000)
    stopWatch()

    expect(elapsedTime.value).toBe(1900)
  })
})
