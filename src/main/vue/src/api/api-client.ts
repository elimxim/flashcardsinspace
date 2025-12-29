import axios from 'axios'
import {
  type ChronoBulkUpdateRequest,
  type ChronodayId,
  ChronoSyncRequest,
  ChronoSyncResponse,
  ChronoUpdateResponse,
  FlashcardSetInitResponse,
  FlashcardSetSuspendResponse,
  ReviewSessionCreateRequest,
  ReviewSessionUpdateRequest,
} from '@/api/communication.ts'
import {
  Flashcard,
  FlashcardAudio,
  FlashcardAudioMetadata,
  FlashcardSet,
  FlashcardSetExtra
} from '@/model/flashcard.ts'
import { Chronoday } from '@/model/chrono.ts'
import { configureDateTransformers } from '@/api/axios-config.ts'
import { configureTokenRefreshInterceptor } from '@/api/token-refresh.ts'
import { User } from '@/model/user.ts'
import { ReviewSession } from '@/model/review.ts'
import { ReviewSessionType } from '@/core-logic/review-logic.ts'
import { Log, LogTag } from '@/utils/logger.ts'

const apiClient = axios.create({
  baseURL: '/api',
  withCredentials: true,
})

configureDateTransformers(apiClient)
configureTokenRefreshInterceptor(apiClient)

export default apiClient

export async function sendFlashcardSetsGetRequest() {
  Log.log(LogTag.GET, `/flashcard-sets`)
  return apiClient.get<FlashcardSet[]>('/flashcard-sets')
}

export async function sendFlashcardSetExtraRequest() {
  Log.log(LogTag.GET, `/flashcard-sets/extra`)
  return apiClient.get<FlashcardSetExtra[]>('/flashcard-sets/extra')
}

export async function sendFlashcardSetInitRequest(id: number, flashcard: Flashcard) {
  Log.log(LogTag.POST, `/flashcard-sets/${id}/init`)
  return apiClient.post<FlashcardSetInitResponse>(`/flashcard-sets/${id}/init`, flashcard)
}

export async function sendFlashcardSetSuspendRequest(id: number, flashcardSet: FlashcardSet) {
  Log.log(LogTag.POST, `/flashcard-sets/${id}/suspend`)
  return apiClient.post<FlashcardSetSuspendResponse>(`/flashcard-sets/${id}/suspend`, flashcardSet)
}

export async function sendFlashcardSetGetRequest(id: number) {
  Log.log(LogTag.GET, `flashcard-sets/${id}`)
  return apiClient.get<FlashcardSet>(`/flashcard-sets/${id}`)
}

export async function sendFlashcardSetUpdateRequest(id: number, flashcardSet: FlashcardSet) {
  Log.log(LogTag.PUT, `flashcard-sets/${id}`)
  return apiClient.put<FlashcardSet>(`/flashcard-sets/${id}`, flashcardSet)
}

export async function sendFlashcardSetRemovalRequest(id: number) {
  Log.log(LogTag.POST, `/flashcard-sets/${id}`)
  return apiClient.delete(`/flashcard-sets/${id}`)
}

export async function sendFlashcardSetCreationRequest(flashcardSet: FlashcardSet) {
  Log.log(LogTag.POST, `/flashcard-sets`)
  return apiClient.post<FlashcardSet>(`/flashcard-sets`, flashcardSet)
}

export async function sendFlashcardsGetRequest(setId: number) {
  Log.log(LogTag.GET, `flashcards-sets/${setId}/flashcards`)
  return apiClient.get<Flashcard[]>(`/flashcard-sets/${setId}/flashcards`)
}

export async function sendFlashcardCreationRequest(setId: number, flashcard: Flashcard) {
  Log.log(LogTag.POST, `/flashcard-sets/${setId}/flashcards`)
  return apiClient.post<Flashcard>(`/flashcard-sets/${setId}/flashcards`, flashcard)
}

export async function sendFlashcardUpdateRequest(setId: number, flashcard: Flashcard) {
  Log.log(LogTag.PUT, `/flashcard-sets/${setId}/flashcards/${flashcard.id}`)
  return apiClient.put<Flashcard>(`/flashcard-sets/${setId}/flashcards/${flashcard.id}`, flashcard)
}

export async function sendFlashcardRemovalRequest(setId: number, id: number) {
  Log.log(LogTag.DELETE, `/flashcard-sets/${setId}/flashcards/${id}`)
  return apiClient.delete(`/flashcard-sets/${setId}/flashcards/${id}`)
}

export async function sendChronoSyncRequest(setId: number) {
  const request: ChronoSyncRequest = {
    clientDatetime: new Date()
  }

  Log.log(LogTag.POST, `/flashcard-sets/${setId}/chrono/sync`)
  return apiClient.post<ChronoSyncResponse>(`/flashcard-sets/${setId}/chrono/sync`, request)
}

export async function sendChronoSyncNextDay(setId: number) {
  Log.log(LogTag.POST, `/flashcard-sets/${setId}/chrono/sync/next`)
  return apiClient.post<ChronoSyncResponse>(`/flashcard-sets/${setId}/chrono/sync/next`)
}

