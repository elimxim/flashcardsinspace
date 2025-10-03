import type { Chronoday } from '@/model/chrono.ts'

export interface ErrorResponseBody {
  timestamp: Date
  statusCode: number
  statusError: string,
  errorCode: string,
  message?: string,
  path: string,
}

export interface FlashcardSetUpdateRequest {
  name?: string,
  languageId?: number,
  default?: boolean,
}

export interface ChronoSyncRequest {
  clientDatetime: string,
}

export interface ChronoSyncResponse {
  chronodays: Chronoday[]
  currDay: Chronoday,
}

export interface ChronoBulkUpdateRequest {
  ids: ChronodayId[],
  status: string,
}

export interface ChronodayId {
  id: number
}
