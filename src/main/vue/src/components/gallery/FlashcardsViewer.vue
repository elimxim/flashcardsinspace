<template>
  <div class="viewer">
    <div class="viewer-toolbar">
      <span class="viewer-toolbar-info">{{ infoText }}</span>
      <SmartButton
        class="viewer-button"
        :text="selecting ? 'Cancel' : 'Select'"
        :clicked="selecting"
        :disabled="!loaded || (!selecting && visibleCount === 0)"
        :on-click="toggleSelecting"
        rounded
        auto-blur
      />
    </div>
    <FlashcardGrid
      :sections="loaded ? sections : []"
      :selected-ids="selectedIds"
      :selecting="selecting"
      :on-tile-click="onTileClick"
    >
      <template #empty>
        <KineticRingSpinner v-if="resolvedLoading" class="viewer-spinner" />
        <template v-else-if="!loaded" />
        <div v-else-if="!flashcardSet" class="viewer-empty">No flashcard set selected</div>
        <div v-else-if="flashcards.length === 0" class="viewer-empty">
          This flashcard set has no flashcards yet
        </div>
        <div v-else class="viewer-empty">
          <span>No flashcards match the filters</span>
          <SmartButton
            class="viewer-button"
            text="Clear filters"
            :on-click="clearFilter"
            rounded
            auto-blur
          />
        </div>
      </template>
    </FlashcardGrid>
    <ZoomOverlay v-model:open="zoomOpen" :z-index="850" :close-on-escape="!flashcardEditOpen">
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
import SmartButton from '@/components/common/SmartButton.vue'
import FlashcardGrid, { type FlashcardGridSection } from '@/components/gallery/FlashcardGrid.vue'
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
import { computed, onUnmounted, ref, watch } from 'vue'
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

function isShown(stage: Stage): boolean {
  const { unknown, attempted, stages } = filter.value
  if (!unknown && !attempted && stages.length === 0) return true
  if (stage === specialStages.UNKNOWN) return unknown
  if (stage === specialStages.ATTEMPTED) return attempted
  return stages.includes(stage.name)
}

// non-empty stages in stage order, flashcards in creation (id) order
const sections = computed<FlashcardGridSection[]>(() => {
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
    .map((stage) => ({
      key: stage.name,
      title: stage.displayName,
      flashcards: groups.get(stage)!.sort((a, b) => a.id - b.id),
    }))
})
const visibleCount = computed(() =>
  sections.value.reduce((sum, section) => sum + section.flashcards.length, 0),
)

const selecting = ref(false)

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
  selectedIds.value = selectedIds.value.includes(id)
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

onUnmounted(() => {
  mediaController?.abort()
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

.viewer-button {
  --smart-button--width: 120px;
  --smart-button--height: 30px;
  --smart-button--title--font-size: 0.8rem;
  --smart-button--bg: #494a6c;
  --smart-button--bg--hover: #32334a;
  --smart-button--bg--clicked: var(--calm-button--bg-color);
  --smart-button--bg--clicked--hover: var(--calm-button--bg-color--hover);
  --smart-button--bg--disabled: rgba(50, 51, 74, 0.15);
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

.viewer-zoomed-card {
  width: clamp(200px, 90vw, 600px);
  height: clamp(290px, 50vh, 450px);
}
</style>
