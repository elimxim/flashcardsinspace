import { describe, it, expect } from 'vitest'
import { nextChronoday, selectConsecutiveDaysBefore } from '@/core-logic/chrono-logic.ts'
import type { Chronoday } from '@/model/chrono.ts'
import { chronodayStatuses } from '@/core-logic/chrono-logic.ts'

describe('selectConsecutiveDaysBeforeIncluding', () => {
  const chronodays: Chronoday[] = [
    makeChronoday(0, 0, chronodayStatuses.COMPLETED),
    makeChronoday(1, 1, chronodayStatuses.COMPLETED),
    makeChronoday(2, undefined, chronodayStatuses.OFF),
    makeChronoday(3, 2, chronodayStatuses.COMPLETED),
    makeChronoday(4, 3, chronodayStatuses.COMPLETED),
    makeChronoday(5, 4, chronodayStatuses.NOT_STARTED),
  ]

  it('should return an empty array if the condition is not met for the start day', () => {
    const startDay = chronodays[5]
    const acceptedStatuses = new Set([chronodayStatuses.COMPLETED])

    const result = selectConsecutiveDaysBefore(chronodays, startDay, acceptedStatuses)

    expect(result).toEqual([])
  })

  it('should skip OFF days and continue collecting days that meet the condition', () => {
    const startDay = chronodays[3]
    const acceptedStatuses = new Set([chronodayStatuses.COMPLETED])

    const result = selectConsecutiveDaysBefore(chronodays, startDay, acceptedStatuses)

    // Should include day 3, skip OFF day 2, and include days 1 and 0
    expect(result).toEqual([chronodays[3], chronodays[1], chronodays[0]])
  })

  it('should return all consecutive days that meet the condition, skipping OFF days', () => {
    const startDay = chronodays[4]
    const acceptedStatuses = new Set([chronodayStatuses.COMPLETED])

    const result = selectConsecutiveDaysBefore(chronodays, startDay, acceptedStatuses)

    // Should include days 4, 3, skip OFF day 2, and include days 1 and 0
    expect(result).toEqual([chronodays[4], chronodays[3], chronodays[1], chronodays[0]])
  })

  it('should stop when a non-OFF day that does not meet the condition is found', () => {
    const startDay = chronodays[5]
    const acceptedStatuses = new Set([chronodayStatuses.COMPLETED])

    const result = selectConsecutiveDaysBefore(chronodays, startDay, acceptedStatuses)

    // Should stop at day 5 (NOT_STARTED) which doesn't meet the condition
    expect(result).toEqual([])
  })

  it('should return all days from the start day to the beginning if they all meet the condition', () => {
    const startDay = chronodays[1]
    const acceptedStatuses = new Set([chronodayStatuses.COMPLETED])

    const result = selectConsecutiveDaysBefore(chronodays, startDay, acceptedStatuses)

    expect(result).toEqual([chronodays[1], chronodays[0]])
  })

  it('should work correctly when startDay is the first day in the array', () => {
    const startDay = chronodays[0]
    const acceptedStatuses = new Set([chronodayStatuses.COMPLETED])

    const result = selectConsecutiveDaysBefore(chronodays, startDay, acceptedStatuses)

    expect(result).toEqual([startDay])
  })

  it('should return an empty array when the provided chronodays array is empty', () => {
    const dummyStartDay = makeChronoday(0, 0, 'COMPLETED')

    const result = selectConsecutiveDaysBefore([], dummyStartDay, new Set())

    expect(result).toEqual([])
  })
})

describe('nextChronoday', () => {
  const chronodays: Chronoday[] = [
    makeChronoday(0, 0, chronodayStatuses.COMPLETED),
    makeChronoday(1, 1, chronodayStatuses.COMPLETED),
    makeChronoday(2, undefined, chronodayStatuses.OFF),
    makeChronoday(3, 2, chronodayStatuses.NOT_STARTED),
  ]

  it('should return the day after the given day', () => {
    const result = nextChronoday(chronodays, chronodays[1])

    expect(result).toEqual(chronodays[2])
  })

  it('should return undefined when the given day is the last one', () => {
    const result = nextChronoday(chronodays, chronodays[3])

    expect(result).toBeUndefined()
  })

  it('should return undefined when the given day is not found', () => {
    const unknownDay = makeChronoday(99, 99, chronodayStatuses.NOT_STARTED)

    const result = nextChronoday(chronodays, unknownDay)

    expect(result).toBeUndefined()
  })

  it('should return undefined when the chronodays array is empty', () => {
    const result = nextChronoday([], chronodays[0])

    expect(result).toBeUndefined()
  })
})

const makeChronoday = (id: number, seqNumber: number | undefined, status: string): Chronoday => ({
  id,
  chronodate: `2025-08-${10 + id}`,
  seqNumber,
  status,
  stages: [],
})
