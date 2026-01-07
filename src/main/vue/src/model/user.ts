export interface User {
  id: number
  email: string
  emailVerified: boolean
  name: string
  roles: string[]
  registeredAt: Date // ZonedDateTime
  timezone: string
  languageId: number
}
