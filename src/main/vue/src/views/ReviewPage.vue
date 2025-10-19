<template>
  <div
    :class="[
      'page',
      'page--x-centered',
      'page--y-centered',
      'page--min-padded',
    ]"
  >
    <div class="review-info">
      <span class="corner-text">
        {{ reviewTopic }}
      </span>
      <span class="review-progressbar">
        <Progressbar
          :progress="progress"
          height="0.5rem"
          track-rounded
          bar-rounded
        />
      </span>
      <AwesomeButton
        ref="escapeButton"
        icon="fa-solid fa-xmark"
        :on-click="finishReviewAndGoToFlashcards"
      />
    </div>
    <div class="review-body">
      <SpaceDeck
        ref="spaceDeck"
        v-model:flashcard="currFlashcard"
        :on-flashcard-removed="onFlashcardRemoved"
      />
      <div class="review-nav">
        <SmartButton
          v-if="reviewMode === ReviewMode.LIGHTSPEED"
          ref="stageDownButton"
          text="Don't know"
          class="review-button remove-button"
          :disabled="noNextAvailable"
          :hidden="noNextAvailable"
          :on-click="stageDown"
          auto-blur
          rounded
        />
        <SmartButton
          v-if="reviewMode === ReviewMode.LIGHTSPEED"
          ref="stageUpButton"
          text="Know"
          class="review-button create-button"
          :disabled="editFormWasOpened || noNextAvailable"
          :hidden="noNextAvailable"
          :on-click="stageUp"
          auto-blur
          rounded
        />
        <SmartButton
          v-if="reviewMode === ReviewMode.SPECIAL"
          ref="prevButton"
          class="review-button update-button"
          text="Prev"
          :disabled="noPrevAvailable"
          :on-click="prev"
          auto-blur
          rounded
        />
        <SmartButton
          v-if="reviewMode === ReviewMode.SPECIAL"
          ref="nextButton"
          class="review-button update-button"
          text="Next"
          :disabled="noNextAvailable"
          :on-click="next"
          auto-blur
          rounded
        />
        <SmartButton
          v-if="reviewMode === ReviewMode.SPACE"
          ref="prevButton"
          class="review-button update-button"
          text="Prev"
          :disabled="noPrevAvailable"
          :on-click="prev"
          auto-blur
          rounded
        />
        <SmartButton
          v-if="reviewMode === ReviewMode.SPACE"
          class="review-button move-back-button"
          text="Move back"
          :disabled="noNextAvailable"
          :hidden="noNextAvailable"
          :on-click="moveBack"
          :hold-time="1.2"
          auto-blur
          rounded
        />
        <SmartButton
          v-if="reviewMode === ReviewMode.SPACE"
          ref="nextButton"
          class="review-button update-button"
          text="Next"
          :disabled="noNextAvailable"
          :on-click="next"
          auto-blur
          rounded
        />
      </div>
    </div>
  </div>
  <SpaceToast/>
</template>

<script setup lang="ts">
import Progressbar from '@/components/Progressbar.vue'
import SpaceDeck from '@/components/SpaceDeck.vue'
import SmartButton from '@/components/SmartButton.vue'
import AwesomeButton from '@/components/AwesomeButton.vue'
import SpaceToast from '@/components/SpaceToast.vue'
import { useFlashcardStore } from '@/stores/flashcard-store.ts'
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { copyFlashcard, updateFlashcard } from '@/core-logic/flashcard-logic.ts'
import { nextStage, prevStage, Stage, stages } from '@/core-logic/stage-logic.ts'
import { useChronoStore } from '@/stores/chrono-store.ts'
import {
  createReviewQueue,
  createReviewQueueForStage, EmptyReviewQueue,
  ReviewMode,
  ReviewQueue,
  toReviewMode
} from '@/core-logic/review-logic.ts'
import { routeNames } from '@/router'
import { onBeforeRouteLeave, useRouter } from 'vue-router'
import { loadSelectedSetId } from '@/shared/cookies.ts'
import { useToggleStore } from '@/stores/toggle-store.ts'
import { Flashcard, FlashcardSet } from '@/model/flashcard.ts'
import {
  loadFlashcardAndChronoStoresById
} from '@/shared/stores.ts'
import {
  sendChronoBulkUpdateRequest,
  sendFlashcardUpdateRequest
} from '@/api/api-client.ts'
import { useSpaceToaster } from '@/stores/toast-store.ts'
import {
  chronodayStatuses,
  isCompleteAvailable,
  isInProgressAvailable,
  selectConsecutiveDaysBefore
} from '@/core-logic/chrono-logic.ts'
import type { Chronoday } from '@/model/chrono.ts'

const props = defineProps<{
  stage?: Stage,
}>()

const router = useRouter()
const toaster = useSpaceToaster()
const toggleStore = useToggleStore()
const chronoStore = useChronoStore()
const flashcardStore = useFlashcardStore()

