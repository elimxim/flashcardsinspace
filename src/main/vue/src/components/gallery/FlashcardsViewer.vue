<template>
  <div class="viewer">
    <div class="viewer-toolbar">
      <span class="viewer-toolbar-info">{{ infoText }}</span>
      <button
        type="button"
        class="viewer-select-button viewer-select-button--fixed"
        :class="{ 'viewer-select-button--active': selecting }"
        :disabled="!loaded || (!selecting && visibleCount === 0)"
        @click="toggleSelecting"
      >
        {{ selecting ? 'Cancel' : 'Select' }}
      </button>
    </div>
    <div ref="scrollArea" class="viewer-scroll scrollbar-hidden">
      <KineticRingSpinner v-if="resolvedLoading" class="viewer-spinner" />
      <template v-else-if="!loaded" />
      <div v-else-if="!flashcardSet" class="viewer-empty">No flashcard set selected</div>
      <div v-else-if="flashcards.length === 0" class="viewer-empty">
        This flashcard set has no flashcards yet
      </div>
      <div v-else-if="categories.length === 0" class="viewer-empty">
        <span>No flashcards match the filters</span>
        <button type="button" class="viewer-select-button" @click="clearFilter">
          Clear filters
        </button>
      </div>
      <section
        v-for="category in categories"
        v-else
        :key="category.stage.name"
        class="viewer-category"
      >
        <div class="viewer-category-header">
          <span class="viewer-category-name">{{ category.stage.displayName }}</span>
          <span class="cp-count-box">{{ category.flashcards.length }}</span>
        </div>
        <div class="viewer-category-line" />
        <div class="viewer-grid" :style="gridStyle">
          <button
            v-for="flashcard in category.flashcards"
            :key="flashcard.id"
            type="button"
            class="viewer-tile"
            :class="{
              'viewer-tile--selecting': selecting,
              'viewer-tile--selected': selectedIdSet.has(flashcard.id),
            }"
            @click="onTileClick(flashcard)"
          >
            <span class="viewer-tile-text">{{ flashcard.frontSide }}</span>
            <font-awesome-icon
              v-if="selectedIdSet.has(flashcard.id)"
              icon="fa-solid fa-circle-check"
              class="viewer-tile-check"
            />
          </button>
        </div>
      </section>
    </div>
    <ZoomOverlay
      v-model:open="zoomOpen"
      :z-index="850"
      :close-on-escape="!flashcardEditOpen"
    >
      <div v-if="zoomedFlashcard" class="viewer-zoomed-card">
        <SpaceCard
          :key="zoomedFlashcard.id"
          v-model:auto-play-voice="autoPlayVoice"
          v-model:auto-repeat-voice="autoRepeatVoice"
          :stage="zoomedFlashcard.stage"
          :front-side="zoomedFlashcard.frontSide"
          :back-side="zoomedFlashcard.backSide"
          :front-side-audio="zoomedMedia.frontAudio"
          :back-side-audio="zoomedMedia.backAudio"
          :front-side-picture="zoomedMedia.frontPicture"
          :back-side-picture="zoomedMedia.backPicture"
          :viewed-times="zoomedFlashcard.timesReviewed"
          :on-edit="openEdit"
        />
      </div>
    </ZoomOverlay>
    <FlashcardEditModal
      v-model:flashcard="editedFlashcard"
      v-model:removed="editedFlashcardRemoved"
      v-model:audio-changed="editedMediaChanged"
      v-model:picture-changed="editedMediaChanged"
    />
  </div>
</template>

<script lang="ts">
export interface GalleryFilter {
  unknown: boolean
  attempted: boolean
  stages: string[]
}
</script>

