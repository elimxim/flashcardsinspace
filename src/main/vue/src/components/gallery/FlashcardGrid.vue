<template>
  <div ref="scrollArea" class="grid-scroll scrollbar-hidden">
    <slot v-if="sections.length === 0" name="empty" />
    <section v-for="section in sections" v-else :key="section.key" class="grid-section">
      <div class="grid-section-header">
        <span class="grid-section-title">{{ section.title }}</span>
        <span class="cp-count-box">{{ section.flashcards.length }}</span>
      </div>
      <div class="grid-section-line" />
      <div class="grid" :style="gridStyle">
        <button
          v-for="flashcard in section.flashcards"
          :key="flashcard.id"
          type="button"
          class="grid-tile"
          :class="{
            'grid-tile--selecting': selecting,
            'grid-tile--selected': selectedIdSet.has(flashcard.id),
          }"
          @click="onTileClick(flashcard)"
        >
          <span class="grid-tile-text">{{ flashcard.frontSide }}</span>
          <font-awesome-icon
            v-if="selectedIdSet.has(flashcard.id)"
            icon="fa-solid fa-circle-check"
            class="grid-tile-check"
          />
        </button>
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
import { type Flashcard } from '@/model/flashcard.ts'
import { computed, onMounted, onUnmounted, ref } from 'vue'

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

const selectedIdSet = computed(() => new Set(props.selectedIds))

// Cards are fitted to the grid's width: whole columns near the tile width that pinching
// (or ctrl + wheel, which is what a trackpad pinch sends) aims at. They are refitted only
// on zoom and on window resize, so opening or closing a sidebar never resizes them; only
// the number of cards per row changes.

const DEFAULT_TILE_WIDTH = 180
// stops zooming out sooner on narrow (mobile) screens than the column limit alone would
const MIN_TILE_WIDTH = 90
const MAX_COLUMNS = 12
const GRID_GAP = 6
// a sidebar animates the grid's width; rewrapping the rows on every frame of it
// makes them jump, so the new width is taken once it settles
const RESIZE_SETTLE_MS = 150

const scrollArea = ref<HTMLElement>()
const containerWidth = ref(0)
const tileWidth = ref(DEFAULT_TILE_WIDTH)
const fittedWidth = ref(0)
let scrollPaddingX = 0
let resizeTimeout: ReturnType<typeof setTimeout> | undefined
let windowResizeTimeout: ReturnType<typeof setTimeout> | undefined

function maxColumns(width: number): number {
  return Math.max(1, Math.min(MAX_COLUMNS, Math.floor(width / MIN_TILE_WIDTH)))
}

function gridWidth(): number {
  const el = scrollArea.value
  return el ? Math.max(0, el.clientWidth - scrollPaddingX) : 0
}

function fitCards() {
  const width = containerWidth.value
  if (width <= 0) return
  const columns = Math.min(maxColumns(width), Math.max(1, Math.round(width / tileWidth.value)))
  fittedWidth.value = (width - GRID_GAP * (columns - 1)) / columns
}

// never wider than the grid, e.g. a single column fitted before the sidebar opened
const cardWidth = computed(() => Math.min(fittedWidth.value, containerWidth.value))

// how many cards of that width fit into the grid (epsilon guards float rounding)
const columns = computed(() => {
  const card = cardWidth.value
  if (card <= 0) return 1
  return Math.max(1, Math.floor((containerWidth.value + GRID_GAP) / (card + GRID_GAP) + 0.001))
})

const gridStyle = computed(() => ({
  gridTemplateColumns: `repeat(${columns.value}, ${cardWidth.value}px)`,
  gap: `${GRID_GAP}px`,
}))

let pinchStartDistance = 0
let pinchStartTileWidth = 0

// clamped to what the column limits allow, so zooming past a limit
// doesn't pile up width that has to be unwound later
function zoomTo(width: number) {
  const grid = containerWidth.value
  if (grid <= 0) return
  tileWidth.value = Math.min(grid, Math.max(grid / maxColumns(grid), width))
  fitCards()
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

// a sidebar keeps the cards' size, the rows just rewrap
const resizeObserver = new ResizeObserver(() => {
  clearTimeout(resizeTimeout)
  resizeTimeout = setTimeout(() => (containerWidth.value = gridWidth()), RESIZE_SETTLE_MS)
})

// a new window size (or a rotated phone) refits the cards to the grid
function onWindowResize() {
  clearTimeout(windowResizeTimeout)
  windowResizeTimeout = setTimeout(() => {
    containerWidth.value = gridWidth()
    fitCards()
  }, RESIZE_SETTLE_MS)
}

onMounted(() => {
  const el = scrollArea.value
  if (!el) return
  const style = getComputedStyle(el)
  scrollPaddingX = parseFloat(style.paddingLeft) + parseFloat(style.paddingRight)
  containerWidth.value = gridWidth()
  fitCards()
  window.addEventListener('resize', onWindowResize)
  resizeObserver.observe(el)
  el.addEventListener('touchstart', onTouchStart, { passive: true })
  el.addEventListener('touchmove', onTouchMove, { passive: false })
  el.addEventListener('touchend', onTouchEnd, { passive: true })
  el.addEventListener('touchcancel', onTouchEnd, { passive: true })
  el.addEventListener('wheel', onWheel, { passive: false })
})

onUnmounted(() => {
  clearTimeout(resizeTimeout)
  clearTimeout(windowResizeTimeout)
  window.removeEventListener('resize', onWindowResize)
  resizeObserver.disconnect()
  const el = scrollArea.value
  if (!el) return
  el.removeEventListener('touchstart', onTouchStart)
  el.removeEventListener('touchmove', onTouchMove)
  el.removeEventListener('touchend', onTouchEnd)
  el.removeEventListener('touchcancel', onTouchEnd)
  el.removeEventListener('wheel', onWheel)
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

.grid-tile {
  position: relative;
  aspect-ratio: 4 / 3;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 8px;
  border: none;
  border-radius: 10px;
  background: white;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.12);
  overflow: hidden;
  container-type: inline-size;
  outline: none;
  cursor: zoom-in;
  transition: box-shadow 0.2s ease-in-out;
}

@media (hover: hover) {
  .grid-tile:hover {
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
  }
}

.grid-tile--selecting {
  cursor: pointer;
}

.grid-tile--selected {
  background: #f5f5f8;
}

.grid-tile--selected .grid-tile-text {
  opacity: 0.7;
}

.grid-tile-check {
  position: absolute;
  right: 6px;
  bottom: 6px;
  font-size: clamp(14px, 12cqi, 24px);
  color: #007bff;
  background: white;
  border-radius: 50%;
}

.grid-tile-text {
  max-height: 100%;
  overflow: hidden;
  color: #2f3340;
  font-size: clamp(0.5rem, 11cqi, 1.3rem);
  line-height: 1.25;
  text-align: center;
  overflow-wrap: anywhere;
}
</style>
