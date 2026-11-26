import { type InjectionKey, onMounted, onUnmounted, type Ref, shallowRef, watch } from 'vue'
import { type Flashcard } from '@/model/flashcard.ts'
import { useFlashcardStore } from '@/stores/flashcard-store.ts'
import { usePictureStore } from '@/stores/picture-store.ts'
import { usePictureThumbnailStore } from '@/stores/picture-thumbnail-store.ts'
import {
  requestFlashcardPictureBlob,
  requestFlashcardPictureThumbnails,
} from '@/core-logic/flashcard-picture-logic.ts'
import { flashcardSides } from '@/core-logic/flashcard-logic.ts'
import { isCanceledError } from '@/core-logic/media-error.ts'
import { Log, LogTag } from '@/utils/logger.ts'

// Tiles show their front picture. Thumbnails of the whole grid are loaded up front, a page
// at a time; once tiles grow past what a thumbnail renders sharply, the tiles on screen and
// half a screen around it switch to the original pictures, loaded a few at a time.

const THUMBNAIL_PAGE_SIZE = 50
const MAX_ORIGINAL_DOWNLOADS = 3
const PRELOAD_MARGIN = '50% 0px' // how far beyond the visible area originals are preloaded

/** Lets each tile report its element, so the grid knows which tiles are on or near the screen */
export interface GridTileTracker {
  track(element: Element, flashcardId: number): void
  untrack(element: Element): void
}

export interface GridPictures {
  tracker: GridTileTracker
  pictureOf(flashcardId: number): Blob | undefined
}

export const gridTileTrackerKey: InjectionKey<GridTileTracker> = Symbol('gridTileTracker')

export function useGridPictures(
  scrollArea: Ref<HTMLElement | undefined>,
  flashcards: () => Flashcard[],
  showOriginals: () => boolean,
): GridPictures {
  const flashcardStore = useFlashcardStore()
  const pictureStore = usePictureStore()
  const thumbnailStore = usePictureThumbnailStore()
  const front = flashcardSides.FRONT

  const setId = () => flashcardStore.flashcardSet?.id

  function hasFrontPicture(flashcardId: number): boolean {
    return pictureStore.loaded && pictureStore.getPictureId(flashcardId, front) !== undefined
  }

  // tiles on the screen or within the preload margin
  const nearIds = shallowRef(new Set<number>())
  const elementIds = new Map<Element, number>()
  const idElements = new Map<number, Element>()
  let observer: IntersectionObserver | undefined

  function onIntersection(entries: IntersectionObserverEntry[]) {
    const next = new Set(nearIds.value)
    entries.forEach((entry) => {
      const id = elementIds.get(entry.target)
      if (id === undefined) return
      if (entry.isIntersecting) {
        next.add(id)
      } else {
        next.delete(id)
      }
    })
    nearIds.value = next
  }

  const tracker: GridTileTracker = {
    track(element, flashcardId) {
      elementIds.set(element, flashcardId)
      idElements.set(flashcardId, element)
      observer?.observe(element)
    },
    untrack(element) {
      const id = elementIds.get(element)
      elementIds.delete(element)
      observer?.unobserve(element)
      if (id === undefined) return
      idElements.delete(id)
      if (nearIds.value.has(id)) {
        const next = new Set(nearIds.value)
        next.delete(id)
        nearIds.value = next
      }
    },
  }

  // --- thumbnails ---

  let thumbnailController: AbortController | undefined
  let loadingThumbnails = false

  // near tiles first, then the rest in grid order
  function missingThumbnails(): number[] {
    const missing = flashcards()
      .map((flashcard) => flashcard.id)
      .filter((id) => hasFrontPicture(id) && thumbnailStore.getThumbnail(id, front) === undefined)
    const near = nearIds.value
    return [...missing.filter((id) => near.has(id)), ...missing.filter((id) => !near.has(id))]
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

  const originals = shallowRef(new Map<number, Blob>())
  const downloads = new Map<number, AbortController>()
  // not retried while the gallery is open; their tiles keep the thumbnail
  const failedIds = new Set<number>()

  function distanceToScreen(flashcardId: number, screen: DOMRect): number {
    const rect = idElements.get(flashcardId)?.getBoundingClientRect()
    if (!rect) return Infinity
    return Math.max(0, screen.top - rect.bottom, rect.top - screen.bottom)
  }

  // the near tiles that have a front picture, closest to the screen first. A tile waits for
  // its thumbnail state, so a replaced picture (its thumbnail deleted) drops the old original.
  function wantedOriginals(): number[] {
    const root = scrollArea.value
    if (!showOriginals() || !root) return []
    const screen = root.getBoundingClientRect()
    const near = nearIds.value
    return flashcards()
      .filter(
        (flashcard) =>
          near.has(flashcard.id) &&
          hasFrontPicture(flashcard.id) &&
          thumbnailStore.getThumbnail(flashcard.id, front) !== undefined,
      )
      .map((flashcard) => ({ id: flashcard.id, distance: distanceToScreen(flashcard.id, screen) }))
      .sort((a, b) => a.distance - b.distance)
      .map((tile) => tile.id)
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
    // tiles that left keep only their thumbnail on screen; the original stays in the picture cache
    if ([...originals.value.keys()].some((id) => !wantedSet.has(id))) {
      originals.value = new Map([...originals.value].filter(([id]) => wantedSet.has(id)))
    }

    for (const id of wanted) {
      if (downloads.size >= MAX_ORIGINAL_DOWNLOADS) break
      if (originals.value.has(id) || downloads.has(id) || failedIds.has(id)) continue
      void loadOriginal(id)
    }
  }

  async function loadOriginal(flashcardId: number) {
    const id = setId()
    if (!id) return
    const controller = new AbortController()
    downloads.set(flashcardId, controller)
    try {
      const blob = await requestFlashcardPictureBlob(id, flashcardId, front, controller.signal)
      if (controller.signal.aborted) return
      if (blob) {
        originals.value = new Map(originals.value).set(flashcardId, blob)
      } else {
        failedIds.add(flashcardId)
      }
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

  // reruns when the grid's flashcards, their pictures or the stored thumbnails change
  // (an uploaded or removed picture deletes its thumbnail)
  watch(
    () => missingThumbnails().join(','),
    () => loadThumbnails(),
  )
  watch(
    () => wantedOriginals().join(','),
    () => scheduleOriginals(),
  )

  onMounted(() => {
    observer = new IntersectionObserver(onIntersection, {
      root: scrollArea.value,
      rootMargin: PRELOAD_MARGIN,
    })
    elementIds.forEach((_, element) => observer?.observe(element))
    void loadThumbnails()
  })

  onUnmounted(() => {
    observer?.disconnect()
    thumbnailController?.abort()
    downloads.forEach((controller) => controller.abort())
    downloads.clear()
  })

  return {
    tracker,
    pictureOf(flashcardId: number): Blob | undefined {
      return (
        originals.value.get(flashcardId) ??
        thumbnailStore.getThumbnail(flashcardId, front) ??
        undefined
      )
    },
  }
}