<script setup lang="ts">
import KineticRingSpinner from '@/components/spinners/KineticRingSpinner.vue'
import SpaceCard from '@/components/SpaceCard.vue'
import ZoomOverlay from '@/components/common/ZoomOverlay.vue'
import FlashcardEditModal from '@/modals/FlashcardEditModal.vue'
import { type Flashcard } from '@/model/flashcard.ts'
import { useFlashcardStore } from '@/stores/flashcard-store.ts'
import { useChronoStore } from '@/stores/chrono-store.ts'
import { useToggleStore } from '@/stores/toggle-store.ts'
import { useSpaceToaster } from '@/stores/toast-store.ts'
import { useFlashcardSetLoading } from '@/stores/flashcard-set-loading.ts'
import { storeToRefs } from 'pinia'
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { type Stage, allStages, specialStages } from '@/core-logic/stage-logic.ts'
import { flashcardStageOn } from '@/core-logic/review-logic.ts'
import {
  type FlashcardMedia,
  fetchFlashcardMedia,
  NO_MEDIA,
} from '@/core-logic/flashcard-media-prefetch.ts'
import { errorResponseData } from '@/core-logic/media-error.ts'
import { userApiErrors } from '@/api/user-api-error.ts'

const filter = defineModel<GalleryFilter>('filter', { required: true })
const selectedIds = defineModel<number[]>('selectedIds', { required: true })

const flashcardStore = useFlashcardStore()
const chronoStore = useChronoStore()
const toggleStore = useToggleStore()

const { flashcardSet, flashcards } = storeToRefs(flashcardStore)
const { currDay } = storeToRefs(chronoStore)
const { flashcardEditOpen } = storeToRefs(toggleStore)
const { resolvedLoading, loaded } = storeToRefs(useFlashcardSetLoading())

interface GalleryCategory {
  stage: Stage
  flashcards: Flashcard[]
}

function isShown(stage: Stage): boolean {
  const { unknown, attempted, stages } = filter.value
  if (!unknown && !attempted && stages.length === 0) return true
  if (stage === specialStages.UNKNOWN) return unknown
  if (stage === specialStages.ATTEMPTED) return attempted
  return stages.includes(stage.name)
}

const categories = computed<GalleryCategory[]>(() => {
  const groups = new Map<Stage, Flashcard[]>()
  flashcards.value.forEach((flashcard) => {
    const stage = flashcardStageOn(flashcard, currDay.value)
    if (!isShown(stage)) return
    const group = groups.get(stage)
    if (group) {
      group.push(flashcard)
    } else {
      groups.set(stage, [flashcard])
    }
  })
  return allStages
    .filter((stage) => groups.has(stage))
    .map((stage) => ({ stage, flashcards: groups.get(stage)!.sort((a, b) => a.id - b.id) }))
})
const visibleCount = computed(() =>
  categories.value.reduce((sum, category) => sum + category.flashcards.length, 0),
)

const selecting = ref(false)
const selectedIdSet = computed(() => new Set(selectedIds.value))

const infoText = computed(() => {
  if (!loaded.value) return ''
  if (!selecting.value) {
    const count = visibleCount.value
    if (count === 0) return ''
    return count === 1 ? '1 flashcard' : `${count} flashcards`
  }
  const count = selectedIds.value.length
  return count > 0 ? `${count} selected` : 'Select flashcards'
})

const zoomOpen = ref(false)
const zoomedFlashcard = ref<Flashcard>()
const zoomedMedia = ref<FlashcardMedia>(NO_MEDIA)

let zoomedMediaFlashcardId: number | undefined
let mediaController: AbortController | undefined

const autoPlayVoice = ref(false)
const autoRepeatVoice = ref(false)
const editedFlashcard = ref<Flashcard>()
const editedFlashcardRemoved = ref(false)
const editedMediaChanged = ref(false)

function toggleSelecting() {
  selecting.value = !selecting.value
  if (!selecting.value) {
    selectedIds.value = []
  }
}

function toggleSelected(id: number) {
  selectedIds.value = selectedIdSet.value.has(id)
    ? selectedIds.value.filter((v) => v !== id)
    : [...selectedIds.value, id]
}

