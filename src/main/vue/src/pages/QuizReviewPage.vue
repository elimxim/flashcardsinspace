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
      :center-title-padding="40"
      center-title
    >
      <template #left>
        <div class="review-mode">
          Quiz
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
      <div class="review-progressbar">
        <Progressbar
          :progress="progress"
          height="16px"
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
          :session-type="ReviewSessionType.QUIZ"
          :on-flashcard-removed="onFlashcardRemoved"
          :on-audio-changed="onAudioChanged"
          :can-slide-left="!noNextAvailable"
          :can-slide-right="!noNextAvailable"
          :on-slide-left="() => quizAnswer(false)"
          :on-slide-right="() => quizAnswer(true)"
          swipe-left-text="Don't know"
          swipe-right-text="Know"
        >
          <QuizResult
            :elapsed-time="elapsedTime"
            :round="quizRound"
            :overall-total="quizOverallTotal"
            :overall-correct="quizOverallCorrect"
            :round-total="flashcardsTotal"
            :round-failed="incorrectFlashcards.length"
            :on-next-round="startNextQuizRound"
            :on-finish="finishReviewAndLeave"
          />
        </SpaceDeck>
        <div v-if="!isTouchDevice" class="review-nav">
          <SmartButton
            class="decision-button dangerous-button"
            text="Don't know"
            :disabled="noNextAvailable"
            :hidden="noNextAvailable"
            :on-click="spaceDeck?.slideLeft"
            auto-blur
            rounded
          />
          <SmartButton
            class="decision-button safe-button"
            text="Know"
            :disabled="noNextAvailable"
            :hidden="noNextAvailable"
            :on-click="spaceDeck?.slideRight"
            auto-blur
            rounded
          />
        </div>
      </div>
    </div>
  </div>
  <SpaceToast/>
</template>

<script setup lang="ts">
import ControlBar from '@/components/ControlBar.vue'
import Progressbar from '@/components/Progressbar.vue'
import SpaceDeck from '@/components/review/SpaceDeck.vue'
import SmartButton from '@/components/SmartButton.vue'
import AwesomeButton from '@/components/AwesomeButton.vue'
import SpaceToast from '@/components/SpaceToast.vue'
import QuizResult from '@/components/review/QuizResult.vue'
import { useFlashcardStore } from '@/stores/flashcard-store.ts'
import { useStopWatch } from '@/utils/stop-watch.ts'
import { isTouchDevice } from '@/utils/utils.ts'
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { Stage } from '@/core-logic/stage-logic.ts'
import { useChronoStore } from '@/stores/chrono-store.ts'
import {
  createReviewQueueForStages,
  MonoStageReviewQueue,
  ReviewSessionType
} from '@/core-logic/review-logic.ts'
import { routeNames } from '@/router'
import { useRouter } from 'vue-router'
import { loadSelectedSetIdFromCookies, } from '@/utils/cookies.ts'
import { useToggleStore } from '@/stores/toggle-store.ts'
import { Flashcard } from '@/model/flashcard.ts'
import { loadFlashcardRelatedStoresById } from '@/utils/stores.ts'
import {
  sendReviewSessionChildCreateRequest,
  sendReviewSessionCreateRequest,
  sendReviewSessionGetRequest,
  sendReviewSessionUpdateRequest,
} from '@/api/api-client.ts'
import { useSpaceToaster } from '@/stores/toast-store.ts'
import { ReviewSessionCreateRequest } from '@/api/communication.ts'
import { Log, LogTag } from '@/utils/logger.ts'
import { userApiErrors } from '@/api/user-api-error.ts'
import { destroyReviewStore, useReviewStore } from '@/stores/review-store.ts'

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
const { currDay } = storeToRefs(chronoStore)

const reviewStore = useReviewStore(ReviewSessionType.QUIZ)

