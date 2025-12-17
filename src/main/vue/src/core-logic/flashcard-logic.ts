import {
  type Flashcard,
  type FlashcardSet,
  FlashcardSetExtra,
  type ReviewInfo
} from '@/model/flashcard.ts'
import { type Stage, learningStages } from '@/core-logic/stage-logic.ts'
import type { Language } from '@/model/language.ts'
import {
  sendFlashcardAudioFetchRequest,
  sendFlashcardAudioRemovalRequest,
  sendFlashcardAudioUploadRequest
} from '@/api/api-client.ts'
import { useSpaceToaster } from '@/stores/toast-store.ts'
import { useAudioCache } from '@/stores/audio-cache.ts'
import { useAudioStore } from '@/stores/audio-store.ts'
import { Ref } from 'vue'
import { Log, LogTag } from '@/utils/logger.ts';

export const flashcardSetStatuses = {
  ACTIVE: 'ACTIVE',
  DELETED: 'DELETED',
  SUSPENDED: 'SUSPENDED',
}

export const flashcardSides = {
  FRONT: 'FRONT',
  BACK: 'BACK',
}

export function newFlashcard(frontSide: string, backSide: string, chronodate: string): Flashcard {
  return {
    id: 0,
    frontSide: frontSide,
    backSide: backSide,
    stage: learningStages.S1.name,
    timesReviewed: 0,
    reviewHistory: { history: [] },
    creationDate: chronodate,
  }
}

export function changeFlashcardSides(flashcard: Flashcard, frontSide: string, backSide: string): boolean {
  let changed = false
  if (flashcard.frontSide !== frontSide) {
    flashcard.frontSide = frontSide
    changed = true
  }
  if (flashcard.backSide !== backSide) {
    flashcard.backSide = backSide
    changed = true
  }

  if (changed) {
    flashcard.lastUpdatedAt = new Date()
  }

  return changed
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

export function updateFlashcard(flashcard: Flashcard, stage: Stage, chronodate: string): Flashcard {
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
  flashcardSetId: number,
  flashcardId: number,
  isFrontSide: boolean,
): Promise<Blob | undefined> {
  const audioStore = useAudioStore()
  const audioCache = useAudioCache()
  const toaster = useSpaceToaster()

  const cachedAudio = audioCache.getAudio(flashcardId, isFrontSide)
  if (cachedAudio) {
    Log.error(LogTag.LOGIC, `Returning cached audio for Flashcard.id=${flashcardId}, isFrontSide=${isFrontSide}`)
    return cachedAudio
  }

  return await sendFlashcardAudioFetchRequest(flashcardSetId, flashcardId, getFlashcardSide(isFrontSide))
    .then((response) => {
      const audioId = Number(response.headers['x-audio-id'])
      audioStore.setAudioId(flashcardId, getFlashcardSide(isFrontSide), audioId)
      audioCache.addAudio(flashcardId, response.data, isFrontSide)
      return response.data
    })
    .catch((error) => {
      Log.error(LogTag.LOGIC, `Failed to fetch audio for Flashcard.id=${flashcardId}, isFrontSide=${isFrontSide}`, error)
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
      console.log(`Audio.id=${response.data.id} uploaded, Audio.size: ${response.data.audioSize}, Audio.mime: ${response.data.mimeType}`)
      audioStore.setAudioId(flashcard.id, side, response.data.id)
      audioCache.addAudio(flashcard.id, audioBlob, isFrontSide)
      return true
    })
    .catch((error) => {
      Log.error(LogTag.LOGIC, `Failed to upload audio for Flashcard.id=${flashcard.id}`, error)
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
      Log.error(LogTag.LOGIC, `Failed to remove Audio.id=${audioId} for Flashcard.id=${flashcard.id}`, error)
      toaster.bakeError(`Couldn't remove audio`, error.response?.data)
      return false
    })
}

export async function fetchFlashcardAudio(
  flashcardSetId: number | undefined,
  flashcardId: number | undefined,
  flashcardFrontSideAudioBlob: Ref<Blob | undefined>,
  flashcardBackSideAudioBlob: Ref<Blob | undefined>,
) {
  if (!flashcardSetId || !flashcardId) {
    flashcardFrontSideAudioBlob.value = undefined
    flashcardBackSideAudioBlob.value = undefined
    return
  }

  const audioStore = useAudioStore()

  await Promise.all([
    (async function () {
      const frontSideAudioId = audioStore.getAudioId(flashcardId, flashcardSides.FRONT)
      if (frontSideAudioId) {
        return await fetchFlashcardAudioBlob(flashcardSetId, flashcardId, true)
          .then((blob) => {
            flashcardFrontSideAudioBlob.value = blob
          })
      } else {
        flashcardFrontSideAudioBlob.value = undefined
      }
    })(),
    (async function () {
      const backSideAudioId = audioStore.getAudioId(flashcardId, flashcardSides.BACK)
      if (backSideAudioId) {
        return await fetchFlashcardAudioBlob(flashcardSetId, flashcardId, false)
          .then((blob) => {
            flashcardBackSideAudioBlob.value = blob
          })
      } else {
        flashcardBackSideAudioBlob.value = undefined
      }
    })(),
  ])
}
