<template>
  <div
    :class="[
      'page',
      { 'page--bg--light': !reviewMode.isOuterSpace() },
      'flex-column',
      'flex-center',
      'scroll-none',
      'review-page--theme',
    ]"
  >
    <ControlBar :title="flashcardSetName" center-title>
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
      <div class="review-progressbar">
        <Progressbar
          :progress="progress"
          height="16px"
        />
      </div>
      <div class="review-info">
        <div class="review-count review-count--left">
          {{ flashcardsSeen }}
        </div>
        <div class="review-count review-count--right">
          {{ flashcardsRemaining }}
        </div>
      </div>
      <div class="review-body">
        <SpaceDeck
          ref="spaceDeck"
          v-model:flashcard="currFlashcard"
          v-model:auto-play-voice="autoPlayVoice"
          v-model:auto-repeat-voice="autoRepeatVoice"
          :on-flashcard-removed="onFlashcardRemoved"
          :on-audio-changed="onAudioChanged"
          :flashcard-front-side-audio="flashcardFrontSideAudioBlob"
          :flashcard-back-side-audio="flashcardBackSideAudioBlob"
          :show-slot="reviewMode.isQuiz()"
        >
          <QuizResult
            v-if="reviewMode.isQuiz()"
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
        <div class="review-nav">
          <template v-if="reviewMode.isLightspeed()">
            <SmartButton
              text="Don't know"
              class="decision-button dangerous-button"
              :disabled="noNextAvailable"
              :hidden="noNextAvailable"
              :on-click="stageDown"
              auto-blur
              rounded
            />
            <SmartButton
              text="Know"
              class="decision-button safe-button"
              :disabled="noNextAvailable"
              :hidden="noNextAvailable"
              :on-click="stageUp"
              auto-blur
              rounded
            />
          </template>
          <template v-if="reviewMode.isSpecial()">
            <SmartButton
              class="calm-button"
              text="Prev"
              :disabled="noPrevAvailable"
              :on-click="prev"
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
              :on-click="next"
              auto-blur
              rounded
            />
          </template>
          <template v-if="reviewMode.isQuiz()">
            <SmartButton
              class="decision-button dangerous-button"
              text="Don't know"
              :disabled="noNextAvailable"
              :hidden="noNextAvailable"
              :on-click="() => quizAnswer(false)"
              auto-blur
              rounded
            />
            <SmartButton
              class="decision-button safe-button"
              text="Know"
              :disabled="noNextAvailable"
              :hidden="noNextAvailable"
              :on-click="() => quizAnswer(true)"
              auto-blur
              rounded
            />
          </template>
        </div>
      </div>
    </div>
  </div>
  <SpaceToast/>
</template>

