import { describe, it, expect } from 'vitest'
import { selectConsecutiveDaysBeforeIncluding } from '@/core-logic/chrono-logic.ts'
import type { Chronoday } from '@/model/chrono.ts'
import { chronodayStatuses } from '@/core-logic/chrono-logic.ts'

describe('selectConsecutiveDaysBeforeIncluding', () => {
  const chronodays: Chronoday[] = [
    chronoday(0, 0, chronodayStatuses.COMPLETED),
    chronoday(1, 1, chronodayStatuses.COMPLETED),
    chronoday(2, undefined, chronodayStatuses.OFF),
    chronoday(3, 2, chronodayStatuses.COMPLETED),
    chronoday(4, 3, chronodayStatuses.COMPLETED),
    chronoday(5, 4, chronodayStatuses.NOT_STARTED),
  ]

  it('should return an empty array if the condition is not met for the start day', () => {
    // given:
    const startDay = chronodays[5]
    const condition = (day: Chronoday) => day.status === chronodayStatuses.COMPLETED

    // when:
    const result = selectConsecutiveDaysBeforeIncluding(chronodays, startDay, condition)

    // then:
    expect(result).toEqual([])
  })

  it('should skip OFF days and continue collecting days that meet the condition', () => {
    // given:
    const startDay = chronodays[3]
    const condition = (day: Chronoday) => day.status === chronodayStatuses.COMPLETED

    // when:
    const result = selectConsecutiveDaysBeforeIncluding(chronodays, startDay, condition)

    // then:
    // Should include day 3, skip OFF day 2, and include days 1 and 0
    expect(result).toEqual([chronodays[3], chronodays[1], chronodays[0]])
  })

  it('should return all consecutive days that meet the condition, skipping OFF days', () => {
    // given:
    const startDay = chronodays[4]
    const condition = (day: Chronoday) => day.status === chronodayStatuses.COMPLETED

    // when:
    const result = selectConsecutiveDaysBeforeIncluding(chronodays, startDay, condition)

    // then:
    // Should include days 4, 3, skip OFF day 2, and include days 1 and 0
    expect(result).toEqual([chronodays[4], chronodays[3], chronodays[1], chronodays[0]])
  })

  it('should stop when a non-OFF day that does not meet the condition is found', () => {
    // given:
    const startDay = chronodays[5]
    const condition = (day: Chronoday) => day.status === chronodayStatuses.COMPLETED

    // when:
    const result = selectConsecutiveDaysBeforeIncluding(chronodays, startDay, condition)

    // then:
    // Should stop at day 5 (NOT_STARTED) which doesn't meet the condition
    expect(result).toEqual([])
  })

  it('should return all days from the start day to the beginning if they all meet the condition', () => {
    // given:
    const startDay = chronodays[1]
    const condition = (day: Chronoday) => day.status === chronodayStatuses.COMPLETED

    // when:
    const result = selectConsecutiveDaysBeforeIncluding(chronodays, startDay, condition)

    // then:
    expect(result).toEqual([chronodays[1], chronodays[0]])
  })

  it('should work correctly when startDay is the first day in the array', () => {
    // given:
    const startDay = chronodays[0]
    const condition = (day: Chronoday) => day.status === chronodayStatuses.COMPLETED

    // when:
    const result = selectConsecutiveDaysBeforeIncluding(chronodays, startDay, condition)

    // then:
    expect(result).toEqual([startDay])
  })

  it('should return an empty array when the provided chronodays array is empty', () => {
    // given:
    const dummyStartDay = chronoday(0, 0, 'COMPLETED')

    // when:
    const result = selectConsecutiveDaysBeforeIncluding([], dummyStartDay, () => true)

    // then:
    expect(result).toEqual([])
  })
})

const chronoday = (id: number, seqNumber: number | undefined, status: string): Chronoday => ({
  id,
  chronodate: `2025-08-${10 + id}`,
  seqNumber,
  status,
  stages: [],
})
