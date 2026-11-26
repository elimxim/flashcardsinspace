import type { Flashcard, FlashcardSet } from '@/model/flashcard.ts'
import { usePictureStore } from '@/stores/picture-store.ts'
import { usePictureCache } from '@/stores/picture-cache.ts'
import { usePictureThumbnailStore } from '@/stores/picture-thumbnail-store.ts'
import { useSpaceToaster } from '@/stores/toast-store.ts'
import {
  sendFlashcardPictureGetRequest,
  sendFlashcardPictureRemovalRequest,
  sendFlashcardPictureThumbnailsRequest,
  sendFlashcardPictureUploadRequest,
} from '@/api/api-client.ts'
import { Log, LogTag } from '@/utils/logger.ts'
import { userApiErrors } from '@/api/user-api-error.ts'

export async function requestFlashcardPictureBlob(
  flashcardSetId: number,
  flashcardId: number,
  flashcardSide: string,
  signal?: AbortSignal,
): Promise<Blob | undefined> {
  const pictureCache = usePictureCache()

  const cachedPicture = pictureCache.getPicture(flashcardId, flashcardSide)
  if (cachedPicture) {
    Log.log(
      LogTag.LOGIC,
      `Returning cached picture for Flashcard.id=${flashcardId}, Flashcard.side=${flashcardSide}`,
    )
    return cachedPicture
  }

  const response = await sendFlashcardPictureGetRequest(
    flashcardSetId,
    flashcardId,
    flashcardSide,
    signal,
  )

  if (response.status === 204) return undefined

  pictureCache.addPicture(flashcardId, response.data, flashcardSide)
  return response.data
}

export async function requestFlashcardPictureThumbnails(
  flashcardSetId: number,
  flashcardIds: number[],
  flashcardSide: string,
  signal?: AbortSignal,
): Promise<void> {
  const thumbnailStore = usePictureThumbnailStore()
  const response = await sendFlashcardPictureThumbnailsRequest(
    flashcardSetId,
    flashcardIds,
    flashcardSide,
    signal,
  )

  const thumbnails = new Map<number, Blob>()

  if (response.status !== 204) {
    const body = new Response(response.data, {
      headers: { 'Content-Type': String(response.headers['content-type']) },
    })
    const parts = await body.formData()
    parts.forEach((part, name) => {
      if (part instanceof Blob) thumbnails.set(Number(name), part)
    })
  }

  thumbnailStore.switchSet(flashcardSetId)
  flashcardIds.forEach((flashcardId) =>
    thumbnailStore.setThumbnail(flashcardId, flashcardSide, thumbnails.get(flashcardId) ?? null),
  )

  Log.log(
    LogTag.LOGIC,
    `Received ${thumbnails.size} of ${flashcardIds.length} picture thumbnails for FlashcardSet.id=${flashcardSetId}`,
  )
}

export async function uploadFlashcardPictureBlob(
  flashcardSet: FlashcardSet,
  flashcard: Flashcard,
  pictureBlob: Blob,
  flashcardSide: string,
): Promise<boolean> {
  const pictureStore = usePictureStore()
  const pictureCache = usePictureCache()
  const thumbnailStore = usePictureThumbnailStore()
  const toaster = useSpaceToaster()

  Log.log(
    LogTag.LOGIC,
    `Uploading picture for Flashcard.id=${flashcard.id}, Flashcard.side=${flashcardSide}, size=${(pictureBlob.size / 1024).toFixed(1)} KB`,
  )

  return await sendFlashcardPictureUploadRequest(
    flashcardSet.id,
    flashcard.id,
    flashcardSide,
    pictureBlob,
  )
    .then((response) => {
      Log.log(
        LogTag.LOGIC,
        `Picture.id=${response.data.id} uploaded, Picture.size: ${response.data.pictureSize}, Picture.mime: ${response.data.mimeType}`,
      )
      pictureStore.setPictureId(flashcard.id, flashcardSide, response.data.id)
      pictureCache.addPicture(flashcard.id, pictureBlob, flashcardSide)
      thumbnailStore.deleteThumbnail(flashcard.id, flashcardSide)
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
  const thumbnailStore = usePictureThumbnailStore()
  const toaster = useSpaceToaster()

  return await sendFlashcardPictureRemovalRequest(flashcardSet.id, flashcard.id, pictureId)
    .then(() => {
      Log.log(LogTag.LOGIC, `Picture.id=${pictureId} removed`)
      pictureStore.removePictureId(flashcard.id, flashcardSide)
      pictureCache.deletePicture(flashcard.id, flashcardSide)
      thumbnailStore.deleteThumbnail(flashcard.id, flashcardSide)
      return true
    })
    .catch((error) => {
      Log.error(
        LogTag.LOGIC,
        `Failed to remove Picture.id=${pictureId} for Flashcard.id=${flashcard.id}`,
        error,
      )
      toaster.bakeError(userApiErrors.PICTURE__REMOVAL_FAILED, error.response?.data)
      return false
    })
}