function onTileClick(flashcard: Flashcard) {
  if (selecting.value) {
    toggleSelected(flashcard.id)
  } else {
    zoomedFlashcard.value = flashcard
    zoomOpen.value = true
    loadZoomedMedia(flashcard.id)
  }
}

async function loadZoomedMedia(flashcardId: number) {
  mediaController?.abort()
  // refetching the same flashcard keeps showing its current media until the new one arrives
  if (zoomedMediaFlashcardId !== flashcardId) {
    zoomedMedia.value = NO_MEDIA
    zoomedMediaFlashcardId = undefined
  }
  const setId = flashcardSet.value?.id
  if (!setId) return

  const controller = new AbortController()
  mediaController = controller
  const media = await fetchFlashcardMedia(setId, flashcardId, controller.signal)
  // the user closed it or opened another flashcard while this was in flight
  if (controller.signal.aborted || zoomedFlashcard.value?.id !== flashcardId) return

  zoomedMedia.value = media
  zoomedMediaFlashcardId = flashcardId
  if (media.error !== undefined) {
    useSpaceToaster().bakeError(
      userApiErrors.MEDIA__FETCHING_FAILED,
      errorResponseData(media.error),
    )
  }
}

// The zoomed flashcard stays open under the edit modal and shows the result
// once the modal closes (or closes too, when the flashcard was removed).
function openEdit() {
  editedFlashcard.value = zoomedFlashcard.value
  toggleStore.toggleFlashcardEdit()
}

watch(zoomOpen, (open) => {
  if (!open) mediaController?.abort()
})

watch(flashcardEditOpen, (open) => {
  if (open) return
  const id = editedFlashcard.value?.id
  const flashcard = id !== undefined ? flashcardStore.flashcardMap.get(id) : undefined
  const mediaChanged = editedMediaChanged.value
  editedFlashcardRemoved.value = false
  editedMediaChanged.value = false
  if (!flashcard) {
    zoomOpen.value = false
    return
  }

  zoomedFlashcard.value = flashcard
  if (mediaChanged) {
    loadZoomedMedia(flashcard.id)
  }
})

function clearFilter() {
  filter.value = { unknown: false, attempted: false, stages: [] }
}

// Cards are sized from the tab width, not the viewer's, so opening or closing the sidebar
// never resizes them; only the number of cards per row changes. The card size aims at a
// tile width and fits as many columns as the tab allows, so wider screens get more columns;
// pinching (or ctrl + wheel, which is what a trackpad pinch sends) changes the tile width.

const DEFAULT_TILE_WIDTH = 180
// stops zooming out sooner on narrow (mobile) screens than the column limit alone would
const MIN_TILE_WIDTH = 90
const MAX_COLUMNS = 12
const GRID_GAP = 6
// the sidebar animates the viewer's width; rewrapping the rows on every frame of it
// makes them jump, so the new width is taken once it settles
const RESIZE_SETTLE_MS = 150

const scrollArea = ref<HTMLElement>()
const tabWidth = ref(document.documentElement.clientWidth)
const scrollPaddingX = ref(0)
const containerWidth = ref(0)
const tileWidth = ref(DEFAULT_TILE_WIDTH)
let resizeTimeout: ReturnType<typeof setTimeout> | undefined

function maxColumns(width: number): number {
  return Math.max(1, Math.min(MAX_COLUMNS, Math.floor(width / MIN_TILE_WIDTH)))
}

// the grid's width if the viewer took the whole tab
const tabGridWidth = computed(() => Math.max(0, tabWidth.value - scrollPaddingX.value))

const tabColumns = computed(() => {
  const width = tabGridWidth.value
  if (width <= 0) return 1
  return Math.min(maxColumns(width), Math.max(1, Math.round(width / tileWidth.value)))
})

const cardWidth = computed(
  () => (tabGridWidth.value - GRID_GAP * (tabColumns.value - 1)) / tabColumns.value,
)

