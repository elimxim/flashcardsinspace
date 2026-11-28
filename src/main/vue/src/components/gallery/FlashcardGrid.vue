<template>
  <div
    ref="scrollArea"
    class="grid-scroll scrollbar-hidden"
    @touchstart.passive="onTouchStart"
    @touchmove="onTouchMove"
    @touchend.passive="onTouchEnd"
    @touchcancel.passive="onTouchEnd"
    @wheel="onWheel"
  >
    <slot v-if="sections.length === 0" name="empty" />
    <section v-for="section in sections" v-else :key="section.key" class="grid-section">
      <div class="grid-section-header">
        <span class="grid-section-title">{{ section.title }}</span>
        <span class="cp-count-box">{{ section.flashcards.length }}</span>
      </div>
      <div class="grid-section-line" />
      <div class="grid" :style="gridStyle">
        <FlashcardGridTile
          v-for="flashcard in section.flashcards"
          :key="flashcard.id"
          :flashcard="flashcard"
          :selecting="selecting"
          :selected="selectedIdSet.has(flashcard.id)"
          :picture="pictures.pictureOf(flashcard.id)"
          :on-click="onTileClick"
        />
      </div>
    </section>
  </div>
</template>

<script lang="ts">
export interface FlashcardGridSection {
  key: string
  title: string
  flashcards: Flashcard[]
}
</script>

<script setup lang="ts">
import FlashcardGridTile from '@/components/gallery/FlashcardGridTile.vue'
import { useGalleryGridPictures } from '@/utils/gallery-grid-pictures.ts'
import {
  galleryGridTileTrackerKey,
  useGalleryGridTileTracker,
} from '@/utils/gallery-grid-tile-tracker.ts'
import { type Flashcard } from '@/model/flashcard.ts'
import { computed, onMounted, onUnmounted, provide, ref, watch } from 'vue'

const props = withDefaults(
  defineProps<{
    sections: FlashcardGridSection[]
    selectedIds?: number[]
    selecting?: boolean
    onTileClick?: (flashcard: Flashcard) => void
  }>(),
  {
    selectedIds: () => [],
    selecting: false,
    onTileClick: () => {},
  },
)

const zoom = defineModel<number>('zoom', { default: 0 })
const selectedIdSet = computed(() => new Set(props.selectedIds))

const DEFAULT_TILE_WIDTH = 180
// stops zooming out sooner on narrow (mobile) screens than the column limit alone would
const MIN_TILE_WIDTH = 90
const MAX_COLUMNS = 12
const GRID_GAP = 6
const FIT_SLACK_PX = 0.05
// a sidebar animates the grid's width; rewrapping the rows on every frame of it
// makes them jump, so the new width is taken once it settles
const RESIZE_SETTLE_MS = 150

const scrollArea = ref<HTMLElement>()
const containerWidth = ref(0)
const tileWidth = ref(DEFAULT_TILE_WIDTH)
const fittedWidth = ref(0)
// the window width the cards were last fitted at
let fittedWindowWidth = 0
let resizeTimeout: ReturnType<typeof setTimeout> | undefined

function maxColumns(width: number): number {
  return Math.max(1, Math.min(MAX_COLUMNS, Math.floor(width / MIN_TILE_WIDTH)))
}

function fitCards() {
  const width = containerWidth.value
  if (width <= 0) return
  fittedWindowWidth = window.innerWidth
  const columns = Math.min(maxColumns(width), Math.max(1, Math.round(width / tileWidth.value)))
  fittedWidth.value = (width - GRID_GAP * (columns - 1)) / columns
}

const tiles = useGalleryGridTileTracker(scrollArea)
provide(galleryGridTileTrackerKey, tiles)

const pictures = useGalleryGridPictures(
  tiles,
  () => props.sections.flatMap((section) => section.flashcards),
  () => Math.min(fittedWidth.value, containerWidth.value),
)

