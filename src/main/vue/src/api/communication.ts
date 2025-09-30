import type { Chronoday } from '@/model/chrono.ts'

export interface ErrorResponseBody {
  timestamp: Date
  statusCode: number
  statusError: string,
  errorCode: string,
  message: string | undefined,
  path: string,
}

export interface FlashcardSetUpdateRequest {
  name: string | null,
  default: boolean | null,
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
