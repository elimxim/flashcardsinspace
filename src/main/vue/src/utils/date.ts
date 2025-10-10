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
