<template>
  <div
    :class="[
      'page',
      'page--bg--light',
      'flex-column',
      'flex-center',
      'scroll-none',
      'touch-none',
    ]"
  >
    <ControlBar
      style="z-index: 10;"
      :title="flashcardSetName"
      :center-title-padding="100"
      center-title
    >
      <template #left>
        <div class="review-mode">
          {{ ReviewSessionType.LIGHTSPEED }}
        </div>
      </template>
      <template #right>
        <AwesomeButton
          icon="fa-solid fa-circle-xmark"
          class="control-bar-button"
          tooltip="Finish review and leave"
          tooltip-position="bottom-left"
          :on-click="finishReviewAndLeave"
        />
      </template>
    </ControlBar>
    <div class="review-layout">
      <KineticRingSpinner v-if="resolvedLoading" :ring-size="240"/>
      <template v-else-if="!loadingStarted">
        <div class="review-progressbar">
          <Progressbar
            :progress="progress"
            height="16px"
            glide
          />
        </div>
        <div class="review-info">
          <div class="cp-count-box cp-count-box--big">
            {{ flashcardsSeen }}
          </div>
          <div class="cp-count-box cp-count-box--big">
            {{ flashcardsRemaining }}
          </div>
        </div>
        <div class="review-body">
          <SpaceDeck
            ref="spaceDeck"
            :session-type="ReviewSessionType.LIGHTSPEED"
            :on-flashcard-removed="onFlashcardRemoved"
            :on-audio-changed="onAudioChanged"
            :can-slide-left="!noNextAvailable"
            :can-slide-right="!noNextAvailable"
            :on-slide-left="stageDown"
            :on-slide-right="stageUp"
            swipe-left-text="Don't know"
            swipe-right-text="Know"
          >
            <ReviewResult/>
          </SpaceDeck>
          <div v-if="!isTouchDevice" class="review-nav">
            <SmartButton
              text="Don't know"
              class="decision-button dangerous-button"
              :disabled="noNextAvailable"
              :hidden="noNextAvailable"
              :on-click="spaceDeck?.slideLeft"
              auto-blur
              rounded
            />
            <SmartButton
              text="Know"
              class="decision-button safe-button"
              :disabled="noNextAvailable"
              :hidden="noNextAvailable"
              :on-click="spaceDeck?.slideRight"
              auto-blur
              rounded
            />
          </div>
        </div>
      </template>
    </div>
  </div>
  <SpaceToast/>
</template>

<script setup lang="ts">
import ControlBar from '@/components/ControlBar.vue'
import KineticRingSpinner from '@/components/KineticRingSpinner.vue'
import Progressbar from '@/components/Progressbar.vue'
import SpaceDeck from '@/components/review/SpaceDeck.vue'
import SmartButton from '@/components/SmartButton.vue'
import AwesomeButton from '@/components/AwesomeButton.vue'
import SpaceToast from '@/components/SpaceToast.vue'
import ReviewResult from '@/components/review/ReviewResult.vue'
import { useFlashcardStore } from '@/stores/flashcard-store.ts'
import { useStopWatch } from '@/utils/stop-watch.ts'
import { isTouchDevice } from '@/utils/utils.ts'
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { copyFlashcard, updateFlashcard } from '@/core-logic/flashcard-logic.ts'
import { nextStage, prevStage, Stage, } from '@/core-logic/stage-logic.ts'
import { useChronoStore } from '@/stores/chrono-store.ts'
import { createReviewQueue, ReviewSessionType } from '@/core-logic/review-logic.ts'
import { routeNames } from '@/router'
import { useRouter } from 'vue-router'
import { loadSelectedSetIdFromCookies, } from '@/utils/cookies.ts'
import { useToggleStore } from '@/stores/toggle-store.ts'
import { Flashcard, FlashcardSet } from '@/model/flashcard.ts'
import { loadStoresForFlashcardSetId } from '@/utils/store-loading.ts'
import {
  sendChronoBulkUpdateRequest,
  sendFlashcardUpdateRequest,
  sendReviewSessionCreateRequest,
  sendReviewSessionUpdateRequest,
} from '@/api/api-client.ts'
import { useSpaceToaster } from '@/stores/toast-store.ts'
import {
  chronodayStatuses,
  chronodayStatusesToCompleteDay,
  chronodayStatusesToProgressDay,
  selectConsecutiveDaysBefore
} from '@/core-logic/chrono-logic.ts'
import { ReviewSessionCreateRequest } from '@/api/communication.ts'
import { Log, LogTag } from '@/utils/logger.ts'
import { userApiErrors } from '@/api/user-api-error.ts'
import { destroyReviewStore, useReviewStore } from '@/stores/review-store.ts'
import { useDeferredLoading } from '@/utils/deferred-loading.ts'

