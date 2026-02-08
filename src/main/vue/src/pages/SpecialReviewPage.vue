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
          {{ reviewMode.topic }}
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
            :on-flashcard-removed="onFlashcardRemoved"
            :on-audio-changed="onAudioChanged"
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
import KineticRingSpinner from '@/components/KineticRingSpinner.vue'
import Progressbar from '@/components/Progressbar.vue'
import SpaceDeck from '@/components/review/SpaceDeck.vue'
import SmartButton from '@/components/SmartButton.vue'
import AwesomeButton from '@/components/AwesomeButton.vue'
import SpaceToast from '@/components/SpaceToast.vue'
import Starfield from '@/components/Starfield.vue'
import ReviewResult from '@/components/review/ReviewResult.vue'
import { useFlashcardStore } from '@/stores/flashcard-store.ts'
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { storeToRefs } from 'pinia'
import { copyFlashcard, updateFlashcard } from '@/core-logic/flashcard-logic.ts'
import { learningStages, specialStages } from '@/core-logic/stage-logic.ts'
import { useChronoStore } from '@/stores/chrono-store.ts'
import {
  createReviewQueueForStages,
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
  sendReviewSessionCreateRequest,
  sendReviewSessionUpdateRequest
} from '@/api/api-client.ts'
import { useSpaceToaster } from '@/stores/toast-store.ts'
import { Log, LogTag } from '@/utils/logger.ts'
import { userApiErrors } from '@/api/user-api-error.ts'
import { destroyReviewStore, useReviewStore } from '@/stores/review-store.ts'
import { useDeferredLoading } from '@/utils/deferred-loading.ts'
import { useStopWatch } from '@/utils/stop-watch.ts'
import { ReviewSessionCreateRequest } from '@/api/communication.ts'
import { UXConfig } from '@/utils/device-utils.ts'

const props = defineProps<{
  sessionId?: number,
  reviewMode: ReviewMode,
}>()

const router = useRouter()
const toaster = useSpaceToaster()
const toggleStore = useToggleStore()
const chronoStore = useChronoStore()
const flashcardStore = useFlashcardStore()
const hasCleanedUp = ref(false)

const { flashcardSet, flashcards } = storeToRefs(flashcardStore)
const { currDay } = storeToRefs(chronoStore)

const {
  loadingStarted,
  resolvedLoading,
  startLoading,
  stopLoading,
} = useDeferredLoading()

const reviewStore = useReviewStore(props.reviewMode.sessionType)

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
const reviewedFlashcardIds = ref<number[]>([])

const elapsedTime = ref(0)
const { startWatch, stopWatch } = useStopWatch(elapsedTime)

const spaceDeck = ref<InstanceType<typeof SpaceDeck>>()

async function startReview() {
  Log.log(LogTag.LOGIC, `Starting review: ${props.reviewMode.sessionType}`)
  const stage = reviewSessionTypeToSpecialStage(props.reviewMode.sessionType) ?? specialStages.UNKNOWN
  reviewStore.loadState(createReviewQueueForStages(flashcards.value, [stage], currDay.value))
  await createReviewSession()
  await reviewStore.nextFlashcard(flashcardSet.value, (success) => {
    if (success) startWatch()
  })
  Log.log(LogTag.LOGIC, `Flashcards TOTAL: ${flashcardsTotal.value}`)
}

async function finishReview() {
  Log.log(LogTag.LOGIC, `Finishing review: ${props.reviewMode.sessionType}`)
  stopWatch()
  await updateReviewSession(reviewedFlashcardIds.value, true)
  reviewStore.$reset()
  reviewedFlashcardIds.value = []
}

function finishReviewAndLeave() {
  finishReview()
  router.push({ name: routeNames.controlPanel })
}

async function prev() {
  if (noPrevAvailable.value) return
  await updateReviewSession([])
  await reviewStore.prevFlashcard(flashcardSet.value)
}

async function next() {
  if (!currFlashcard.value || noNextAvailable.value) return
  reviewedFlashcardIds.value.push(currFlashcard.value.id)
  await updateReviewSession([currFlashcard.value.id])
  await reviewStore.nextFlashcard(flashcardSet.value)
}

async function moveBack() {
  if (!flashcardSet.value || !currFlashcard.value) {
    Log.error(LogTag.LOGIC, `moveBack is impossible:`,
      `FlashcardSet.id=${flashcardSet.value?.id ?? 'undefined'}`,
      `current Flashcard.id=${currFlashcard.value?.id ?? 'undefined'}`
    )
    return
  }
  const flashcard = copyFlashcard(currFlashcard.value)
  updateFlashcard(flashcard, learningStages.S1, currDay.value.chronodate)
  const success = await sendUpdatedFlashcard(flashcardSet.value, flashcard)
  if (success) {
    reviewQueue.value.removeCurrent()
    await spaceDeck.value?.animateOutLeft(true)
    await reviewStore.nextFlashcard(flashcardSet.value)
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

async function createReviewSession() {
  if (!flashcardSet.value) return

  const request: ReviewSessionCreateRequest = {
    type: props.reviewMode.sessionType,
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
      Log.error(LogTag.LOGIC, `Failed to update review session ${props.sessionId}`, error.response?.data)
      toaster.bakeError(userApiErrors.REVIEW_SESSION__UPDATING_FAILED, error.response?.data)
    })
}

function onFlashcardRemoved() {
  reviewQueue.value.removeCurrent()
  reviewStore.nextFlashcard(flashcardSet.value)
}

function onAudioChanged() {
  reviewStore.fetchAudio(flashcardSet.value)
}

async function cleanUpReview() {
  if (hasCleanedUp.value) {
    return
  }

  hasCleanedUp.value = true

  await finishReview()
  destroyReviewStore(props.reviewMode.sessionType)
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

onBeforeRouteLeave(async () => {
  await cleanUpReview()
})

onUnmounted(async () => {
  document.removeEventListener('keydown', handleKeydown)
  await cleanUpReview()
})

async function handleKeydown(event: KeyboardEvent) {
  if (toggleStore.isAnyModalOpen()) return

  if (event.key === 'Escape') {
    event.stopPropagation()
    finishReviewAndLeave()
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

.review-nav--centered {
  justify-content: center;
}

.decision-button {
  --smart-button--width: 130px;
}

</style>