const {
  flashcardSet,
  flashcards,
} = storeToRefs(flashcardStore)
const {
  chronodays,
  currDay
} = storeToRefs(chronoStore)
const { flashcardEditOpen } = storeToRefs(toggleStore)

const spaceDeck = ref<InstanceType<typeof SpaceDeck>>()
const escapeButton = ref<InstanceType<typeof AwesomeButton>>()
const stageDownButton = ref<InstanceType<typeof SmartButton>>()
const stageUpButton = ref<InstanceType<typeof SmartButton>>()
const prevButton = ref<InstanceType<typeof SmartButton>>()
const nextButton = ref<InstanceType<typeof SmartButton>>()

const reviewTopic = computed(() => props.stage?.displayName ?? 'Lightspeed')
const reviewMode = computed(() => toReviewMode(props.stage))
const isLightspeedMode = computed(() => reviewMode.value === ReviewMode.LIGHTSPEED)
const reviewQueue = ref<ReviewQueue>(new EmptyReviewQueue())
const flashcardsTotal = ref(0)
const flashcardsRemaining = computed(() => reviewQueue.value.remaining())
const editFormWasOpened = ref(false)
const currFlashcard = ref<Flashcard>()
const progress = computed(() => {
  const total = flashcardsTotal.value
  let remaining = flashcardsRemaining.value + 1
  if (noNextAvailable.value) remaining = 0
  const completionRate = (total - remaining) / total
  return Math.max(0, Math.min(1, completionRate))
})
const noPrevAvailable = computed(() => {
  if (flashcardsTotal.value === 1) {
    return !noNextAvailable.value
  }
  return flashcardsTotal.value === flashcardsRemaining.value + 1
})
const noNextAvailable = computed(() => currFlashcard.value === undefined)

function prevFlashcard(): boolean {
  currFlashcard.value = reviewQueue.value.prev()
  return currFlashcard.value !== undefined
}

function nextFlashcard(): boolean {
  currFlashcard.value = reviewQueue.value.next()
  return currFlashcard.value !== undefined
}

function startReview() {
  console.log(`Starting review on stage: ${props.stage?.displayName ?? 'default'}`)
  if (props.stage) {
    reviewQueue.value = createReviewQueueForStage(flashcards.value, props.stage)
  } else {
    reviewQueue.value = createReviewQueue(flashcards.value)
  }
  flashcardsTotal.value = reviewQueue.value.remaining()
  nextFlashcard()
  console.log(`Flashcards TOTAL: ${flashcardsTotal.value}`)
}

async function finishReview() {
  console.log(`Finishing review on stage: ${props.stage?.displayName ?? 'default'}`)
  reviewQueue.value = new EmptyReviewQueue()
  flashcardsTotal.value = 0
  if (flashcardSet.value) {
    if (noNextAvailable.value && isLightspeedMode.value) {
      await markDaysAsCompleted(flashcardSet.value)
    }
  } else {
    console.error(`Can't gracefully finish review: flashcard set is undefined`)
  }
}

async function finishReviewAndGoToFlashcards() {
  await finishReview()
    .then(() =>
      router.push({ name: routeNames.flashcards })
    )
}

async function stageDown() {
  if (flashcardSet.value && currFlashcard.value) {
    editFormWasOpened.value = false
    spaceDeck.value?.willSlideToLeft()
    const flashcard = copyFlashcard(currFlashcard.value)
    updateFlashcard(flashcard, prevStage(flashcard.stage))
    const success = await sendUpdatedFlashcard(flashcardSet.value, flashcard)
    if (success) {
      await markDaysAndGoNext(flashcardSet.value)
    }
  } else {
    console.error(`stageDown's impossible:`,
      `flashcard set ${flashcardSet.value?.id ?? 'undefined'}`,
      `flashcard ${currFlashcard.value?.id ?? 'undefined'}`
    )
  }
}

async function stageUp() {
  if (flashcardSet.value && currFlashcard.value) {
    editFormWasOpened.value = false
    spaceDeck.value?.willSlideToRight()
    const flashcard = copyFlashcard(currFlashcard.value)
    updateFlashcard(flashcard, nextStage(flashcard.stage))
    const success = await sendUpdatedFlashcard(flashcardSet.value, flashcard)
    if (success) {
      await markDaysAndGoNext(flashcardSet.value)
    }
  } else {
    console.error(`stageUp's impossible:`,
      `flashcard set ${flashcardSet.value?.id ?? 'undefined'}`,
      `flashcard ${currFlashcard.value?.id ?? 'undefined'}`
    )
  }
}

async function prev() {
  spaceDeck.value?.willSlideToLeft()
  prevFlashcard()
}

async function next() {
  spaceDeck.value?.willSlideToRight()
  nextFlashcard()
}

