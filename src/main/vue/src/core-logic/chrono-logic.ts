import { asIsoDateStr } from '@/utils/date.ts'
import { toSortedOrderNumbers } from '@/core-logic/stage-logic.ts'
import type { Chronoday } from '@/model/chrono.ts'

export const chronodayStatuses = {
  INITIAL: 'INITIAL',
  NOT_STARTED: 'NOT_STARTED',
  IN_PROGRESS: 'IN_PROGRESS',
  COMPLETED: 'COMPLETED',
  OFF: 'OFF',
}

export interface CalendarDay {
  number: number
  date: string
  status?: string
  stages?: string
  seqNumber?: number
  isCurrMonth: boolean
  isCurrDay: boolean
}

export const chronodayStatusesToProgressDay = new Set([
  chronodayStatuses.NOT_STARTED,
])

export const chronodayStatusesToCompleteDay = new Set([
  chronodayStatuses.NOT_STARTED,
  chronodayStatuses.IN_PROGRESS,
])

export function selectConsecutiveDaysBefore(
  chronodays: Chronoday[],
  startDay: Chronoday,
  acceptedStatues: Set<string>,
  including: boolean = true,
): Chronoday[] {
  if (chronodays.length === 0) return []
  if (startDay.status === chronodayStatuses.INITIAL) return []

  const startDayIndex = chronodays.findIndex(day => day.id === startDay.id)
  const startIndex = including ? startDayIndex : startDayIndex - 1
  if (startIndex === -1) {
    throw Error(`Start day ${startDay.id} not found in chronodays`)
  }

  const result: Chronoday[] = []
  for (let i = startIndex; i >= 0; i--) {
    const chronoday = chronodays[i]
    if (chronoday && acceptedStatues.has(chronoday.status)) {
      result.push(chronoday)
    } else if (chronoday && chronoday.status !== chronodayStatuses.OFF) {
      break
    }
  }
  return result
}

export function calcCalendarPage(currMonth: Date, currDay: Chronoday, chronodays: Chronoday[]): CalendarDay[] {
  const chronodayMap = new Map(chronodays.map(day => [day.chronodate, day]))
  const year = currMonth.getFullYear()
  const month = currMonth.getMonth()

  const firstMonthDay = new Date(year, month, 1)
  const firstWeekDay = firstMonthDay.getDay()
  const prevYear = month === 0 ? year - 1 : year
  const prevMonth = month === 0 ? 11 : month - 1
  const totalDaysInPrevMonth = totalDays(prevYear, prevMonth + 1)

  // filling empty slots at the start
  const result: CalendarDay[] = []
  for (let i = firstWeekDay - 1; i >= 0; i--) {
    const date = new Date(prevYear, prevMonth, totalDaysInPrevMonth - i)

    result.push({
      number: date.getDate(),
      date: date.toDateString(),
      isCurrMonth: false,
      isCurrDay: false,
    })
  }

  const totalDaysInMonth = totalDays(year, month + 1)

  // filling the current month
  for (let i = 1; i <= totalDaysInMonth; i++) {
    const date = new Date(year, month, i)
    const isoDateStr = asIsoDateStr(date)
    const chronoday = chronodayMap.get(isoDateStr)

    result.push({
      number: i,
      date: date.toDateString(),
      status: chronoday?.status,
      stages: toSortedOrderNumbers(chronoday?.stages ?? []).toString(),
      seqNumber: chronoday?.seqNumber,
      isCurrMonth: true,
      isCurrDay: isoDateStr === currDay.chronodate,
    })
  }

  const lastMonthDay = new Date(year, month + 1, 0)
  const lastWeekDay = lastMonthDay.getDay()
  const nextMonth = month === 11 ? 0 : month + 1
  const nextYear = month === 11 ? year + 1 : year

  // filling empty slots at the end to complete the last week
  const endPadding = 6 - lastWeekDay
  for (let i = 1; i <= endPadding; i++) {
    const date = new Date(nextYear, nextMonth, i)

    result.push({
      number: date.getDate(),
      date: date.toDateString(),
      isCurrMonth: false,
      isCurrDay: false,
    })
  }

  // ensure exactly 6 rows (42 cells) by filling from the next month
  const totalCells = 42
  if (result.length < totalCells) {
    const extra = totalCells - result.length
    for (let i = 1; i <= extra; i++) {
      const date = new Date(nextYear, nextMonth, endPadding + i)
      result.push({
        number: date.getDate(),
        date: date.toDateString(),
        isCurrMonth: false,
        isCurrDay: false,
      })
    }
  }

  return result
}

function totalDays(year: number, month: number): number {
  return new Date(year, month, 0).getDate()
}
