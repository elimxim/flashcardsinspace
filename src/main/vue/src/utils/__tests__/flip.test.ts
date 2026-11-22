import { describe, expect, it } from 'vitest'
import { springEasing } from '@/utils/flip.ts'

function values(easing: string): number[] {
  return easing
    .replace(/^linear\(|\)$/g, '')
    .split(', ')
    .map(Number)
}

describe('springEasing', () => {
  it('goes from 0 to exactly 1', () => {
    const points = values(springEasing(0.8))
    expect(points[0]).toBe(0)
    expect(points.at(-1)).toBe(1)
  })

  it('overshoots slightly when underdamped', () => {
    const peak = Math.max(...values(springEasing(0.8)))
    expect(peak).toBeGreaterThan(1)
    expect(peak).toBeLessThan(1.03)
  })

  it('does not overshoot when close to critically damped', () => {
    expect(Math.max(...values(springEasing(1)))).toBeLessThanOrEqual(1.0001)
  })
})
