import type { User } from '@/model/user.ts'
import type { Language } from '@/model/language.ts'

export interface UserResponse {
  user: User
}

export interface LanguageResponse {
  languages: Language[]
}
