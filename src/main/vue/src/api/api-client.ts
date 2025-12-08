import axios from 'axios'
import {
  type ChronoBulkUpdateRequest,
  type ChronodayId,
  ChronoSyncRequest,
  ChronoSyncResponse,
  ChronoUpdateResponse,
  FlashcardSetInitResponse,
  FlashcardSetSuspendResponse,
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

const apiClient = axios.create({
  baseURL: '/api',
  withCredentials: true,
})

configureDateTransformers(apiClient)
configureTokenRefreshInterceptor(apiClient)

export default apiClient

export async function sendFlashcardSetsGetRequest() {
  console.log(`[GET] request => flashcard sets`)
  return apiClient.get<FlashcardSet[]>('/flashcard-sets')
}

export async function sendFlashcardSetExtraRequest() {
  console.log(`[GET] request => flashcard sets extra`)
  return apiClient.get<FlashcardSetExtra[]>('/flashcard-sets/extra')
}

export async function sendFlashcardSetInitRequest(id: number, flashcard: Flashcard) {
  console.log(`[POST] request => init flashcard set ${id}`)
  return apiClient.post<FlashcardSetInitResponse>(`/flashcard-sets/${id}/init`, flashcard)
}

export async function sendFlashcardSetSuspendRequest(id: number, flashcardSet: FlashcardSet) {
  console.log(`[POST] request => suspend flashcard set ${id}`)
  return apiClient.post<FlashcardSetSuspendResponse>(`/flashcard-sets/${id}/suspend`, flashcardSet)
}

export async function sendFlashcardSetGetRequest(id: number) {
  console.log(`[GET] request => flashcard set ${id}`)
  return apiClient.get<FlashcardSet>(`/flashcard-sets/${id}`)
}

export async function sendFlashcardSetUpdateRequest(id: number, flashcardSet: FlashcardSet) {
  console.log(`[PUT] request => flashcard set ${id}`)
  return apiClient.put<FlashcardSet>(`/flashcard-sets/${id}`, flashcardSet)
}

export async function sendFlashcardSetRemovalRequest(id: number) {
  console.log(`[DELETE] request => flashcard set ${id}`)
  return apiClient.delete(`/flashcard-sets/${id}`)
}

export async function sendFlashcardSetCreationRequest(flashcardSet: FlashcardSet) {
  console.log(`[POST] request => create flashcard set`)
  return apiClient.post<FlashcardSet>(`/flashcard-sets`, flashcardSet)
}

export async function sendFlashcardsGetRequest(setId: number) {
  console.log(`[GET] request => flashcards for set ${setId}`)
  return apiClient.get<Flashcard[]>(`/flashcard-sets/${setId}/flashcards`)
}

export async function sendFlashcardCreationRequest(setId: number, flashcard: Flashcard) {
  console.log(`[POST] request => add flashcard to set ${setId}`)
  return apiClient.post<Flashcard>(`/flashcard-sets/${setId}/flashcards`, flashcard)
}

export async function sendFlashcardUpdateRequest(setId: number, flashcard: Flashcard) {
  console.log(`[PUT] request => flashcard ${flashcard.id} in set ${setId}`)
  return apiClient.put<Flashcard>(`/flashcard-sets/${setId}/flashcards/${flashcard.id}`, flashcard)
}

export async function sendFlashcardRemovalRequest(setId: number, id: number) {
  console.log(`[DELETE] request => flashcard ${id} from set ${setId}`)
  return apiClient.delete(`/flashcard-sets/${setId}/flashcards/${id}`)
}

export async function sendChronoSyncRequest(setId: number) {
  const request: ChronoSyncRequest = {
    clientDatetime: new Date()
  }

  console.log(`[POST] request => chrono sync for set ${setId}`)
  return apiClient.post<ChronoSyncResponse>(`/flashcard-sets/${setId}/chrono/sync`, request)
}

export async function sendChronoSyncNextDay(setId: number) {
  console.log(`[POST] request => chrono sync next for set ${setId}`)
  return apiClient.post<ChronoSyncResponse>(`/flashcard-sets/${setId}/chrono/sync/next`)
}

export async function sendChronoSyncPrevDay(setId: number) {
  console.log(`[POST] request => chrono sync prev for set ${setId}`)
  return apiClient.post<ChronoSyncResponse>(`/flashcard-sets/${setId}/chrono/sync/prev`)
}

export async function sendChronoBulkUpdateRequest(setId: number, status: string, days: Chronoday[]) {
  console.log(`[PUT] request => bulk update days ${status} for set ${setId}`)
  const request: ChronoBulkUpdateRequest = {
    ids: days.map((v): ChronodayId => ({ id: v.id })),
    status: status
  }

  return apiClient.put<ChronoUpdateResponse>(`/flashcard-sets/${setId}/chrono/bulk`, request)
}

export async function sendFlashcardAudioMetadataGetRequest(setId: number) {
  console.log(`[GET] request => audio metadata for flashcard set ${setId}`)
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

  console.log(`[POST] request => upload audio for flashcard ${flashcardId} / ${side} in set ${setId}`)
  return apiClient.post<FlashcardAudio>(`/flashcard-sets/${setId}/flashcards/${flashcardId}/audio`,
    form,
    {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: 60_000,
    }
  )
}

export async function sendFlashcardAudioFetchRequest(setId: number, flashcardId: number, side: string) {
  console.log(`[GET] request => audio for flashcard ${flashcardId} / ${side} in set ${setId}`)
  return apiClient.get<Blob>(`/flashcard-sets/${setId}/flashcards/${flashcardId}/audio`, {
    responseType: 'blob',
    params: {
      side: side
    },
  })
}

export async function sendFlashcardAudioFetchByIdRequest(setId: number, flashcardId: number, audioId: number) {
  console.log(`[GET] request => audio ${audioId} for flashcard ${flashcardId} in set ${setId}`)
  return apiClient.get<Blob>(`/flashcard-sets/${setId}/flashcards/${flashcardId}/audio/${audioId}`, {
    responseType: 'blob'
  })
}

export async function sendFlashcardAudioRemovalRequest(setId: number, flashcardId: number, audioId: number) {
  console.log(`[DELETE] request => audio ${audioId} for flashcard ${flashcardId} in set ${setId}`)
  return apiClient.delete(`/flashcard-sets/${setId}/flashcards/${flashcardId}/audio/${audioId}`)
}

export async function sendUserUpdateRequest(username: string, userEmail: string, languageId: number | undefined) {
  console.log(`[PUT] request => user`)
  return apiClient.put<User>(`/users`, {
    name: username,
    email: userEmail,
    languageId: languageId,
  })
}

export async function sendReviewSessionGetRequest(setId: number, id: number) {
  console.log(`[GET] request => review session ${id} for set ${setId}`)
  return apiClient.get<ReviewSession>(`/flashcard-sets/${setId}/review-sessions/${id}`)
}

export async function sendReviewSessionCreateRequest(setId: number, session: ReviewSession) {
  console.log(`[POST] request => create review session for set ${setId}`)
  return apiClient.post<ReviewSession>(`/flashcard-sets/${setId}/review-sessions`, session)
}

export async function sendReviewSessionChildCreateRequest(setId: number, parentId: number, session: ReviewSession) {
  console.log(`[POST] request => child review session ${parentId} for set ${setId}`)
  return apiClient.post<ReviewSession>(`/flashcard-sets/${setId}/review-sessions/${parentId}/children`, session)
}

export async function sendReviewSessionUpdateRequest(setId: number, id: number, session: ReviewSession) {
  console.log(`[PUT] request => review session ${id} for set ${setId}`)
  return apiClient.put<ReviewSession>(`/flashcard-sets/${setId}/review-sessions/${id}`, session)
}
