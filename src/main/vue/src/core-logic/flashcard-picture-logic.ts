import type { Flashcard, FlashcardSet } from '@/model/flashcard.ts'
import { usePictureStore } from '@/stores/picture-store.ts'
import { usePictureCache } from '@/stores/picture-cache.ts'
import { useSpaceToaster } from '@/stores/toast-store.ts'
import {
  sendFlashcardPictureGetRequest,
  sendFlashcardPictureRemovalRequest,
  sendFlashcardPictureUploadRequest,
} from '@/api/api-client.ts'
import { Log, LogTag } from '@/utils/logger.ts'
import { userApiErrors } from '@/api/user-api-error.ts'
import { Ref } from 'vue'
import { flashcardSides } from '@/core-logic/flashcard-logic.ts'

export async function fetchFlashcardPictureBlob(
  flashcardSetId: number,
  flashcardId: number,
  flashcardSide: string,
): Promise<Blob | undefined> {
  const pictureCache = usePictureCache()
  const toaster = useSpaceToaster()

  const cachedPicture = pictureCache.getPicture(flashcardId, flashcardSide)
  if (cachedPicture) {
    Log.log(LogTag.LOGIC, `Returning cached picture for Flashcard.id=${flashcardId}, Flashcard.side=${flashcardSide}`)
    return cachedPicture
  }

  return await sendFlashcardPictureGetRequest(flashcardSetId, flashcardId, flashcardSide)
    .then((response) => {
      if (response.status === 204) return undefined
      pictureCache.addPicture(flashcardId, response.data, flashcardSide)
      return response.data
    })
    .catch((error) => {
      Log.error(LogTag.LOGIC, `Failed to fetch picture for Flashcard.id=${flashcardId}, Flashcard.side=${flashcardSide}`, error)
      toaster.bakeError(userApiErrors.PICTURE__FETCHING_FAILED, error.response?.data)
      return undefined
    })
}

export async function uploadFlashcardPictureBlob(
  flashcardSet: FlashcardSet,
  flashcard: Flashcard,
  pictureBlob: Blob,
  flashcardSide: string,
): Promise<boolean> {
  const pictureStore = usePictureStore()
  const pictureCache = usePictureCache()
  const toaster = useSpaceToaster()

  Log.log(LogTag.LOGIC, `Uploading picture for Flashcard.id=${flashcard.id}, Flashcard.side=${flashcardSide}, size=${(pictureBlob.size / 1024).toFixed(1)} KB`)

  return await sendFlashcardPictureUploadRequest(flashcardSet.id, flashcard.id, flashcardSide, pictureBlob)
    .then((response) => {
      Log.log(LogTag.LOGIC, `Picture.id=${response.data.id} uploaded, Picture.size: ${response.data.pictureSize}, Picture.mime: ${response.data.mimeType}`)
      pictureStore.setPictureId(flashcard.id, flashcardSide, response.data.id)
      pictureCache.addPicture(flashcard.id, pictureBlob, flashcardSide)
      return true
    })
    .catch((error) => {
      Log.error(LogTag.LOGIC, `Failed to upload picture for Flashcard.id=${flashcard.id}`, error)
      if (error.response?.status === 413) {
        toaster.bakeError(userApiErrors.PICTURE__TOO_LARGE_AFTER_COMPRESSION, error.response?.data)
      } else {
        toaster.bakeError(userApiErrors.PICTURE__UPLOADING_FAILED, error.response?.data)
      }
      return false
    })
}

export async function removeFlashcardPictureBlob(
  flashcardSet: FlashcardSet,
  flashcard: Flashcard,
  pictureId: number,
  flashcardSide: string,
): Promise<boolean> {
  const pictureStore = usePictureStore()
  const pictureCache = usePictureCache()
  const toaster = useSpaceToaster()

  return await sendFlashcardPictureRemovalRequest(flashcardSet.id, flashcard.id, pictureId)
    .then(() => {
      Log.log(LogTag.LOGIC, `Picture.id=${pictureId} removed`)
      pictureStore.removePictureId(flashcard.id, flashcardSide)
      pictureCache.deletePicture(flashcard.id, flashcardSide)
      return true
    })
    .catch((error) => {
      Log.error(LogTag.LOGIC, `Failed to remove Picture.id=${pictureId} for Flashcard.id=${flashcard.id}`, error)
      toaster.bakeError(userApiErrors.PICTURE__REMOVAL_FAILED, error.response?.data)
      return false
    })
}

export async function fetchFlashcardPicture(
  flashcardSetId: number | undefined,
  flashcardId: number | undefined,
  flashcardFrontSidePictureBlob: Ref<Blob | undefined>,
  flashcardBackSidePictureBlob: Ref<Blob | undefined>,
) {
  if (!flashcardSetId || !flashcardId) {
    flashcardFrontSidePictureBlob.value = undefined
    flashcardBackSidePictureBlob.value = undefined
    return
  }

  const pictureStore = usePictureStore()

  await Promise.all([
    (async function () {
      const frontSidePictureId = pictureStore.getPictureId(flashcardId, flashcardSides.FRONT)
      if (frontSidePictureId) {
        return await fetchFlashcardPictureBlob(flashcardSetId, flashcardId, flashcardSides.FRONT)
          .then((blob) => {
            flashcardFrontSidePictureBlob.value = blob
          })
      } else {
        flashcardFrontSidePictureBlob.value = undefined
      }
    })(),
    (async function () {
      const backSidePictureId = pictureStore.getPictureId(flashcardId, flashcardSides.BACK)
      if (backSidePictureId) {
        return await fetchFlashcardPictureBlob(flashcardSetId, flashcardId, flashcardSides.BACK)
          .then((blob) => {
            flashcardBackSidePictureBlob.value = blob
          })
      } else {
        flashcardBackSidePictureBlob.value = undefined
      }
    })(),
  ])
}