const {
  reviewQueue,
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

const spaceDeck = ref<InstanceType<typeof SpaceDeck>>()

const quizRound = ref(1)
const quizOverallTotal = ref(0)
const quizOverallCorrect = ref(0)
const reviewedFlashcardIds = ref<number[]>([])
const incorrectFlashcards = ref<Flashcard[]>([])

async function startReview() {
  Log.log(LogTag.LOGIC, `Starting review: ${ReviewSessionType.QUIZ}`)
  reviewStore.loadState(createReviewQueueForStages(flashcards.value, props.stages, currDay.value))
  quizOverallTotal.value = flashcardsTotal.value
  await loadOrCreateQuizSession()
  await reviewStore.nextFlashcard(flashcardSet.value, (success) => {
    if (success) startWatch()
  })
  Log.log(LogTag.LOGIC, `Flashcards TOTAL: ${flashcardsTotal.value}`)
}

async function loadOrCreateQuizSession() {
  if (props.sessionId) {
    await loadQuizSession(props.sessionId)
  } else {
    await createQuizSession()
  }
}

function resetState() {
  stopWatch()
  reviewStore.resetState()
  reviewedFlashcardIds.value = []
  incorrectFlashcards.value = []
  quizOverallTotal.value = 0
  quizOverallCorrect.value = 0
}

function finishReview() {
  Log.log(LogTag.LOGIC, `Finishing review: ${ReviewSessionType.QUIZ}`)
  currFlashcardWatcher.stop()
  resetState()
}

function finishReviewAndLeave() {
  finishReview()
  router.push({ name: routeNames.controlPanel })
}

async function quizAnswer(know: boolean) {
  if (!currFlashcard.value) return
  if (know) {
    quizOverallCorrect.value = quizOverallCorrect.value + 1
    await updateQuizSession([currFlashcard.value.id], [])
  } else {
    incorrectFlashcards.value.push(currFlashcard.value)
    await updateQuizSession([currFlashcard.value.id], [currFlashcard.value.id])
  }
  await reviewStore.nextFlashcard(flashcardSet.value)
}

async function startNextQuizRound() {
  if (!flashcardSet.value || !props.sessionId) return
  if (incorrectFlashcards.value.length === 0) {
    Log.error(LogTag.LOGIC, 'Cannot start new round: no incorrect flashcards')
    return
  }

  await sendReviewSessionChildCreateRequest(flashcardSet.value.id, props.sessionId, {
    type: ReviewSessionType.QUIZ,
    chronodayId: currDay.value.id,
  })
    .then((response) => {
      Log.log(LogTag.LOGIC, `Child review session ${response.data.id} created, parent: ${props.sessionId}`)
      router.replace({
        query: {
          ...router.currentRoute.value.query,
          sessionId: response.data.id,
        }
      })

      reviewStore.loadState(new MonoStageReviewQueue(incorrectFlashcards.value))
      quizRound.value = quizRound.value + 1
      incorrectFlashcards.value = []

      spaceDeck.value?.setDeckReady()

      startWatch()

      return reviewStore.nextFlashcard(flashcardSet.value)
    })
    .catch((error) => {
      Log.error(LogTag.LOGIC, `Failed to create child review session`, error.response?.data)
      toaster.bakeError(userApiErrors.QUIZ_SESSION__NEXT_ROUND_FAILED, error.response?.data)
    })
}

function onFlashcardRemoved() {
  reviewStore.nextFlashcard(flashcardSet.value)
}

function onAudioChanged() {
  reviewStore.fetchAudio(flashcardSet.value)
}

const currFlashcardWatcher = watch(currFlashcard, async (newVal) => {
  if (!newVal) {
    stopWatch()
    await updateQuizSession(
      reviewedFlashcardIds.value,
      incorrectFlashcards.value.map(f => f.id),
      true
    )
  }
})

async function createQuizSession() {
  if (!flashcardSet.value) return

  const request: ReviewSessionCreateRequest = {
    type: ReviewSessionType.QUIZ,
    chronodayId: currDay.value.id,
    metadata: {
      currRoundFlashcardIds: reviewQueue.value.remainingFlashcards().map(f => f.id),
      overallTotalCount: reviewQueue.value.remaining(),
    }
  }

  await sendReviewSessionCreateRequest(flashcardSet.value.id, request)
    .then((response) => {
      Log.log(LogTag.LOGIC, `Quiz session ${response.data.id} created`)
      router.replace({
        query: {
          ...router.currentRoute.value.query,
          sessionId: response.data.id,
        }
      })
    })
    .catch((error) => {
      Log.error(LogTag.LOGIC, `Failed to create a quiz session`, error.response?.data)
      toaster.bakeError(userApiErrors.REVIEW_SESSION__CREATION_FAILED, error.response?.data)
    })
}

async function loadQuizSession(sessionId: number) {
  if (!flashcardSet.value) return
  await sendReviewSessionGetRequest(flashcardSet.value.id, sessionId)
    .then((response) => {
      elapsedTime.value = response.data.elapsedTime
      quizRound.value = response.data.metadata?.round ?? 1
      quizOverallCorrect.value = response.data.metadata?.overallCorrectCount ?? 0
      quizOverallTotal.value = response.data.metadata?.overallTotalCount ?? 0
      const reviewedFlashcardIdSet = new Set(response.data.flashcardIds ?? [])
      const nextRoundFlashcardIdSet = new Set(response.data.metadata?.nextRoundFlashcardIds ?? [])
      const currRoundFlashcardIdSet = new Set(response.data.metadata?.currRoundFlashcardIds ?? [])
      const currRoundFlashcards = flashcards.value.filter(f => currRoundFlashcardIdSet.has(f.id))
      const flashcardsForReview = currRoundFlashcards.filter(f => !reviewedFlashcardIdSet.has(f.id))
      reviewedFlashcardIds.value = [...reviewedFlashcardIdSet]
      incorrectFlashcards.value = currRoundFlashcards.filter(f => nextRoundFlashcardIdSet.has(f.id))
      reviewStore.loadState(new MonoStageReviewQueue(flashcardsForReview))
      reviewStore.setFlashcardsTotal(currRoundFlashcards.length)
      Log.log(LogTag.LOGIC, `Quiz session ${sessionId} retrieved`)
    })
    .catch((error) => {
      resetState()
      Log.error(LogTag.LOGIC, `Failed to retrieve quiz session ${sessionId}`, error.response?.data)
      toaster.bakeError(userApiErrors.REVIEW_SESSION__FETCHING_FAILED, error.response?.data)
    })
}

async function updateQuizSession(reviewedFlashcardIds: number[], nextRoundFlashcardIds: number[], finished: boolean = false) {
  if (!flashcardSet.value || !props.sessionId) return
  await sendReviewSessionUpdateRequest(flashcardSet.value.id, props.sessionId, {
    elapsedTime: elapsedTime.value,
    flashcardIds: reviewedFlashcardIds.map(id => ({ id: id })),
    finished: finished,
    metadata: {
      nextRoundFlashcardIds: nextRoundFlashcardIds,
      overallCorrectCount: quizOverallCorrect.value,
    },
  })
    .then(() => {
      Log.log(LogTag.LOGIC, `Quiz session ${props.sessionId} updated`)
    })
    .catch((error) => {
      Log.error(LogTag.LOGIC, `Failed to update quiz session ${props.sessionId}`, error.response?.data)
      toaster.bakeError(userApiErrors.REVIEW_SESSION__UPDATING_FAILED, error.response?.data)
    })
}

onMounted(async () => {
  reviewStore.resetState()
  if (!flashcardStore.loaded) {
    Log.log(LogTag.LOGIC, 'Flashcard set is not loaded, loading...')
    const selectedSetId = loadSelectedSetIdFromCookies()
    if (selectedSetId) {
      await loadFlashcardRelatedStoresById(selectedSetId)
    } else {
      Log.log(LogTag.LOGIC, 'Flashcard set not found in cookies')
    }
  }
  await startReview()
  spaceDeck.value?.setDeckReady()
  document.addEventListener('keydown', handleKeydown)
})

onUnmounted(async () => {
  finishReview()
  destroyReviewStore(ReviewSessionType.QUIZ)
  document.removeEventListener('keydown', handleKeydown)
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

.decision-button {
  --smart-button--width: 130px;
}

</style>
