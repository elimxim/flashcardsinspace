export interface User {
  id: number
  email: string
  name: string
  roles: string[]
  registeredAt: Date // ZonedDateTime
  timezone: string
  languageId: number
}
