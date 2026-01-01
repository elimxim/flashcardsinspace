/**
 * Creates a seeded pseudorandom number generator using the Mulberry32 algorithm.
 *
 * @param a - The seed value (32-bit integer)
 * @returns A function that generates the next pseudorandom number in the sequence.
 *          Each call returns a floating-point number in the range [0, 1).
 */
export function mulberry32(a: number) {
  return function () {
    let t = (a += 0x6D2B79F5)
    t = Math.imul(t ^ (t >>> 15), t | 1)
    t ^= t + Math.imul(t ^ (t >>> 7), t | 61)
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296
  }
}

export function shuffle<T>(array: T[]): T[] {
  for (let i = array.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [array[i], array[j]] = [array[j], array[i]]
  }
  return array
}

/**
 * Format a Date object as LocalDate string in user's local timezone
 * @param date Date object
 * @returns ISO date string "yyyy-MM-dd"
 */
export function asIsoDateStr(date: Date): string {
  const year = date.getFullYear()
  const month = (date.getMonth() + 1).toString().padStart(2, '0')
  const day = date.getDate().toString().padStart(2, '0')
  return `${year}-${month}-${day}`
}

/**
 * Parse LocalDate string to Date at midnight in user's local timezone
 * @param dateStr ISO date string "yyyy-MM-dd"
 * @returns Date object at midnight local time
 */
export function parseLocalDate(dateStr: string): Date {
  const [year, month, day] = dateStr.split('-').map(Number)
  return new Date(year, month - 1, day)
}

export function parseNumber(value?: unknown): number | undefined {
  if (value === undefined || value === null) return undefined
  return Number(value)
}

export function datePrevMonth(date: Date): Date {
  const prev = new Date(date)
  prev.setMonth(date.getMonth() - 1)
  return prev
}

export function dateNextMonth(date: Date): Date {
  const next = new Date(date)
  next.setMonth(date.getMonth() + 1)
  return next
}

export const isHoverSupported = typeof window !== 'undefined'
  ? window.matchMedia('(hover: hover)').matches
  : false

/**
 * Detects if the primary input method is touch (mobile/tablet).
 * Uses CSS media queries to distinguish between:
 * - Mobile/Tablet: pointer is coarse (finger) AND no hover capability
 * - Desktop/Hybrid: fine pointer (mouse) OR has hover capability
 */
export const isTouchDevice = typeof window !== 'undefined'
  ? window.matchMedia('(pointer: coarse) and (hover: none)').matches
  : false

