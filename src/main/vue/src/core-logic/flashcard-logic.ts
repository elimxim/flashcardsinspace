import {
  type Flashcard,
  type FlashcardSet,
  FlashcardSetExtra,
  type ReviewInfo
} from '@/model/flashcard.ts'
import { type Stage, stages } from '@/core-logic/stage-logic.ts'
import type { Language } from '@/model/language.ts'
import { useChronoStore } from '@/stores/chrono-store.ts'
import { storeToRefs } from 'pinia'
import {
  sendFlashcardAudioFetchRequest,
  sendFlashcardAudioRemovalRequest,
  sendFlashcardAudioUploadRequest
} from '@/api/api-client.ts'
import { useSpaceToaster } from '@/stores/toast-store.ts'
import { useAudioCache } from '@/stores/audio-cache.ts'
import { useAudioStore } from '@/stores/audio-store.ts'

export const flashcardSetStatuses = {
  ACTIVE: 'ACTIVE',
  DELETED: 'DELETED',
  SUSPENDED: 'SUSPENDED',
}

export const flashcardSides = {
  FRONT: 'FRONT',
  BACK: 'BACK',
}

export function newFlashcard(frontSide: string, backSide: string): Flashcard {
  const chronoStore = useChronoStore()
  const { currDay } = storeToRefs(chronoStore)

  return {
    id: 0,
    frontSide: frontSide,
    backSide: backSide,
    stage: stages.S1.name,
    timesReviewed: 0,
    reviewHistory: { history: [] },
    creationDate: currDay.value.chronodate,
  }
}

export function updateFlashcardSides(flashcard: Flashcard, frontSide: string, backSide: string): Flashcard {
  flashcard.frontSide = frontSide
  flashcard.backSide = backSide
  flashcard.lastUpdatedAt = new Date()
  return flashcard
}

export function copyFlashcardSet(value: FlashcardSet): FlashcardSet {
  return JSON.parse(JSON.stringify(value))
}

export function copyFlashcard(value: Flashcard): Flashcard {
  return JSON.parse(JSON.stringify(value))
}

export function sortFlashcardSets(flashcardSets: FlashcardSet[]): FlashcardSet[] {
  return flashcardSets.sort((a, b) => {
    return a.name.localeCompare(b.name)
  })
}

export function updateFlashcard(flashcard: Flashcard, stage: Stage): Flashcard {
  const chronoStore = useChronoStore()
  const { currDay } = storeToRefs(chronoStore)

  const chronodate = currDay.value.chronodate

  const info: ReviewInfo = {
    stage: stage.name,
    reviewDate: chronodate,
  }

  flashcard.stage = stage.name
  flashcard.lastReviewDate = chronodate
  flashcard.timesReviewed += 1
  flashcard.reviewHistory.history.push(info)
  flashcard.lastUpdatedAt = new Date()
  return flashcard
}

export function createFlashcardSet(
  name: string,
  language: Language,
): FlashcardSet {
  return {
    id: 0,
    name: name,
    status: flashcardSetStatuses.ACTIVE,
    languageId: language.id,
    createdAt: new Date(),
  }
}

export function getFlashcardSide(isFrontSide: boolean) {
  return isFrontSide ? flashcardSides.FRONT : flashcardSides.BACK
}

export function mapFlashcardSetExtra(flashcardSetExtras: FlashcardSetExtra[]): Map<number, FlashcardSetExtra> {
  return new Map(flashcardSetExtras.map(v => [v.id, v]))
}

export async function fetchFlashcardAudioBlob(
  flashcardSet: FlashcardSet | undefined,
  flashcard: Flashcard | undefined,
  isFrontSide: boolean,
): Promise<Blob | undefined> {
  if (!flashcardSet || !flashcard) return undefined

  const audioStore = useAudioStore()
  const audioCache = useAudioCache()
  const toaster = useSpaceToaster()

  const cachedAudio = audioCache.getAudio(flashcard.id, isFrontSide)
  if (cachedAudio) {
    console.log(`Returning cached audio for flashcard ${flashcard.id}, isFrontSide: ${isFrontSide}`)
    return cachedAudio
  }

  return await sendFlashcardAudioFetchRequest(flashcardSet.id, flashcard.id, getFlashcardSide(isFrontSide))
    .then((response) => {
      const audioId = Number(response.headers['x-audio-id'])
      audioStore.setAudioId(flashcard.id, getFlashcardSide(isFrontSide), audioId)
      audioCache.addAudio(flashcard.id, response.data, isFrontSide)
      return response.data
    })
    .catch((error) => {
      console.error(`Failed to fetch audio for flashcard ${flashcard.id}, isFrontSide: ${isFrontSide}`, error)
      toaster.bakeError(`Couldn't fetch audio`, error.response?.data)
      return undefined
    })
}

export async function uploadFlashcardAudioBlob(
  flashcardSet: FlashcardSet,
  flashcard: Flashcard,
  audioBlob: Blob,
  isFrontSide: boolean,
): Promise<boolean> {
  const audioStore = useAudioStore()
  const audioCache = useAudioCache()
  const toaster = useSpaceToaster()

  const side = getFlashcardSide(isFrontSide)

  return await sendFlashcardAudioUploadRequest(flashcardSet.id, flashcard.id, side, audioBlob)
    .then((response) => {
      console.log(`Audio uploaded ${response.data.id}, size: ${response.data.audioSize}, mime: ${response.data.mimeType}`)
      audioStore.setAudioId(flashcard.id, side, response.data.id)
      audioCache.addAudio(flashcard.id, audioBlob, isFrontSide)
      return true
    })
    .catch((error) => {
      console.error(`Failed to upload audio for flashcard ${flashcard.id}`, error)
      toaster.bakeError(`Couldn't upload audio`, error.response?.data)
      return false
    })
}

export async function removeFlashcardAudioBlob(
  flashcardSet: FlashcardSet,
  flashcard: Flashcard,
  audioId: number,
  isFrontSide: boolean
): Promise<boolean> {
  const audioStore = useAudioStore()
  const audioCache = useAudioCache()
  const toaster = useSpaceToaster()

  return await sendFlashcardAudioRemovalRequest(flashcardSet.id, flashcard.id, audioId)
    .then(() => {
      audioStore.removeAudioId(flashcard.id, getFlashcardSide(isFrontSide))
      audioCache.deleteAudio(flashcard.id, isFrontSide)
      return true
    })
    .catch((error) => {
      console.error(`Failed to remove audio ${audioId} for flashcard ${flashcard.id}`, error)
      toaster.bakeError(`Couldn't remove audio`, error.response?.data)
      return false
    })
}