const props = defineProps<{
  sessionId?: number,
  stages: Stage[],
}>()

const router = useRouter()
const toaster = useSpaceToaster()
const toggleStore = useToggleStore()
const chronoStore = useChronoStore()
const flashcardStore = useFlashcardStore()

const { flashcardSet, flashcards } = storeToRefs(flashcardStore)
const { chronodays, currDay } = storeToRefs(chronoStore)

const {
  loadingStarted,
  resolvedLoading,
  startLoading,
  stopLoading,
} = useDeferredLoading()

const reviewStore = useReviewStore(ReviewSessionType.LIGHTSPEED)

const {
  reviewStoreLoaded,
  flashcardsTotal,
  currFlashcard,
  flashcardsRemaining,
  flashcardsSeen,
  progress,
  noNextAvailable,
} = storeToRefs(reviewStore)

const flashcardSetName = computed(() => flashcardSet.value?.name || '')

const elapsedTime = ref(0)
const { startWatch, stopWatch } = useStopWatch(elapsedTime)

const startingReview = ref(false)

const reviewedFlashcardIds = ref<number[]>([])
const incorrectFlashcards = ref<Flashcard[]>([])

const spaceDeck = ref<InstanceType<typeof SpaceDeck>>()

async function startReview() {
  startingReview.value = true
  try {
    Log.log(LogTag.LOGIC, `Starting review: ${ReviewSessionType.LIGHTSPEED}`)
    reviewStore.loadState(createReviewQueue(flashcards.value, currDay.value, chronodays.value))
    await createReviewSession()
    await reviewStore.nextFlashcard(flashcardSet.value, (success) => {
      if (success) startWatch()
    })
    Log.log(LogTag.LOGIC, `Flashcards TOTAL: ${flashcardsTotal.value}`)
  } finally {
    startingReview.value = false
  }
}

function resetState() {
  stopWatch()
  reviewStore.$reset()
  reviewedFlashcardIds.value = []
  incorrectFlashcards.value = []
}

async function finishReview() {
  if (startingReview.value || !reviewStoreLoaded.value) return
  Log.log(LogTag.LOGIC, `Finishing review: ${ReviewSessionType.LIGHTSPEED}`)
  currFlashcardWatcher.stop()
  try {
    if (flashcardSet.value && noNextAvailable.value) {
      await markDaysAsCompleted(flashcardSet.value)
    }
  } finally {
    resetState()
  }
}

async function finishReviewAndLeave() {
  await finishReview().then(() =>
    router.push({ name: routeNames.controlPanel })
  )
}

async function stageDown() {
  if (!flashcardSet.value || !currFlashcard.value) return
  const flashcard = copyFlashcard(currFlashcard.value)
  updateFlashcard(flashcard, prevStage(flashcard.stage), currDay.value.chronodate)
  const success = await sendUpdatedFlashcard(flashcardSet.value, flashcard)
  incorrectFlashcards.value.push(currFlashcard.value)
  if (success) {
    await getNextAndMarkDays(flashcardSet.value)
  }
}

async function stageUp() {
  if (!flashcardSet.value || !currFlashcard.value) return
  const flashcard = copyFlashcard(currFlashcard.value)
  updateFlashcard(flashcard, nextStage(flashcard.stage), currDay.value.chronodate)
  const success = await sendUpdatedFlashcard(flashcardSet.value, flashcard)
  if (success) {
    await getNextAndMarkDays(flashcardSet.value)
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
      Log.error(LogTag.LOGIC, `Failed to update Flashcard.id=${flashcard.id}`, error.response?.data)
      toaster.bakeError(userApiErrors.FLASHCARD__PROGRESSION_FAILED, error.response?.data)
      return false
    })
}

async function getNextAndMarkDays(flashcardSet: FlashcardSet) {
  if (await reviewStore.nextFlashcard(flashcardSet)) {
    await markDaysAsInProgress(flashcardSet)
  } else {
    await markDaysAsCompleted(flashcardSet)
  }
}

async function markDaysAs(
  flashcardSetId: number,
  status: string,
  acceptedStatuses: Set<string>,
) {
  if (!acceptedStatuses.has(currDay.value.status)) {
    return
  }

  const days = selectConsecutiveDaysBefore(chronodays.value, currDay.value, acceptedStatuses)
  if (days.length === 0) {
    Log.error(LogTag.LOGIC,
      `No days to mark as ${status}`,
      `FlashcardSet.id=${flashcardSetId}`,
      `currDay=${JSON.stringify(currDay.value)}`
    )
    return
  }

  await sendChronoBulkUpdateRequest(flashcardSetId, status, days)
    .then((response) => {
      chronoStore.updateDays(response.data.chronodays)
      chronoStore.updateDayStreak(response.data.dayStreak)
    })
    .catch((error) => {
      Log.error(LogTag.LOGIC, `Failed to mark days as ${status} for FlashcardSet.id=${flashcardSetId}`, error.response?.data)
      toaster.bakeError(userApiErrors.SCHEDULE__UPDATING_FAILED, error.response?.data)
    })
}

