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
          <font-awesome-icon :icon="reviewIcons.get(ReviewSessionType.LIGHTSPEED)!!"/>
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
            :can-slide-left="!noOneAvailable"
            :can-slide-right="!noOneAvailable"
            :on-slide-left="stageDown"
            :on-slide-right="stageUp"
            swipe-left-text="Don't know"
            swipe-right-text="Know"
          >
            <ReviewResult/>
          </SpaceDeck>
          <div v-if="UXConfig().showNavButtons" class="review-nav">
            <SmartButton
              text="Don't know"
              class="decision-button dangerous-button"
              :disabled="noOneAvailable"
              :hidden="noOneAvailable"
              :on-click="spaceDeck?.slideLeft"
              auto-blur
              rounded
            />
            <SmartButton
              text="Know"
              class="decision-button safe-button"
              :disabled="noOneAvailable"
              :hidden="noOneAvailable"
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
import KineticRingSpinner from '@/components/spinners/KineticRingSpinner.vue'
import Progressbar from '@/components/common/Progressbar.vue'
import SpaceDeck from '@/components/SpaceDeck.vue'
import SmartButton from '@/components/common/SmartButton.vue'
import AwesomeButton from '@/components/common/AwesomeButton.vue'
import SpaceToast from '@/components/common/SpaceToast.vue'
import ReviewResult from '@/components/ReviewResult.vue'
import { useFlashcardStore } from '@/stores/flashcard-store.ts'
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { storeToRefs } from 'pinia'
import { copyFlashcard, updateFlashcard } from '@/core-logic/flashcard-logic.ts'
import { nextStage, prevStage, Stage } from '@/core-logic/stage-logic.ts'
import { useChronoStore } from '@/stores/chrono-store.ts'
import {
  createReviewQueue,
  reviewIcons,
  ReviewSessionType,
} from '@/core-logic/review-logic.ts'
import { routeNames } from '@/router'
import { onBeforeRouteLeave, useRouter } from 'vue-router'
import { loadSelectedSetIdFromCookies } from '@/utils/cookies.ts'
import { useToggleStore } from '@/stores/toggle-store.ts'
import { Flashcard, FlashcardSet } from '@/model/flashcard.ts'
import { loadStoresForFlashcardSetId } from '@/utils/store-loading.ts'
import {
  sendChronoBulkUpdateRequest,
  sendFlashcardUpdateRequest,
} from '@/api/api-client.ts'
import { useSpaceToaster } from '@/stores/toast-store.ts'
import {
  chronodayStatuses,
  chronodayStatusesToCompleteDay,
  chronodayStatusesToProgressDay,
} from '@/core-logic/chrono-logic.ts'
import { Log, LogTag } from '@/utils/logger.ts'
import { userApiErrors } from '@/api/user-api-error.ts'
import { destroyReviewStore, useReviewStore } from '@/stores/review-store.ts'
import { useDeferredLoading } from '@/utils/deferred-loading.ts'
import { UXConfig } from '@/utils/device-utils.ts'
import { useRunOnce } from '@/utils/run-once.ts'
import { Chronoday } from "@/model/chrono.ts"
import { createReviewSessionAttendant } from "@/core-logic/review-session-attendant.ts"

defineProps<{
  sessionId?: number,
  stages: Stage[],
}>()

const router = useRouter()
const toaster = useSpaceToaster()
const toggleStore = useToggleStore()
const chronoStore = useChronoStore()
const flashcardStore = useFlashcardStore()

const { flashcardSet, flashcards } = storeToRefs(flashcardStore)
const { currDay } = storeToRefs(chronoStore)

const {
  loadingStarted,
  resolvedLoading,
  startLoading,
  stopLoading,
} = useDeferredLoading()

const sessionRunner = createReviewSessionAttendant(ReviewSessionType.LIGHTSPEED, flashcardSet, currDay)
const reviewStore = useReviewStore(ReviewSessionType.LIGHTSPEED, flashcardSet)

const {
  flashcardsTotal,
  currFlashcard,
  flashcardsRemaining,
  flashcardsSeen,
  progress,
  noOneAvailable,
  noNextAvailable,
} = storeToRefs(reviewStore)

const { runOnce: startReviewOnce, isPending: reviewStarting } = useRunOnce(startReview)
const { runOnce: finishReviewOnce } = useRunOnce(finishReview)

