import axios from 'axios'
import { Chronoday } from '@/model/chrono.ts';
import {
  ChronoSyncRequest,
  ChronoSyncResponse,
  FlashcardSetInitResponse
} from '@/api/communication.ts';
import type { Flashcard, FlashcardSet } from '@/model/flashcard.ts';

const apiClient = axios.create({
  baseURL: '/api',
  withCredentials: true,
})

export default apiClient

export async function sendFlashcardSetInitRequest(id: number, flashcard: Flashcard) {
  console.log(`[POST] request => init flashcard set ${id}`)
  return await apiClient.post<FlashcardSetInitResponse>(`/flashcard-sets/${id}/init`, flashcard)
}

export async function sendFlashcardSetGetRequest(id: number) {
  console.log(`[GET] request => flashcard set ${id}`)
  return await apiClient.get<FlashcardSet>(`/flashcard-sets/${id}`)
}

export async function sendFlashcardCreationRequest(setId: number, flashcard: Flashcard) {
  console.log(`[POST] request => add flashcard to set ${setId}`)
  return await apiClient.post<Flashcard>(`/flashcard-sets/${setId}/flashcards`, flashcard)
}

export async function sendFlashcardRemovalRequest(setId: number, id: number) {
  console.log(`[DELETE] request => remove flashcard ${id} from set ${setId}`)
  return await apiClient.delete(`/flashcard-sets/${setId}/flashcards/${id}`)
}

export async function sendChronoSyncRequest(setId: number) {
  const request: ChronoSyncRequest = {
    clientDatetime: new Date().toISOString()
  }

  console.log(`[PUT] request => sync chronodays for set ${setId}`)
  return await apiClient.put<ChronoSyncResponse>(`/flashcard-sets/${setId}/chronodays`, request)
}

export async function sendChronodayCreationRequest(setId: number) {
  console.log(`[POST] request => add chronoday for set ${setId}`)
  return await apiClient.post<Chronoday>(`/flashcard-sets/${setId}/chronodays`)
}
