import { describe, expect, it } from 'vitest'
import {
  dragVelocity,
  originTransform,
  isDismissFlick,
  reachedDismissDistance,
  remainingSlideDuration,
  sheetDragOffset,
} from '@/utils/modal-motion.ts'

describe('originTransform', () => {
  it('moves and scales the window onto the origin box', () => {
    const origin = { left: 0, top: 0, width: 50, height: 20 }
    const window = { left: 100, top: 100, width: 500, height: 400 }

    expect(originTransform(origin, window)).toBe('translate(-325px, -290px) scale(0.1, 0.05)')
  })

  it('never collapses the window to zero', () => {
    const origin = { left: 10, top: 10, width: 1, height: 1 }
    const window = { left: 0, top: 0, width: 1000, height: 1000 }

    expect(originTransform(origin, window)).toContain('scale(0.04, 0.04)')
  })
})

describe('sheetDragOffset', () => {
  it('follows the finger downwards', () => {
    expect(sheetDragOffset(120)).toBe(120)
  })

  it('resists pulling upwards', () => {
    expect(sheetDragOffset(-100)).toBe(-20)
  })
})

describe('dragVelocity', () => {
  it('measures only the most recent movement', () => {
    const samples = [
      { y: 0, time: 0 },
      { y: 10, time: 200 },
      { y: 60, time: 250 },
      { y: 110, time: 300 },
    ]

    expect(dragVelocity(samples)).toBe(1)
  })

  it('is zero without movement over time', () => {
    expect(dragVelocity([])).toBe(0)
    expect(dragVelocity([{ y: 5, time: 10 }])).toBe(0)
  })
})

describe('reachedDismissDistance', () => {
  it('is reached at 40% of the sheet height', () => {
    expect(reachedDismissDistance(240, 600)).toBe(true)
    expect(reachedDismissDistance(239, 600)).toBe(false)
  })
})

describe('isDismissFlick', () => {
  it('needs a fast downward movement', () => {
    expect(isDismissFlick(0.8)).toBe(true)
    expect(isDismissFlick(0.2)).toBe(false)
    expect(isDismissFlick(-0.8)).toBe(false)
  })
})

describe('remainingSlideDuration', () => {
  it('scales with the distance left to travel', () => {
    expect(remainingSlideDuration(0, 600, 300)).toBe(300)
    expect(remainingSlideDuration(300, 600, 300)).toBe(150)
  })

  it('keeps a minimum so the slide stays visible', () => {
    expect(remainingSlideDuration(600, 600, 300)).toBe(90)
  })
})
