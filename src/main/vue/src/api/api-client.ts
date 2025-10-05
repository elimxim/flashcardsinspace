import axios from 'axios'
import {
  ChronoSyncRequest,
  ChronoSyncResponse,
  FlashcardSetInitResponse, type FlashcardSetUpdateRequest
} from '@/api/communication.ts'
import type { Flashcard, FlashcardSet } from '@/model/flashcard.ts'

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
  return apiClient.put<ChronoSyncResponse>(`/flashcard-sets/${setId}/chrono/sync`, request)
}

export async function sendChronoSyncNextDay(setId: number) {
  console.log(`[POST] request => chrono sync next for set ${setId}`)
  return apiClient.post<ChronoSyncResponse>(`/flashcard-sets/${setId}/chrono/sync/next`)
}

export async function sendChronoSyncPrevDay(setId: number) {
  console.log(`[POST] request => chrono sync prev for set ${setId}`)
  return apiClient.post<ChronoSyncResponse>(`/flashcard-sets/${setId}/chrono/sync/prev`)
}