// The browser puts as many cards of the fitted width into a row as fit, and never makes one
// wider than the grid (e.g. a single column fitted before a sidebar opened). The slack keeps
// its own sub-pixel rounding from dropping a column when the cards fit the row exactly.
const gridStyle = computed(() => ({
  gridTemplateColumns:
    fittedWidth.value > 0
      ? `repeat(auto-fill, min(${fittedWidth.value - FIT_SLACK_PX}px, 100%))`
      : undefined,
  gap: `${GRID_GAP}px`,
}))

let pinchStartDistance = 0
let pinchStartTileWidth = 0

function zoomRange(): [number, number] {
  const grid = containerWidth.value
  return [grid / maxColumns(grid), grid]
}

function zoomTo(width: number) {
  if (containerWidth.value <= 0) return
  const [min, max] = zoomRange()
  tileWidth.value = Math.min(max, Math.max(min, width))
  fitCards()
}

function zoomOf(width: number): number {
  const [min, max] = zoomRange()
  if (max <= min) return 0
  return Math.min(1, Math.max(0, Math.log(width / min) / Math.log(max / min)))
}

function touchDistance(touches: TouchList): number {
  const a = touches[0]
  const b = touches[1]
  return Math.hypot(a.clientX - b.clientX, a.clientY - b.clientY)
}

function onTouchStart(event: TouchEvent) {
  if (event.touches.length === 2) {
    pinchStartDistance = touchDistance(event.touches)
    pinchStartTileWidth = tileWidth.value
  }
}

function onTouchMove(event: TouchEvent) {
  if (event.touches.length === 2 && pinchStartDistance > 0) {
    event.preventDefault()
    zoomTo((pinchStartTileWidth * touchDistance(event.touches)) / pinchStartDistance)
  }
}

function onTouchEnd(event: TouchEvent) {
  if (event.touches.length < 2) {
    pinchStartDistance = 0
  }
}

function onWheel(event: WheelEvent) {
  if (!event.ctrlKey) return
  event.preventDefault()
  zoomTo(tileWidth.value * Math.exp(-event.deltaY * 0.01))
}

watch([tileWidth, containerWidth], () => {
  if (containerWidth.value > 0) zoom.value = zoomOf(tileWidth.value)
})

watch(zoom, (value) => {
  if (containerWidth.value <= 0 || Math.abs(value - zoomOf(tileWidth.value)) < 1e-9) return
  const [min, max] = zoomRange()
  zoomTo(min * (max / min) ** value)
  zoom.value = zoomOf(tileWidth.value)
})

// A new window size (or a rotated phone) refits the cards to the grid; a sidebar keeps
// their size and the rows just rewrap. The first width is taken at once, so the grid
// doesn't start out unfitted.
const resizeObserver = new ResizeObserver(([entry]) => {
  const apply = () => {
    containerWidth.value = entry.contentRect.width
    if (window.innerWidth !== fittedWindowWidth) fitCards()
  }
  clearTimeout(resizeTimeout)
  if (containerWidth.value <= 0) {
    apply()
  } else {
    resizeTimeout = setTimeout(apply, RESIZE_SETTLE_MS)
  }
})

onMounted(() => {
  if (scrollArea.value) resizeObserver.observe(scrollArea.value)
})

onUnmounted(() => {
  clearTimeout(resizeTimeout)
  resizeObserver.disconnect()
})
</script>

<style scoped>
.grid-scroll {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  gap: 18px;
  padding: 6px 12px 16px 12px;
  overflow-x: hidden;
  overflow-y: auto;
  overscroll-behavior: contain;
  touch-action: pan-x pan-y;
}

.grid-section {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.grid-section-header {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.grid-section-title {
  color: #2f3340;
  font-size: 1.05rem;
  font-weight: 600;
}

.grid-section-line {
  height: 1px;
  background: rgba(47, 51, 64, 0.2);
  margin-bottom: 4px;
}

.grid {
  display: grid;
}
</style>