async function moveBack() {
  if (flashcardSet.value && currFlashcard.value) {
    editFormWasOpened.value = false
    spaceDeck.value?.willSlideToLeft()
    const flashcard = copyFlashcard(currFlashcard.value)
    updateFlashcard(flashcard, stages.S1)
    const success = await sendUpdatedFlashcard(flashcardSet.value, flashcard)
    if (success) {
      nextFlashcard()
    }
  } else {
    console.error(`moveBack's impossible:`,
      `flashcard set ${flashcardSet.value?.id ?? 'undefined'}`,
      `flashcard ${currFlashcard.value?.id ?? 'undefined'}`
    )
  }
}

async function sendUpdatedFlashcard(flashcardSet: FlashcardSet, flashcard: Flashcard): Promise<boolean> {
  return await sendFlashcardUpdateRequest(flashcardSet.id, flashcard)
    .then((response) => {
      flashcardStore.changeFlashcard(response.data)
      currFlashcard.value = response.data
      return true
    })
    .catch((error) => {
      console.error(`Failed to update flashcard ${flashcard.id}`, error.response?.data)
      toaster.bakeError(`Couldn't move a flashcard`, error.response?.data)
      return false
    })
}

async function markDaysAndGoNext(flashcardSet: FlashcardSet) {
  if (nextFlashcard() && isLightspeedMode.value) {
    await markDaysAsInProgress(flashcardSet)
  } else if (isLightspeedMode.value) {
    await markDaysAsCompleted(flashcardSet)
  }
}

async function markDaysAsInProgress(flashcardSet: FlashcardSet) {
  if (isInProgressAvailable(currDay.value)) {
    await markDaysAs(flashcardSet, chronodayStatuses.IN_PROGRESS, isInProgressAvailable)
  }
}

async function markDaysAsCompleted(flashcardSet: FlashcardSet) {
  if (isCompleteAvailable(currDay.value)) {
    await markDaysAs(flashcardSet, chronodayStatuses.COMPLETED, isCompleteAvailable)
  }
}

async function markDaysAs(
  flashcardSet: FlashcardSet,
  status: string,
  daysFilter: (chronoday: Chronoday) => boolean,
) {
  const days = selectConsecutiveDaysBefore(chronodays.value, currDay.value, daysFilter)
  if (days.length === 0) {
    console.error(
      `No days to mark as ${status}`,
      `flashcard set ${flashcardSet.id}`,
      `current day: ${JSON.stringify(currDay.value)}`
    )
    return
  }

  await sendChronoBulkUpdateRequest(flashcardSet.id, status, days)
    .then((response) => {
      chronoStore.updateDays(response.data)
    })
    .catch((error) => {
      console.error(`Failed to mark days as ${status} for ${flashcardSet.id}`, error.response?.data)
      toaster.bakeError(`Couldn't move a flashcard`, error.response?.data)
    })
}

function onFlashcardRemoved() {
  editFormWasOpened.value = false
  nextFlashcard()
}

watch(flashcardEditOpen, (newVal) => {
  if (newVal) {
    editFormWasOpened.value = newVal
  }
})

onMounted(async () => {
  if (!flashcardStore.loaded) {
    console.log('Flashcard set not loaded, loading...')
    const selectedSetId = loadSelectedSetId()
    if (selectedSetId) {
      await loadFlashcardAndChronoStoresById(selectedSetId)
    } else {
      console.log('Flashcard set not found in cookies')
    }
  }
  startReview()
  spaceDeck.value?.setDeckReady()
  document.addEventListener('keydown', handleKeydown)
})

onUnmounted(async () => {
  await finishReview()
  document.removeEventListener('keydown', handleKeydown)
})

onBeforeRouteLeave(async () => {
  await finishReview()
})

function handleKeydown(event: KeyboardEvent) {
  if (toggleStore.isAnyModalOpen()) return

  if (event.key === 'Escape') {
    event.stopPropagation()
    escapeButton.value?.press()
  } else if (event.key === 'ArrowLeft') {
    event.stopPropagation()
    stageDownButton.value?.click()
    prevButton.value?.click()
  } else if (event.key === 'ArrowRight') {
    event.stopPropagation()
    stageUpButton.value?.click()
    nextButton.value?.click()
  }
}

</script>

<style scoped>
.review-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 4px;
  width: 100%;
  gap: 10px;
}

.corner-text {
  background: none;
  color: #9f9f9f;
  font-size: 1rem;
}

.review-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: fit-content;
  gap: 20px;
}

.review-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  height: fit-content;
  gap: 10px;
}

.review-progressbar {
  flex: 1;
  --progressbar--from: var(--review-progressbar--from);
  --progressbar--via: var(--review-progressbar--via);
  --progressbar--to: var(--review-progressbar--to);
  --progressbar--bg-color: var(--review-progressbar--bg-color);
}

</style>
