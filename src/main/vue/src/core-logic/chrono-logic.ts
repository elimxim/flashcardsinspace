import { asIsoDateStr } from '@/utils/date.ts'
import { toSortedOrders } from '@/core-logic/stage-logic.ts'
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
  status: string | null
  stages: string | null
  seqNumber: number | null
  isCurrMonth: boolean
  isCurrDay: boolean
}

export function isInProgressAvailable(chronoday: Chronoday): boolean {
  switch (chronoday.status) {
    case chronodayStatuses.NOT_STARTED:
      return true
    default:
      return false
  }
}

export function isCompleteAvailable(chronoday: Chronoday): boolean {
  switch (chronoday.status) {
    case chronodayStatuses.NOT_STARTED:
    case chronodayStatuses.IN_PROGRESS:
      return true
    default:
      return false
  }
}

export function selectConsecutiveDaysBeforeIncluding(
  chronodays: Chronoday[],
  startDay: Chronoday,
  condition: (chronoday: Chronoday) => boolean,
): Chronoday[] {
  const result: Chronoday[] = []
  for (let i = startDay.seqNumber; i >= 0; i--) {
    const chronoday = chronodays[i]
    if (chronoday !== undefined && condition(chronoday)) {
      result.push(chronoday)
    } else {
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
      status: null,
      stages: null,
      seqNumber: null,
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
      status: chronoday?.status ?? null,
      stages: toSortedOrders(chronoday?.stages ?? []).toString(),
      seqNumber: chronoday?.seqNumber ?? null,
      isCurrMonth: true,
      isCurrDay: isoDateStr === currDay.chronodate,
    })
  }

  const lastMonthDay = new Date(year, month + 1, 0)
  const lastWeekDay = lastMonthDay.getDay()
  const nextMonth = month === 11 ? 0 : month + 1
  const nextYear = month === 11 ? year + 1 : year

  // filling empty slots at the end
  for (let i = 1; i <= 6 - lastWeekDay; i++) {
    const date = new Date(nextYear, nextMonth, i)

    result.push({
      number: date.getDate(),
      date: date.toDateString(),
      status: null,
      stages: null,
      seqNumber: null,
      isCurrMonth: false,
      isCurrDay: false,
    })
  }

  return result
}

function totalDays(year: number, month: number): number {
  return new Date(year, month, 0).getDate()
}