export async function sendChronoSyncPrevDay(setId: number) {
  Log.log(LogTag.POST, `/flashcard-sets/${setId}/chrono/sync/prev`)
  return apiClient.post<ChronoSyncResponse>(`/flashcard-sets/${setId}/chrono/sync/prev`)
}

export async function sendChronoBulkUpdateRequest(setId: number, status: string, days: Chronoday[]) {
  Log.log(LogTag.PUT, `/flashcard-sets/${setId}/chrono/bulk`)
  const request: ChronoBulkUpdateRequest = {
    ids: days.map((v): ChronodayId => ({ id: v.id })),
    status: status
  }

  return apiClient.put<ChronoUpdateResponse>(`/flashcard-sets/${setId}/chrono/bulk`, request)
}

export async function sendFlashcardAudioMetadataGetRequest(setId: number) {
  Log.log(LogTag.GET, `/flashcard-sets/${setId}/flashcards/audio/metadata`)
  return apiClient.get<FlashcardAudioMetadata[]>(`/flashcard-sets/${setId}/flashcards/audio/metadata`)
}

export async function sendFlashcardAudioUploadRequest(setId: number, flashcardId: number, side: string, audioBlob: Blob) {
  const ext =
    audioBlob.type.includes('ogg') ? 'ogg'
      : audioBlob.type.includes('mp4') ? 'm4a'
        : audioBlob.type.includes('webm') ? 'webm'
          : 'unknown'

  const form = new FormData()
  form.append('side', side)
  form.append('file', new File([audioBlob], `voice-${Date.now()}.${ext}`, {
      type: audioBlob.type,
    })
  )

  Log.log(LogTag.POST, `/flashcard-sets/${setId}/flashcards/${flashcardId}/audio?side=${side}`)
  return apiClient.post<FlashcardAudio>(`/flashcard-sets/${setId}/flashcards/${flashcardId}/audio`,
    form,
    {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: 60_000,
    }
  )
}

export async function sendFlashcardAudioGetRequest(setId: number, flashcardId: number, side: string) {
  Log.log(LogTag.GET, `/flashcard-sets/${setId}/flashcards/${flashcardId}/audio?side=${side}`)
  return apiClient.get<Blob>(`/flashcard-sets/${setId}/flashcards/${flashcardId}/audio`, {
    responseType: 'blob',
    params: {
      side: side
    },
  })
}

export async function sendFlashcardAudioFetchByIdRequest(setId: number, flashcardId: number, audioId: number) {
  Log.log(LogTag.GET, `/flashcard-sets/${setId}/flashcards/${flashcardId}/audio/${audioId}`)
  return apiClient.get<Blob>(`/flashcard-sets/${setId}/flashcards/${flashcardId}/audio/${audioId}`, {
    responseType: 'blob'
  })
}

export async function sendFlashcardAudioRemovalRequest(setId: number, flashcardId: number, audioId: number) {
  Log.log(LogTag.DELETE, `/flashcard-sets/${setId}/flashcards/${flashcardId}/audio/${audioId}`)
  return apiClient.delete(`/flashcard-sets/${setId}/flashcards/${flashcardId}/audio/${audioId}`)
}

export async function sendUserUpdateRequest(username: string, userEmail: string, languageId: number | undefined) {
  Log.log(LogTag.PUT, `/users`)
  return apiClient.put<User>(`/users`, {
    name: username,
    email: userEmail,
    languageId: languageId,
  })
}

export async function sendLatestUncompletedReviewSessionGetRequest(setId: number, type: ReviewSessionType) {
  Log.log(LogTag.GET, `/flashcard-sets/${setId}/review-sessions/latest-uncompleted?type=${type}`)
  return apiClient.get<ReviewSession>(`/flashcard-sets/${setId}/review-sessions/latest-uncompleted?type=${type}`)
}

export async function sendReviewSessionGetRequest(setId: number, id: number) {
  Log.log(LogTag.GET, `/flashcard-sets/${setId}/review-sessions/${id}`)
  return apiClient.get<ReviewSession>(`/flashcard-sets/${setId}/review-sessions/${id}`)
}

export async function sendReviewSessionCreateRequest(setId: number, request: ReviewSessionCreateRequest) {
  Log.log(LogTag.POST, `/flashcard-sets/${setId}/review-sessions`)
  return apiClient.post<ReviewSession>(`/flashcard-sets/${setId}/review-sessions`, request)
}

export async function sendReviewSessionChildCreateRequest(setId: number, parentId: number, request: ReviewSessionCreateRequest) {
  Log.log(LogTag.POST, `/flashcard-sets/${setId}/review-sessions/${parentId}/children`)
  return apiClient.post<ReviewSession>(`/flashcard-sets/${setId}/review-sessions/${parentId}/children`, request)
}

export async function sendReviewSessionUpdateRequest(setId: number, id: number, request: ReviewSessionUpdateRequest) {
  Log.log(LogTag.PUT, `/flashcard-sets/${setId}/review-sessions/${id}`)
  return apiClient.put<ReviewSession>(`/flashcard-sets/${setId}/review-sessions/${id}`, request)
}
