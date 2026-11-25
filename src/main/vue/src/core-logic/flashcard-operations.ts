import { type Flashcard } from '@/model/flashcard.ts'
import {
  sendFlashcardBulkRemovalRequest,
  sendFlashcardBulkUpdateRequest,
  sendFlashcardRemovalRequest,
  sendFlashcardUpdateRequest,
} from '@/api/api-client.ts'
import { copyFlashcard, flashcardSides, updateFlashcard } from '@/core-logic/flashcard-logic.ts'
import { learningStages } from '@/core-logic/stage-logic.ts'
import { useFlashcardStore } from '@/stores/flashcard-store.ts'
import { useFlashcardSetStore } from '@/stores/flashcard-set-store.ts'
import { useAudioStore } from '@/stores/audio-store.ts'
import { useAudioCache } from '@/stores/audio-cache.ts'
import { usePictureStore } from '@/stores/picture-store.ts'
import { usePictureCache } from '@/stores/picture-cache.ts'

/**
 * Removes the flashcard on the server, then from every store and cache that knows it
 * (the server removes its audio and pictures along with it). Rejects with the request error.
 */
export async function removeFlashcard(setId: number, flashcardId: number): Promise<void> {
  await sendFlashcardRemovalRequest(setId, flashcardId)
  forgetFlashcards(setId, [flashcardId])
}

/** {@link removeFlashcard} for several flashcards at once: all of them are removed or none is. */
export async function removeFlashcards(setId: number, flashcardIds: number[]): Promise<void> {
  await sendFlashcardBulkRemovalRequest(setId, flashcardIds)
  forgetFlashcards(setId, flashcardIds)
}

function forgetFlashcards(setId: number, flashcardIds: number[]) {
  const flashcardStore = useFlashcardStore()
  const flashcardSetStore = useFlashcardSetStore()
  const audioStore = useAudioStore()
  const audioCache = useAudioCache()
  const pictureStore = usePictureStore()
  const pictureCache = usePictureCache()
  for (const flashcardId of flashcardIds) {
    flashcardStore.removeFlashcard(flashcardId)
    flashcardSetStore.decrementFlashcardsNumber(setId)
    for (const side of [flashcardSides.FRONT, flashcardSides.BACK]) {
      audioStore.removeAudioId(flashcardId, side)
      audioCache.deleteAudio(flashcardId, side)
      pictureStore.removePictureId(flashcardId, side)
      pictureCache.deletePicture(flashcardId, side)
    }
  }
}

/**
 * Sends an Outer space flashcard back to S1 on the given day and stores the result.
 * Rejects with the request error.
 */
export async function moveFlashcardBack(
  setId: number,
  flashcard: Flashcard,
  chronodate: string,
): Promise<Flashcard> {
  const response = await sendFlashcardUpdateRequest(setId, movedBack(flashcard, chronodate))
  useFlashcardStore().changeFlashcard(response.data)
  return response.data
}

/** {@link moveFlashcardBack} for several flashcards at once: all of them move or none does. */
export async function moveFlashcardsBack(
  setId: number,
  flashcards: Flashcard[],
  chronodate: string,
): Promise<Flashcard[]> {
  const moved = flashcards.map((flashcard) => movedBack(flashcard, chronodate))
  const response = await sendFlashcardBulkUpdateRequest(setId, moved)
  const flashcardStore = useFlashcardStore()
  response.data.flashcards.forEach((flashcard) => flashcardStore.changeFlashcard(flashcard))
  return response.data.flashcards
}

function movedBack(flashcard: Flashcard, chronodate: string): Flashcard {
  return updateFlashcard(copyFlashcard(flashcard), learningStages.S1, chronodate)
}
