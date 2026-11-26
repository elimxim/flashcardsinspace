import { onMounted, onUnmounted, watch } from 'vue'
import { type Flashcard } from '@/model/flashcard.ts'
import { useFlashcardStore } from '@/stores/flashcard-store.ts'
import { usePictureStore } from '@/stores/picture-store.ts'
import { usePictureThumbnailStore } from '@/stores/picture-thumbnail-store.ts'
import { usePictureCache } from '@/stores/picture-cache.ts'
import {
  requestFlashcardPictureBlob,
  requestFlashcardPictureThumbnails,
} from '@/core-logic/flashcard-picture-logic.ts'
import { flashcardSides } from '@/core-logic/flashcard-logic.ts'
import { isCanceledError } from '@/core-logic/media-error.ts'
import { Log, LogTag } from '@/utils/logger.ts'
import { type GridTileTracker } from '@/utils/grid-tile-tracker.ts'

// Tiles show their front picture. Thumbnails of the whole grid are loaded up front, a page
// at a time; once tiles grow past what a thumbnail renders sharply, the tiles near the
// screen switch to the original pictures, loaded a few at a time into the picture cache,
// which shows them on every tile while zoomed in.

const THUMBNAIL_PAGE_SIZE = 50
const MAX_ORIGINAL_DOWNLOADS = 3

export interface GridPictures {
  pictureOf(flashcardId: number): Blob | undefined
}

export function useGridPictures(
  tiles: GridTileTracker,
  flashcards: () => Flashcard[],
  showOriginals: () => boolean,
): GridPictures {
  const flashcardStore = useFlashcardStore()
  const pictureStore = usePictureStore()
  const thumbnailStore = usePictureThumbnailStore()
  const pictureCache = usePictureCache()
  const front = flashcardSides.FRONT

  const setId = () => flashcardStore.flashcardSet?.id

  function hasFrontPicture(flashcardId: number): boolean {
    return pictureStore.loaded && pictureStore.getPictureId(flashcardId, front) !== undefined
  }

  // --- thumbnails ---

  let thumbnailController: AbortController | undefined
  let loadingThumbnails = false

  // near tiles first, then the rest in grid order
  function missingThumbnails(): number[] {
    const missing = flashcards()
      .map((flashcard) => flashcard.id)
      .filter((id) => hasFrontPicture(id) && thumbnailStore.getThumbnail(id, front) === undefined)
    return [...missing.filter((id) => tiles.isNear(id)), ...missing.filter((id) => !tiles.isNear(id))]
  }

  // one page at a time until nothing is missing; a failed page stops it until the grid changes
  async function loadThumbnails() {
    if (loadingThumbnails) return
    loadingThumbnails = true
    thumbnailController = new AbortController()
    const signal = thumbnailController.signal
    try {
      while (!signal.aborted) {
        const id = setId()
        const page = missingThumbnails().slice(0, THUMBNAIL_PAGE_SIZE)
        if (!id || page.length === 0) break
        await requestFlashcardPictureThumbnails(id, page, front, signal)
      }
    } catch (error) {
      if (!isCanceledError(error)) {
        Log.error(LogTag.SYSTEM, 'Failed to load picture thumbnails', error)
      }
    } finally {
      loadingThumbnails = false
    }
  }

  // --- originals ---

  const downloads = new Map<number, AbortController>()
  // not retried while the gallery is open; their tiles keep the thumbnail
  const failedIds = new Set<number>()

  function cachedOriginal(flashcardId: number): Blob | undefined {
    return pictureCache.peekPicture(flashcardId, front)
  }

  // the near tiles with a front picture that isn't cached yet, closest to the screen first
  function wantedOriginals(): number[] {
    if (!showOriginals()) return []
    const wanted = flashcards()
      .map((flashcard) => flashcard.id)
      .filter((id) => tiles.isNear(id) && hasFrontPicture(id) && cachedOriginal(id) === undefined)
    return tiles.closestFirst(wanted)
  }

  function scheduleOriginals() {
    const wanted = wantedOriginals()
    const wantedSet = new Set(wanted)

    downloads.forEach((controller, id) => {
      if (!wantedSet.has(id)) {
        controller.abort()
        downloads.delete(id)
      }
    })

    for (const id of wanted) {
      if (downloads.size >= MAX_ORIGINAL_DOWNLOADS) break
      if (downloads.has(id) || failedIds.has(id)) continue
      void loadOriginal(id)
    }
  }

  // the picture cache keeps the downloaded original, which shows the tile through it
  async function loadOriginal(flashcardId: number) {
    const id = setId()
    if (!id) return
    const controller = new AbortController()
    downloads.set(flashcardId, controller)
    try {
      const blob = await requestFlashcardPictureBlob(id, flashcardId, front, controller.signal)
      if (!blob && !controller.signal.aborted) failedIds.add(flashcardId)
    } catch (error) {
      if (isCanceledError(error)) return
      Log.error(LogTag.SYSTEM, `Failed to load the picture of Flashcard.id=${flashcardId}`, error)
      failedIds.add(flashcardId)
    } finally {
      if (downloads.get(flashcardId) === controller) {
        downloads.delete(flashcardId)
        scheduleOriginals()
      }
    }
  }

  // reruns when the grid's flashcards, their pictures, the stored thumbnails or the cached
  // originals change (an uploaded or removed picture updates all of them)
  watch(
    () => missingThumbnails().join(','),
    () => loadThumbnails(),
  )
  watch(
    () => wantedOriginals().join(','),
    () => scheduleOriginals(),
  )

  onMounted(() => {
    void loadThumbnails()
  })

  onUnmounted(() => {
    thumbnailController?.abort()
    downloads.forEach((controller) => controller.abort())
    downloads.clear()
  })

  return {
    pictureOf(flashcardId: number): Blob | undefined {
      return (
        (showOriginals() ? cachedOriginal(flashcardId) : undefined) ??
        thumbnailStore.getThumbnail(flashcardId, front) ??
        undefined
      )
    },
  }
}
