export function isDateBetween(date: Date, from: Date, to: Date): boolean {
  console.log(`checking if date ${date} is between ${from} and ${to}`)
  console.log(`from: ${from.getTime()} to: ${to.getTime()} date: ${date.getTime()}`)

  const fromTime = from.getTime()
  const toTime = to.getTime()
  const dateTime = date.getTime()
  return dateTime >= fromTime && dateTime <= toTime
}

export function isStringBetween(date: string, from: Date, to: Date): boolean {
  return isDateBetween(new Date(date), from, to)
}
