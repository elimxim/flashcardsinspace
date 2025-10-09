import axios from 'axios'
import {
  type ChronoBulkUpdateRequest, type ChronodayId,
  ChronoSyncRequest,
  ChronoSyncResponse,
  FlashcardSetInitResponse,
} from '@/api/communication.ts'
import type { Flashcard, FlashcardSet } from '@/model/flashcard.ts'
import { Chronoday } from '@/model/chrono.ts';

const apiClient = axios.create({
  baseURL: '/api',
  withCredentials: true,
})

export default apiClient

export async function sendFlashcardSetInitRequest(id: number, flashcard: Flashcard) {
  console.log(`[POST] request => init flashcard set ${id}`)
  return apiClient.post<FlashcardSetInitResponse>(`/flashcard-sets/${id}/init`, flashcard)
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
    clientDatetime: new Date().toISOString()
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

  return apiClient.put<Chronoday[]>(`/flashcard-sets/${setId}/chrono/bulk`, request)
}
