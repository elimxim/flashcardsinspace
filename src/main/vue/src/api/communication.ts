import type { Language } from '@/model/language.ts'
import type { Chronoday } from '@/model/chrono.ts'

export interface ErrorResponseBody {
  timestamp: Date
  statusCode: number
  statusError: string,
  errorCode: string,
  message: string | undefined,
  path: string,
}

export interface LanguageGetResponse {
  languages: Language[]
}

export interface FlashcardSetUpdateRequest {
  name: string | null,
  default: boolean | null,
}

export interface ChronodaysGetParams {
  clientDatetime: string,
}

export interface ChronodaysGetResponse {
  chronodays: Chronoday[]
  currDay: Chronoday,
}

export interface ChronodaysResponse {
  chronoday: Chronoday,
}

export interface ChronodaysPutRequest {
  status: string,
}
