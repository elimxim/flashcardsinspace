import { type InjectionKey, onMounted, onUnmounted, type Ref, shallowRef } from 'vue'

const NEAR_MARGIN = '50% 0px' // how far beyond the visible area a tile counts as near

export interface GridTileTracker {
  track(element: Element, flashcardId: number): void
  untrack(element: Element): void
  /** Whether the tile is on the screen or within the near margin; reactive */
  isNear(flashcardId: number): boolean
  /** The given flashcards' tiles ordered by their distance to the screen, closest first */
  closestFirst(flashcardIds: number[]): number[]
}

export const gridTileTrackerKey: InjectionKey<GridTileTracker> = Symbol('gridTileTracker')

export function useGridTileTracker(scrollArea: Ref<HTMLElement | undefined>): GridTileTracker {
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

  function distanceToScreen(flashcardId: number, screen: DOMRect): number {
    const rect = idElements.get(flashcardId)?.getBoundingClientRect()
    if (!rect) return Infinity
    return Math.max(0, screen.top - rect.bottom, rect.top - screen.bottom)
  }

  onMounted(() => {
    observer = new IntersectionObserver(onIntersection, {
      root: scrollArea.value,
      rootMargin: NEAR_MARGIN,
    })
    // the tiles mount before the grid, so they have been tracked already
    elementIds.forEach((_, element) => observer?.observe(element))
  })

  onUnmounted(() => {
    observer?.disconnect()
  })

  return {
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
    isNear(flashcardId) {
      return nearIds.value.has(flashcardId)
    },
    closestFirst(flashcardIds) {
      const root = scrollArea.value
      if (!root) return flashcardIds
      const screen = root.getBoundingClientRect()
      return flashcardIds
        .map((id) => ({ id, distance: distanceToScreen(id, screen) }))
        .sort((a, b) => a.distance - b.distance)
        .map((tile) => tile.id)
    },
  }
}
