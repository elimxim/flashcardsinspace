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

export async function requestFlashcardAudioBlob(
  flashcardSetId: number,
  flashcardId: number,
  flashcardSide: string,
  signal?: AbortSignal,
): Promise<Blob | undefined> {
  const audioStore = useAudioStore()
  const audioCache = useAudioCache()

  const cachedAudio = audioCache.getAudio(flashcardId, flashcardSide)
  if (cachedAudio) {
    Log.log(LogTag.LOGIC, `Returning cached audio for Flashcard.id=${flashcardId}, Flashcard.side=${flashcardSide}`)
    return cachedAudio
  }

  const response = await sendFlashcardAudioGetRequest(flashcardSetId, flashcardId, flashcardSide, signal)
  if (response.status === 204) return undefined
  const audioId = Number(response.headers['x-audio-id'])
  audioStore.setAudioId(flashcardId, flashcardSide, audioId)
  audioCache.addAudio(flashcardId, response.data, flashcardSide)
  return response.data
}

export async function uploadFlashcardAudioBlob(
  flashcardSet: FlashcardSet,
  flashcard: Flashcard,
  audioBlob: Blob,
  flashcardSide: string,
): Promise<boolean> {
  const audioStore = useAudioStore()
  const audioCache = useAudioCache()
  const toaster = useSpaceToaster()

  return await sendFlashcardAudioUploadRequest(flashcardSet.id, flashcard.id, flashcardSide, audioBlob)
    .then((response) => {
      Log.log(LogTag.LOGIC, `Audio.id=${response.data.id} uploaded, Audio.size: ${response.data.audioSize}, Audio.mime: ${response.data.mimeType}`)
      audioStore.setAudioId(flashcard.id, flashcardSide, response.data.id)
      audioCache.addAudio(flashcard.id, audioBlob, flashcardSide)
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
  flashcardSide: string
): Promise<boolean> {
  const audioStore = useAudioStore()
  const audioCache = useAudioCache()
  const toaster = useSpaceToaster()

  return await sendFlashcardAudioRemovalRequest(flashcardSet.id, flashcard.id, audioId)
    .then(() => {
      Log.log(LogTag.LOGIC, `Audio.id=${audioId} removed`)
      audioStore.removeAudioId(flashcard.id, flashcardSide)
      audioCache.deleteAudio(flashcard.id, flashcardSide)
      return true
    })
    .catch((error) => {
      Log.error(LogTag.LOGIC, `Failed to remove Audio.id=${audioId} for Flashcard.id=${flashcard.id}`, error)
      toaster.bakeError(userApiErrors.AUDIO__REMOVAL_FAILED, error.response?.data)
      return false
    })
}