async function markDaysAsInProgress(flashcardSet: FlashcardSet) {
  await markDaysAs(flashcardSet.id, chronodayStatuses.IN_PROGRESS, chronodayStatusesToProgressDay)
}

async function markDaysAsCompleted(flashcardSet: FlashcardSet) {
  await markDaysAs(flashcardSet.id, chronodayStatuses.COMPLETED, chronodayStatusesToCompleteDay)
}

function onFlashcardRemoved() {
  reviewStore.nextFlashcard(flashcardSet.value)
}

function onAudioChanged() {
  reviewStore.fetchAudio(flashcardSet.value)
}

const currFlashcardWatcher = watch(currFlashcard, async (newVal, oldVal) => {
  if (oldVal) {
    Log.log(LogTag.DEBUG, `oldVal`)
    reviewedFlashcardIds.value.push(oldVal.id)
    await updateReviewSession([oldVal.id])
  }
  if (!newVal) {
    Log.log(LogTag.DEBUG, `!newVal`)
    stopWatch()
    await updateReviewSession(reviewedFlashcardIds.value, true)
  }
})

async function createReviewSession() {
  if (!flashcardSet.value) return

  const request: ReviewSessionCreateRequest = {
    type: ReviewSessionType.LIGHTSPEED,
    chronodayId: currDay.value.id,
  }

  await sendReviewSessionCreateRequest(flashcardSet.value.id, request)
    .then((response) => {
      Log.log(LogTag.LOGIC, `Review session ${response.data.id} created`)
      router.replace({
        query: {
          ...router.currentRoute.value.query,
          sessionId: response.data.id,
        }
      })
    })
    .catch((error) => {
      Log.error(LogTag.LOGIC, `Failed to create a review session`, error.response?.data)
      toaster.bakeError(userApiErrors.REVIEW_SESSION__CREATION_FAILED, error.response?.data)
    })
}

async function updateReviewSession(flashcardIds: number[], finished: boolean = false) {
  if (!flashcardSet.value || !props.sessionId) return
  await sendReviewSessionUpdateRequest(flashcardSet.value.id, props.sessionId, {
    elapsedTime: elapsedTime.value,
    flashcardIds: flashcardIds.map(id => ({ id: id })),
    finished: finished,
  })
    .then(() => {
      Log.log(LogTag.LOGIC, `Review session ${props.sessionId} updated`)
    })
    .catch((error) => {
      Log.error(LogTag.LOGIC, `Failed to updated review session ${props.sessionId}`, error.response?.data)
      toaster.bakeError(userApiErrors.REVIEW_SESSION__UPDATING_FAILED, error.response?.data)
    })
}

onMounted(async () => {
  try {
    startLoading()
    reviewStore.$reset()
    if (!flashcardStore.loaded) {
      Log.log(LogTag.LOGIC, 'Flashcard set is not loaded, loading...')
      const selectedSetId = loadSelectedSetIdFromCookies()
      if (selectedSetId) {
        await loadStoresForFlashcardSetId(selectedSetId)
      } else {
        Log.log(LogTag.LOGIC, 'Flashcard set not found in cookies')
      }
    }
    await startReview()
  } finally {
    await stopLoading()
  }
  spaceDeck.value?.setDeckReady()
  document.addEventListener('keydown', handleKeydown)
})

onUnmounted(async () => {
  await finishReview()
  destroyReviewStore(ReviewSessionType.LIGHTSPEED)
  document.removeEventListener('keydown', handleKeydown)
})

async function handleKeydown(event: KeyboardEvent) {
  if (toggleStore.isAnyModalOpen()) return

  if (event.key === 'Escape') {
    event.stopPropagation()
    await finishReviewAndLeave()
  } else if (event.key === 'ArrowLeft') {
    event.stopPropagation()
    await spaceDeck?.value?.slideLeft()
  } else if (event.key === 'ArrowRight') {
    event.stopPropagation()
    await spaceDeck?.value?.slideRight()
  }
}

</script>

<style scoped>
.review-layout {
  flex: 1;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.review-mode {
  color: rgba(243, 239, 239, 0.7);
  font-size: clamp(0.9rem, 1.8vw, 1.1rem);
  letter-spacing: 0.05rem;
  word-spacing: 0.05rem;
  text-transform: uppercase;
  white-space: nowrap;
}

.review-info {
  display: flex;
  align-items: start;
  justify-content: space-between;
  width: 100%;
  padding: 2px;
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

.decision-button {
  --smart-button--width: 130px;
}

</style>
