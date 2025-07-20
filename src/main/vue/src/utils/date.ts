export function isDateBetween(date: Date, from: Date, to: Date): boolean {
  const fromTime = from.getTime()
  const toTime = to.getTime()
  const dateTime = date.getTime()
  return dateTime >= fromTime && dateTime <= toTime
}

export function isStringBetween(date: string, from: Date, to: Date): boolean {
  return isDateBetween(new Date(date), from, to)
}

export function isDateBefore(date: Date, before: Date): boolean {
  const beforeTime = before.getTime()
  const dateTime = date.getTime()
  return dateTime < beforeTime
}

export function isStringBefore(date: string, before: Date): boolean {
  return isDateBefore(new Date(date), before)
}