// how many cards of that width fit into the viewer (epsilon guards float rounding)
const columns = computed(() => {
  const width = containerWidth.value
  if (width <= 0) return tabColumns.value
  const fitting = Math.floor((width + GRID_GAP) / (cardWidth.value + GRID_GAP) + 0.001)
  return Math.max(1, Math.min(tabColumns.value, fitting))
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
  const grid = tabGridWidth.value
  if (grid <= 0) return
  tileWidth.value = Math.min(grid, Math.max(grid / maxColumns(grid), width))
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

const resizeObserver = new ResizeObserver((entries) => {
  const width = entries[0]?.contentRect.width ?? 0
  clearTimeout(resizeTimeout)
  if (containerWidth.value === 0) {
    containerWidth.value = width
  } else {
    resizeTimeout = setTimeout(() => (containerWidth.value = width), RESIZE_SETTLE_MS)
  }
})

function onWindowResize() {
  tabWidth.value = document.documentElement.clientWidth
}

onMounted(() => {
  const el = scrollArea.value
  if (!el) return
  const style = getComputedStyle(el)
  scrollPaddingX.value = parseFloat(style.paddingLeft) + parseFloat(style.paddingRight)
  window.addEventListener('resize', onWindowResize)
  resizeObserver.observe(el)
  el.addEventListener('touchstart', onTouchStart, { passive: true })
  el.addEventListener('touchmove', onTouchMove, { passive: false })
  el.addEventListener('touchend', onTouchEnd, { passive: true })
  el.addEventListener('touchcancel', onTouchEnd, { passive: true })
  el.addEventListener('wheel', onWheel, { passive: false })
})

onUnmounted(() => {
  mediaController?.abort()
  clearTimeout(resizeTimeout)
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
.viewer {
  flex: 1;
  min-width: 0;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.viewer-toolbar {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 10px 12px 6px 12px;
}

.viewer-toolbar-info {
  color: #6b6f7b;
  font-size: 0.9rem;
}

.viewer-select-button {
  padding: 4px 14px;
  border: none;
  border-radius: 999px;
  background: rgba(87, 87, 87, 0.12);
  color: #2f3340;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s ease-in-out;
}

@media (hover: hover) {
  .viewer-select-button:hover:not(:disabled) {
    background: rgba(87, 87, 87, 0.2);
  }
}

.viewer-select-button--fixed {
  width: 5.5em;
  text-align: center;
}

.viewer-select-button:disabled {
  opacity: 0.5;
  cursor: default;
}

.viewer-select-button--active {
  background: #007bff;
  color: white;
}

@media (hover: hover) {
  .viewer-select-button--active:hover:not(:disabled) {
    background: #0068d9;
  }
}

.viewer-scroll {
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

.viewer-spinner {
  margin: auto;
}

.viewer-empty {
  margin: auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  color: #6b6f7b;
  text-align: center;
}

.viewer-category {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.viewer-category-header {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.viewer-category-name {
  color: #2f3340;
  font-size: 1.05rem;
  font-weight: 600;
}

.viewer-category-line {
  height: 1px;
  background: rgba(47, 51, 64, 0.2);
  margin-bottom: 4px;
}

.viewer-grid {
  display: grid;
}

.viewer-tile {
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
  .viewer-tile:hover {
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
  }
}

.viewer-tile--selecting {
  cursor: pointer;
}

.viewer-tile--selected {
  background: #f5f5f8;
}

.viewer-tile--selected .viewer-tile-text {
  opacity: 0.7;
}

.viewer-tile-check {
  position: absolute;
  right: 6px;
  bottom: 6px;
  font-size: clamp(14px, 12cqi, 24px);
  color: #007bff;
  background: white;
  border-radius: 50%;
}

.viewer-tile-text {
  max-height: 100%;
  overflow: hidden;
  color: #2f3340;
  font-size: clamp(0.5rem, 11cqi, 1.3rem);
  line-height: 1.25;
  text-align: center;
  overflow-wrap: anywhere;
}

.viewer-zoomed-card {
  width: clamp(200px, 90vw, 600px);
  height: clamp(290px, 50vh, 450px);
}
</style>