<script setup lang="ts">
import ControlBar from '@/components/ControlBar.vue'
import Progressbar from '@/components/Progressbar.vue'
import SpaceDeck from '@/components/SpaceDeck.vue'
import SmartButton from '@/components/SmartButton.vue'
import AwesomeButton from '@/components/AwesomeButton.vue'
import SpaceToast from '@/components/SpaceToast.vue'
import Starfield from '@/components/Starfield.vue'
import QuizResult from '@/components/QuizResult.vue'
import { useFlashcardStore } from '@/stores/flashcard-store.ts'
import { useStopWatch } from '@/utils/stop-watch.ts'
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { storeToRefs } from 'pinia'
import {
  copyFlashcard,
  fetchFlashcardAudio,
  updateFlashcard
} from '@/core-logic/flashcard-logic.ts'
import {
  nextStage,
  prevStage,
  Stage,
  learningStages,
} from '@/core-logic/stage-logic.ts'
import { useChronoStore } from '@/stores/chrono-store.ts'
import {
  createReviewQueue,
  createReviewQueueForStages,
  EmptyReviewQueue,
  ReviewQueue,
  determineReviewMode,
  MonoStageReviewQueue,
  reviewSessionTypeToSpecialStage,
  ReviewSessionType
} from '@/core-logic/review-logic.ts'
import { routeNames } from '@/router'
import { onBeforeRouteLeave, useRouter } from 'vue-router'
import {
  loadSelectedSetIdFromCookies,
} from '@/utils/cookies.ts'
import { useToggleStore } from '@/stores/toggle-store.ts'
import { Flashcard, FlashcardSet } from '@/model/flashcard.ts'
import { loadFlashcardRelatedStoresById } from '@/utils/stores.ts'
import {
  sendChronoBulkUpdateRequest,
  sendFlashcardUpdateRequest,
  sendReviewSessionChildCreateRequest,
  sendReviewSessionCreateRequest,
  sendReviewSessionGetRequest,
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
import { quizSessionIdCookie } from '@/utils/cookies-ref.ts'
import { Log, LogTag } from '@/utils/logger.ts'
import { userApiErrors } from '@/api/user-api-error.ts'

const props = defineProps<{
  sessionType?: string,
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

const reviewMode = computed(() => determineReviewMode(props.sessionType, props.stages))
const reviewQueue = ref<ReviewQueue>(new EmptyReviewQueue())
const reviewSessionId = ref<number>()
const elapsedTime = ref(0)
const flashcardSetName = computed(() => flashcardSet.value?.name || '')
const reviewedFlashcardIds = ref<number[]>([])
const incorrectFlashcards = ref<Flashcard[]>([])
const flashcardsTotal = ref(0)
const flashcardsRemaining = computed(() => {
  if (noNextAvailable.value) return 0
  return reviewQueue.value.remaining() + 1
})
const flashcardsSeen = computed(() =>
  Math.max(0, flashcardsTotal.value - flashcardsRemaining.value)
)
const progress = computed(() => {
  const completionRate = flashcardsSeen.value / flashcardsTotal.value
  if (completionRate) {
    return Math.max(0, Math.min(1, completionRate))
  } else {
    return 0
  }
})

const noNextAvailable = computed(() => currFlashcard.value === undefined)
const noPrevAvailable = computed(() => {
  if (flashcardsTotal.value === 1) {
    return !noNextAvailable.value
  }
  return flashcardsTotal.value === flashcardsRemaining.value
})

const spaceDeck = ref<InstanceType<typeof SpaceDeck>>()
const currFlashcard = ref<Flashcard>()
const flashcardFrontSideAudioBlob = ref<Blob | undefined>()
const flashcardBackSideAudioBlob = ref<Blob | undefined>()
const autoPlayVoice = ref(false)
const autoRepeatVoice = ref(false)
const quizRound = ref(1)
const quizOverallTotal = ref(0)
const quizOverallCorrect = ref(0)

const { startWatch, stopWatch } = useStopWatch(elapsedTime)

async function prevFlashcard(): Promise<boolean> {
  currFlashcard.value = reviewQueue.value.prev()
  await fetchAudio()
  return currFlashcard.value !== undefined
}

async function nextFlashcard(): Promise<boolean> {
  currFlashcard.value = reviewQueue.value.next()
  await fetchAudio()
  if (!currFlashcard.value) {
    stopWatch()
  }
  return currFlashcard.value !== undefined
}

async function startReview() {
  Log.log(LogTag.LOGIC, `Starting review: ${JSON.stringify(reviewMode.value)}`)
  if (reviewMode.value.isLightspeed()) {
    reviewQueue.value = createReviewQueue(flashcards.value, currDay.value, chronodays.value)
  } else if (reviewMode.value.isSpecial()) {
    const stage = reviewSessionTypeToSpecialStage(reviewMode.value.sessionType)
    if (stage) {
      reviewQueue.value = createReviewQueueForStages(flashcards.value, [stage], currDay.value)
    }
  } else {
    reviewQueue.value = createReviewQueueForStages(flashcards.value, props.stages, currDay.value)
  }
  flashcardsTotal.value = reviewQueue.value.remaining()
  quizOverallTotal.value = flashcardsTotal.value
  if (reviewMode.value.isQuiz()) {
    await loadOrCreateQuizSession()
  } else {
    await createReviewSession()
  }
  if (await nextFlashcard()) {
    startWatch()
  }
  Log.log(LogTag.LOGIC, `Flashcards TOTAL: ${flashcardsTotal.value}`)
}

async function loadOrCreateQuizSession() {
  if (props.sessionId) {
    await loadQuizSession(props.sessionId)
  } else  {
    await createReviewSession(true).then(() => {
      quizSessionIdCookie.value = reviewSessionId.value
    })
  }
}

function resetState() {
  stopWatch()
  reviewQueue.value = new EmptyReviewQueue()
  reviewSessionId.value = undefined
  reviewedFlashcardIds.value = []
  incorrectFlashcards.value = []
  flashcardsTotal.value = 0
  flashcardFrontSideAudioBlob.value = undefined
  flashcardBackSideAudioBlob.value = undefined
  autoPlayVoice.value = false
  autoRepeatVoice.value = false
  quizOverallTotal.value = 0
  quizOverallCorrect.value = 0
}

async function finishReview() {
  Log.log(LogTag.LOGIC, `Finishing review: ${JSON.stringify(reviewMode.value)}`)
  resetState()
  if (flashcardSet.value) {
    if (noNextAvailable.value && reviewMode.value.isLightspeed()) {
      await markDaysAsCompleted(flashcardSet.value)
    }
  } else {
    Log.error(LogTag.LOGIC, 'Couldn\'t gracefully finish review: FlashcardSet is undefined')
  }
}

async function finishReviewAndLeave() {
  await finishReview()
    .then(() =>
      router.push({ name: routeNames.controlPanel })
    )
}

async function stageDown() {
  if (!flashcardSet.value || !currFlashcard.value) return
  spaceDeck.value?.willSlideToLeft()
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
  spaceDeck.value?.willSlideToRight()
  const flashcard = copyFlashcard(currFlashcard.value)
  updateFlashcard(flashcard, nextStage(flashcard.stage), currDay.value.chronodate)
  const success = await sendUpdatedFlashcard(flashcardSet.value, flashcard)
  if (success) {
    await getNextAndMarkDays(flashcardSet.value)
  }
}

async function quizAnswer(know: boolean) {
  if (!currFlashcard.value) return
  if (know) {
    quizOverallCorrect.value = quizOverallCorrect.value + 1
    spaceDeck.value?.willSlideToRight()
    await updateQuizSession([currFlashcard.value.id], [])
  } else {
    spaceDeck.value?.willSlideToLeft()
    incorrectFlashcards.value.push(currFlashcard.value)
    await updateQuizSession([currFlashcard.value.id], [currFlashcard.value.id])
  }
  await nextFlashcard()
}

async function startNextQuizRound() {
  if (!flashcardSet.value || !reviewSessionId.value) return
  if (incorrectFlashcards.value.length === 0) {
    Log.error(LogTag.LOGIC, 'Cannot start new round: no incorrect flashcards')
    return
  }

  await sendReviewSessionChildCreateRequest(flashcardSet.value.id, reviewSessionId.value, {
    type: ReviewSessionType.QUIZ,
    chronodayId: currDay.value.id,
  })
    .then((response) => {
      reviewSessionId.value = response.data.id
      quizSessionIdCookie.value = response.data.id

      const newQueue = new MonoStageReviewQueue(incorrectFlashcards.value)
      newQueue.shuffle()

      reviewQueue.value = newQueue
      quizRound.value = quizRound.value + 1
      incorrectFlashcards.value = []
      flashcardsTotal.value = reviewQueue.value.remaining()

      spaceDeck.value?.setDeckReady()

      startWatch()

      return nextFlashcard()
    })
    .catch((error) => {
      Log.error(LogTag.LOGIC, `Failed to create child review session`, error.response?.data)
      toaster.bakeError(userApiErrors.QUIZ_SESSION__NEXT_ROUND_FAILED, error.response?.data)
    })
}

async function prev() {
  if (noPrevAvailable.value) return
  spaceDeck.value?.willSlideToLeft()
  await prevFlashcard()
}

async function next() {
  if (noNextAvailable.value) return
  spaceDeck.value?.willSlideToRight()
  await nextFlashcard()
}

async function moveBack() {
  if (!flashcardSet.value || !currFlashcard.value) {
    Log.error(LogTag.LOGIC, `moveBack is impossible:`,
      `FlashcardSet.id=${flashcardSet.value?.id ?? 'undefined'}`,
      `current Flashcard.id=${currFlashcard.value?.id ?? 'undefined'}`
    )
    return
  }
  spaceDeck.value?.willSlideToLeft()
  const flashcard = copyFlashcard(currFlashcard.value)
  updateFlashcard(flashcard, learningStages.S1, currDay.value.chronodate)
  const success = await sendUpdatedFlashcard(flashcardSet.value, flashcard)
  if (success) {
    await nextFlashcard()
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
  if (await nextFlashcard() && reviewMode.value.isLightspeed()) {
    await markDaysAsInProgress(flashcardSet)
  } else if (reviewMode.value.isLightspeed()) {
    await markDaysAsCompleted(flashcardSet)
  }
}

async function fetchAudio() {
  await fetchFlashcardAudio(
    flashcardSet.value?.id,
    currFlashcard.value?.id,
    flashcardFrontSideAudioBlob,
    flashcardBackSideAudioBlob,
  )
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
  nextFlashcard()
}

function onAudioChanged() {
  fetchAudio()
}

watch(currFlashcard, async (newVal, oldVal) => {
  if (!flashcardSet.value || !reviewSessionId.value) return
  if (oldVal && reviewMode.value.isLightspeed()) {
    reviewedFlashcardIds.value.push(oldVal.id)
    await updateReviewSession([oldVal.id])
  }
  if (!newVal) {
    if (reviewMode.value.isQuiz()) {
      await updateQuizSession(reviewedFlashcardIds.value, incorrectFlashcards.value.map(f => f.id), true)
    } else if (reviewMode.value.isLightspeed()) {
      await updateReviewSession(reviewedFlashcardIds.value, true)
    }
  }
})

async function createReviewSession(quiz: boolean = false) {
  if (!flashcardSet.value) return

  const request: ReviewSessionCreateRequest = {
    type: reviewMode.value.sessionType,
    chronodayId: currDay.value.id,
  }

  if (quiz) {
    request.metadata = {
      currRoundFlashcardIds: reviewQueue.value.remainingFlashcards().map(f => f.id),
      overallTotalCount: reviewQueue.value.remaining(),
    }
  }

  await sendReviewSessionCreateRequest(flashcardSet.value.id, request)
    .then((response) => {
      reviewSessionId.value = response.data.id
      Log.log(LogTag.LOGIC, `Review session ${reviewSessionId.value} created`)
    })
    .catch((error) => {
      Log.error(LogTag.LOGIC, `Failed to create a review session`, error.response?.data)
      toaster.bakeError(userApiErrors.REVIEW_SESSION__CREATION_FAILED, error.response?.data)
    })
}

async function loadQuizSession(sessionId: number) {
  if (!flashcardSet.value) return
  await sendReviewSessionGetRequest(flashcardSet.value.id, sessionId)
    .then((response) => {
      reviewSessionId.value = response.data.id
      elapsedTime.value = response.data.elapsedTime
      quizRound.value = response.data.metadata?.round ?? 1
      quizOverallCorrect.value = response.data.metadata?.overallCorrectCount ?? 0
      quizOverallTotal.value = response.data.metadata?.overallTotalCount ?? 0
      const reviewedFlashcardIdSet = new Set(response.data.flashcardIds ?? [])
      const nextRoundFlashcardIdSet = new Set(response.data.metadata?.nextRoundFlashcardIds ?? [])
      const currRoundFlashcardIdSet = new Set(response.data.metadata?.currRoundFlashcardIds ?? [])
      const currRoundFlashcards = flashcards.value.filter(f => currRoundFlashcardIdSet.has(f.id))
      const flashcardsForReview = currRoundFlashcards.filter(f => !reviewedFlashcardIdSet.has(f.id))
      flashcardsTotal.value = currRoundFlashcardIdSet.size
      reviewedFlashcardIds.value = [...reviewedFlashcardIdSet]
      incorrectFlashcards.value = currRoundFlashcards.filter(f => nextRoundFlashcardIdSet.has(f.id))
      reviewQueue.value = new MonoStageReviewQueue(flashcardsForReview)
      reviewQueue.value.shuffle()
      Log.log(LogTag.LOGIC, `Review session ${reviewSessionId.value} retrieved`)
    })
    .catch((error) => {
      resetState()
      Log.error(LogTag.LOGIC, `Failed to retrieve review session ${sessionId}`, error.response?.data)
      toaster.bakeError(userApiErrors.REVIEW_SESSION__FETCHING_FAILED, error.response?.data)
    })
}

async function updateReviewSession(flashcardIds: number[], finished: boolean = false) {
  if (!flashcardSet.value || !reviewSessionId.value) return
  await sendReviewSessionUpdateRequest(flashcardSet.value.id, reviewSessionId.value, {
    elapsedTime: elapsedTime.value,
    flashcardIds: flashcardIds.map(id => ({ id: id })),
    finished: finished,
  })
    .then(() => {
      Log.log(LogTag.LOGIC, `Review session ${reviewSessionId.value} updated`)
    })
    .catch((error) => {
      Log.error(LogTag.LOGIC, `Failed to updated review session ${reviewSessionId.value}`, error.response?.data)
      toaster.bakeError(userApiErrors.REVIEW_SESSION__UPDATING_FAILED, error.response?.data)
    })
}

async function updateQuizSession(reviewedFlashcardIds: number[], nextRoundFlashcardIds: number[], finished: boolean = false) {
  if (!flashcardSet.value || !reviewSessionId.value) return
  await sendReviewSessionUpdateRequest(flashcardSet.value.id, reviewSessionId.value, {
    elapsedTime: elapsedTime.value,
    flashcardIds: reviewedFlashcardIds.map(id => ({ id: id })),
    finished: finished,
    metadata: {
      nextRoundFlashcardIds: nextRoundFlashcardIds,
      overallCorrectCount: quizOverallCorrect.value,
    },
  })
    .then(() => {
      Log.log(LogTag.LOGIC, `Review session ${reviewSessionId.value} updated`)
    })
    .catch((error) => {
      Log.error(LogTag.LOGIC, `Failed to update review session ${reviewSessionId.value}`, error.response?.data)
      toaster.bakeError(userApiErrors.REVIEW_SESSION__UPDATING_FAILED, error.response?.data)
    })
}

onMounted(async () => {
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
    finishReviewAndLeave()
  } else if (event.key === 'ArrowLeft') {
    event.stopPropagation()
    if (reviewMode.value.isLightspeed()) stageDown()
    if (reviewMode.value.isSpecial()) prev()
    if (reviewMode.value.isQuiz()) quizAnswer(false)
  } else if (event.key === 'ArrowRight') {
    event.stopPropagation()
    if (reviewMode.value.isLightspeed()) stageUp()
    if (reviewMode.value.isSpecial()) next()
    if (reviewMode.value.isQuiz()) quizAnswer(true)
  }
}

</script>

<style scoped>
.review-page--theme {
  --r-page--review-count--color: var(--review-page--review-count--color, rgba(17, 33, 85, 0.92));
  --r-page--review-count--bg: var(--review-page--review-count--border-color, rgb(225, 228, 240));
  --progressbar--from: var(--review-progressbar--from);
  --progressbar--via: var(--review-progressbar--via);
  --progressbar--to: var(--review-progressbar--to);
  --progressbar--bg-color: var(--review-progressbar--bg-color);
}

.review-info {
  display: flex;
  align-items: start;
  justify-content: space-between;
  width: 100%;
}

.review-mode {
  color: rgba(243, 239, 239, 0.7);
  font-size: clamp(0.9rem, 1.8vw, 1.1rem);
  letter-spacing: 0.05rem;
  word-spacing: 0.05rem;
  text-transform: uppercase;
  white-space: nowrap;
}

.review-layout {
  flex: 1;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
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
  width: 100%;
}

.review-count {
  font-size: 1rem;
  font-weight: 600;
  color: var(--r-page--review-count--color);
  background: var(--r-page--review-count--bg);
  padding: 2px;
  margin: 2px;
  border-radius: 6px;
  width: 50px;
  text-align: center;
}

.decision-button {
  --smart-button--width: 130px;
}

</style>