const flashcardSetName = computed(() => flashcardSet.value?.name || '')

const spaceDeck = ref<InstanceType<typeof SpaceDeck>>()

async function startReview() {
  Log.log(LogTag.LOGIC, `Starting review: ${ReviewSessionType.LIGHTSPEED}`)
  try {
    sessionRunner.clear()
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
    reviewStore.loadState(createReviewQueue(flashcards.value, currDay.value))

    await sessionRunner.create()
    await reviewStore.nextFlashcard()

    Log.log(LogTag.LOGIC, `Flashcards TOTAL: ${flashcardsTotal.value}`)
  } finally {
    await stopLoading()
  }
  spaceDeck.value?.setDeckReady()
}

async function finishReview() {
  Log.log(LogTag.LOGIC, `Finishing review: ${ReviewSessionType.LIGHTSPEED}`)
  if (reviewStarting.value) await startReviewOnce()
  await sessionRunner.flush({ all: true })
  if (flashcardSet.value && noOneAvailable.value) {
    await markDaysAsCompleted(flashcardSet.value, currDay.value)
  }
  Log.log(LogTag.LOGIC, `Finished review: ${ReviewSessionType.LIGHTSPEED}`)
}

async function finishReviewAndLeave() {
  await finishReviewOnce()
  await router.push({ name: routeNames.controlPanel })
}

async function stageDown() {
  if (!flashcardSet.value || !currFlashcard.value) return
  const flashcard = copyFlashcard(currFlashcard.value)
  updateFlashcard(flashcard, prevStage(flashcard.stage), currDay.value.chronodate)
  const success = await sendUpdatedFlashcard(flashcardSet.value, flashcard)
  if (success) {
    sessionRunner.track(currFlashcard.value.id)
    await sessionRunner.flush({ all: noNextAvailable.value })
    await getNextAndMarkDays(flashcardSet.value, currDay.value)
  }
}

async function stageUp() {
  if (!flashcardSet.value || !currFlashcard.value) return
  const flashcard = copyFlashcard(currFlashcard.value)
  updateFlashcard(flashcard, nextStage(flashcard.stage), currDay.value.chronodate)
  const success = await sendUpdatedFlashcard(flashcardSet.value, flashcard)
  if (success) {
    sessionRunner.track(currFlashcard.value.id)
    await sessionRunner.flush({ all: noNextAvailable.value })
    await getNextAndMarkDays(flashcardSet.value, currDay.value)
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

async function getNextAndMarkDays(flashcardSet: FlashcardSet, currDay: Chronoday) {
  if (await reviewStore.nextFlashcard()) {
    await markDaysAsInProgress(flashcardSet, currDay)
  } else {
    await markDaysAsCompleted(flashcardSet, currDay)
  }
}

async function markDaysAs(
  flashcardSetId: number,
  currDay: Chronoday,
  status: string,
  acceptedStatuses: Set<string>,
) {
  if (!acceptedStatuses.has(currDay.status)) return
  if (currDay.status === status) return

  await sendChronoBulkUpdateRequest(flashcardSetId, status, [currDay])
    .then((response) => {
      chronoStore.updateDays(response.data.chronodays)
      chronoStore.updateDayStreak(response.data.dayStreak)
    })
    .catch((error) => {
      Log.error(LogTag.LOGIC, `Failed to mark days as ${status} for FlashcardSet.id=${flashcardSetId}`, error.response?.data)
      toaster.bakeError(userApiErrors.SCHEDULE__UPDATING_FAILED, error.response?.data)
    })
}

async function markDaysAsInProgress(flashcardSet: FlashcardSet, currDay: Chronoday) {
  await markDaysAs(flashcardSet.id, currDay, chronodayStatuses.IN_PROGRESS, chronodayStatusesToProgressDay)
}

async function markDaysAsCompleted(flashcardSet: FlashcardSet, currDay: Chronoday) {
  await markDaysAs(flashcardSet.id, currDay, chronodayStatuses.COMPLETED, chronodayStatusesToCompleteDay)
}

onMounted(async () => {
  await startReviewOnce()
  document.addEventListener('keydown', handleKeydown)
})

onBeforeRouteLeave(async () => {
  await finishReviewOnce()
})

onUnmounted(async () => {
  await finishReviewOnce()
  sessionRunner.clear()
  destroyReviewStore(reviewStore)
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
  font-size: 18px;
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
