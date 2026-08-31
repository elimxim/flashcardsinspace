<template>
  <div
    :class="[
      'page',
      { 'page--bg--light': !reviewMode.isOuterSpace() },
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
      <template v-if="reviewMode.topic" #left>
        <div class="review-mode">
          <font-awesome-icon :icon="reviewIcons.get(reviewMode.sessionType)!!"/>
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
      <Starfield
        v-if="reviewMode.isOuterSpace()"
        :density="120"
        :star-size="1.8"
        twinkle
        vertical-drift="3px"
      />
      <KineticRingSpinner
        v-if="resolvedLoading"
        :ring-size="240"
        :track-color="reviewMode.isOuterSpace() ? 'rgb(28,20,57)' : '#f1f5f9'"
      />
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
            :session-type="reviewMode.sessionType"
            :can-slide-left="!noPrevAvailable"
            :can-slide-right="!noNextAvailable"
            :on-slide-left="prev"
            :on-slide-right="next"
            swipe-left-text="Prev"
            swipe-right-text="Next"
          >
            <ReviewResult/>
          </SpaceDeck>
          <div v-if="UXConfig().showNavButtons" class="review-nav">
            <SmartButton
              class="calm-button"
              text="Prev"
              :disabled="noPrevAvailable"
              :on-click="spaceDeck?.slideLeft"
              auto-blur
              rounded
            />
            <SmartButton
              v-if="reviewMode.isOuterSpace()"
              class="decision-button dangerous-button"
              text="Move back"
              :disabled="noNextAvailable"
              :hidden="noNextAvailable"
              :on-click="moveBack"
              :hold-time="1.2"
              auto-blur
              rounded
            />
            <SmartButton
              class="calm-button"
              text="Next"
              :disabled="noNextAvailable"
              :on-click="spaceDeck?.slideRight"
              auto-blur
              rounded
            />
          </div>
          <div
            v-else-if="reviewMode.isOuterSpace()"
            class="review-nav review-nav--centered"
          >
            <SmartButton
              class="decision-button dangerous-button"
              text="Move back"
              :disabled="noNextAvailable"
              :on-click="moveBack"
              :hold-time="1.2"
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
import Starfield from '@/components/common/Starfield.vue'
import ReviewResult from '@/components/ReviewResult.vue'
import { useFlashcardStore } from '@/stores/flashcard-store.ts'
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { storeToRefs } from 'pinia'
import { copyFlashcard, updateFlashcard } from '@/core-logic/flashcard-logic.ts'
import { learningStages, specialStages } from '@/core-logic/stage-logic.ts'
import { useChronoStore } from '@/stores/chrono-store.ts'
import {
  createReviewQueueForStages,
  reviewIcons,
  ReviewMode,
  reviewSessionTypeToSpecialStage,
} from '@/core-logic/review-logic.ts'
import { routeNames } from '@/router'
import { onBeforeRouteLeave, useRouter } from 'vue-router'
import { loadSelectedSetIdFromCookies } from '@/utils/cookies.ts'
import { useToggleStore } from '@/stores/toggle-store.ts'
import { Flashcard, FlashcardSet } from '@/model/flashcard.ts'
import { loadStoresForFlashcardSetId } from '@/utils/store-loading.ts'
import {
  sendFlashcardUpdateRequest,
} from '@/api/api-client.ts'
import { useSpaceToaster } from '@/stores/toast-store.ts'
import { Log, LogTag } from '@/utils/logger.ts'
import { userApiErrors } from '@/api/user-api-error.ts'
import { destroyReviewStore, useReviewStore } from '@/stores/review-store.ts'
import { useDeferredLoading } from '@/utils/deferred-loading.ts'
import { UXConfig } from '@/utils/device-utils.ts'
import { useRunOnce } from '@/utils/run-once.ts'
import { createReviewSessionAttendant } from "@/core-logic/review-session-attendant.ts"

const props = defineProps<{
  sessionId?: number,
  reviewMode: ReviewMode,
}>()

const router = useRouter()
const toaster = useSpaceToaster()
const toggleStore = useToggleStore()
const chronoStore = useChronoStore()
const flashcardStore = useFlashcardStore()

const { runOnce: startReviewOnce, isPending: reviewStarting } = useRunOnce(startReview)
const { runOnce: finishReviewOnce } = useRunOnce(finishReview)

const { flashcardSet, flashcards } = storeToRefs(flashcardStore)
const { currDay } = storeToRefs(chronoStore)

const {
  loadingStarted,
  resolvedLoading,
  startLoading,
  stopLoading,
} = useDeferredLoading()

const sessionRunner = createReviewSessionAttendant(props.reviewMode.sessionType, flashcardSet, currDay)
const reviewStore = useReviewStore(props.reviewMode.sessionType, flashcardSet)

const {
  reviewQueue,
  flashcardsTotal,
  currFlashcard,
  flashcardsRemaining,
  flashcardsSeen,
  progress,
  noPrevAvailable,
  noNextAvailable,
} = storeToRefs(reviewStore)

const flashcardSetName = computed(() => flashcardSet.value?.name || '')

const spaceDeck = ref<InstanceType<typeof SpaceDeck>>()

async function startReview() {
  Log.log(LogTag.LOGIC, `Starting review: ${props.reviewMode.sessionType}`)
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
    const stage = reviewSessionTypeToSpecialStage(props.reviewMode.sessionType) ?? specialStages.UNKNOWN
    reviewStore.loadState(createReviewQueueForStages(flashcards.value, [stage], currDay.value))

    await sessionRunner.create()
    await reviewStore.nextFlashcard()

    Log.log(LogTag.LOGIC, `Flashcards TOTAL: ${flashcardsTotal.value}`)

  } finally {
    await stopLoading()
  }
  spaceDeck.value?.setDeckReady()
}

async function finishReview() {
  Log.log(LogTag.LOGIC, `Finishing review: ${props.reviewMode.sessionType}`)
  if (reviewStarting.value) await startReviewOnce()
  await sessionRunner.flush({ all: true })
  Log.log(LogTag.LOGIC, `Finished review: ${props.reviewMode.sessionType}`)
}

async function finishReviewAndLeave() {
  await finishReviewOnce()
  await router.push({ name: routeNames.controlPanel })
}

async function prev() {
  if (noPrevAvailable.value) return
  await sessionRunner.flush()
  await reviewStore.prevFlashcard()
}

async function next() {
  if (!currFlashcard.value || noNextAvailable.value) return
  sessionRunner.track(currFlashcard.value.id)
  await sessionRunner.flush()
  await reviewStore.nextFlashcard()
}

async function moveBack() {
  if (!flashcardSet.value || !currFlashcard.value) {
    Log.error(LogTag.LOGIC, `moveBack is impossible:`,
      `FlashcardSet.id=${flashcardSet.value?.id ?? 'undefined'}`,
      `current Flashcard.id=${currFlashcard.value?.id ?? 'undefined'}`,
    )
    return
  }
  const flashcard = copyFlashcard(currFlashcard.value)
  updateFlashcard(flashcard, learningStages.S1, currDay.value.chronodate)
  const success = await sendUpdatedFlashcard(flashcardSet.value, flashcard)
  if (success) {
    reviewQueue.value.removeCurrent()
    await spaceDeck.value?.animateOutLeft(true)
    await reviewStore.nextFlashcard()
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

.review-nav--centered {
  justify-content: center;
}

.decision-button {
  --smart-button--width: 130px;
}

</style>
