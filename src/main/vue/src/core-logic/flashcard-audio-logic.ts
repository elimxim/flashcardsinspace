import type { Flashcard, FlashcardSet } from '@/model/flashcard.ts'
import { useAudioStore } from '@/stores/audio-store.ts'
import { useAudioCache } from '@/stores/audio-cache.ts'
import { useSpaceToaster } from '@/stores/toast-store.ts'
import {
  sendFlashcardAudioGetRequest,
  sendFlashcardAudioRemovalRequest,
  sendFlashcardAudioUploadRequest
} from '@/api/api-client.ts'
import { Log, LogTag } from '@/utils/logger.ts'
import { userApiErrors } from '@/api/user-api-error.ts'
import { Ref } from 'vue'
import { flashcardSides, getFlashcardSide } from '@/core-logic/flashcard-logic.ts'

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
    Log.log(LogTag.LOGIC, `Returning cached audio for Flashcard.id=${flashcardId}, isFrontSide=${isFrontSide}`)
    return cachedAudio
  }

  return await sendFlashcardAudioGetRequest(flashcardSetId, flashcardId, getFlashcardSide(isFrontSide))
    .then((response) => {
      if (response.status === 204) return undefined
      const audioId = Number(response.headers['x-audio-id'])
      audioStore.setAudioId(flashcardId, getFlashcardSide(isFrontSide), audioId)
      audioCache.addAudio(flashcardId, response.data, isFrontSide)
      return response.data
    })
    .catch((error) => {
      Log.error(LogTag.LOGIC, `Failed to fetch audio for Flashcard.id=${flashcardId}, isFrontSide=${isFrontSide}`, error)
      toaster.bakeError(userApiErrors.AUDIO__FETCHING_FAILED, error.response?.data)
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
      Log.log(LogTag.LOGIC, `Audio.id=${response.data.id} uploaded, Audio.size: ${response.data.audioSize}, Audio.mime: ${response.data.mimeType}`)
      audioStore.setAudioId(flashcard.id, side, response.data.id)
      audioCache.addAudio(flashcard.id, audioBlob, isFrontSide)
      return true
    })
    .catch((error) => {
      Log.error(LogTag.LOGIC, `Failed to upload audio for Flashcard.id=${flashcard.id}`, error)
      toaster.bakeError(userApiErrors.AUDIO__UPLOADING_FAILED, error.response?.data)
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
      toaster.bakeError(userApiErrors.AUDIO__REMOVAL_FAILED, error.response?.data)
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
