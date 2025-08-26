import { describe, it, expect } from 'vitest'
import { selectConsecutiveDaysBeforeIncluding } from '@/core-logic/chrono-logic.ts'
import type { Chronoday } from '@/model/chrono.ts'
import { chronodayStatuses } from '@/core-logic/chrono-logic.ts'

describe('selectConsecutiveDaysBeforeIncluding', () => {
  const chronodays: Chronoday[] = [
    chronoday(0, chronodayStatuses.COMPLETED),
    chronoday(1, chronodayStatuses.COMPLETED),
    chronoday(2, chronodayStatuses.OFF),
    chronoday(3, chronodayStatuses.COMPLETED),
    chronoday(4, chronodayStatuses.COMPLETED),
    chronoday(5, chronodayStatuses.NOT_STARTED),
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

  it('should return only the start day if it meets the condition but the previous one does not', () => {
    // given:
    const startDay = chronodays[3]
    const condition = (day: Chronoday) => day.status === chronodayStatuses.COMPLETED

    // when:
    const result = selectConsecutiveDaysBeforeIncluding(chronodays, startDay, condition)

    // then:
    expect(result).toEqual([startDay])
  })

  it('should return all consecutive days that meet the condition in reverse order', () => {
    // given:
    const startDay = chronodays[4]
    const condition = (day: Chronoday) => day.status === chronodayStatuses.COMPLETED

    // when:
    const result = selectConsecutiveDaysBeforeIncluding(chronodays, startDay, condition)

    // then:
    expect(result).toEqual([chronodays[4], chronodays[3]])
  })

  it('should stop when a day that does not meet the condition is found', () => {
    // given:
    const startDay = chronodays[4]
    const condition = (day: Chronoday) => day.status === chronodayStatuses.COMPLETED

    // when:
    const result = selectConsecutiveDaysBeforeIncluding(chronodays, startDay, condition)

    // then:
    expect(result).toEqual([chronodays[4], chronodays[3]])
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
    const dummyStartDay = chronoday(0, 'COMPLETED')

    // when:
    const result = selectConsecutiveDaysBeforeIncluding([], dummyStartDay, () => true)

    // then:
    expect(result).toEqual([])
  })
})

const chronoday = (seqNumber: number, status: string): Chronoday => ({
  id: seqNumber,
  chronodate: `2025-08-${10 + seqNumber}`,
  seqNumber,
  status,
  stages: [],
})
