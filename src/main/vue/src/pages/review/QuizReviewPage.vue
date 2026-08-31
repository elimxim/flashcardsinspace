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
          <font-awesome-icon :icon="reviewIcons.get(ReviewSessionType.QUIZ)!!"/>
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
            :session-type="ReviewSessionType.QUIZ"
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
          <div v-if="UXConfig().showNavButtons" class="review-nav">
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
import QuizResult from '@/components/QuizResult.vue'
import { useFlashcardStore } from '@/stores/flashcard-store.ts'
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { storeToRefs } from 'pinia'
import { Stage } from '@/core-logic/stage-logic.ts'
import { useChronoStore } from '@/stores/chrono-store.ts'
import {
  createReviewQueueForStages,
  MonoStageReviewQueue,
  reviewIcons,
  ReviewSessionType,
} from '@/core-logic/review-logic.ts'
import { routeNames } from '@/router'
import { onBeforeRouteLeave, useRouter } from 'vue-router'
import { loadSelectedSetIdFromCookies } from '@/utils/cookies.ts'
import { useToggleStore } from '@/stores/toggle-store.ts'
import { Flashcard } from '@/model/flashcard.ts'
import { loadStoresForFlashcardSetId } from '@/utils/store-loading.ts'
import {
  sendReviewSessionChildCreateRequest,
} from '@/api/api-client.ts'
import { useSpaceToaster } from '@/stores/toast-store.ts'
import { Log, LogTag } from '@/utils/logger.ts'
import { userApiErrors } from '@/api/user-api-error.ts'
import { destroyReviewStore, useReviewStore } from '@/stores/review-store.ts'
import { useDeferredLoading } from '@/utils/deferred-loading.ts'
import { UXConfig } from '@/utils/device-utils.ts'
import { useRunOnce } from '@/utils/run-once.ts'
import {
  createReviewSessionAttendant,
} from "@/core-logic/review-session-attendant.ts"
import { ReviewSession } from "@/model/review.ts"
import { errorResponseData } from "@/core-logic/media-error.ts"

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

const {
  loadingStarted,
  resolvedLoading,
  startLoading,
  stopLoading,
} = useDeferredLoading()

const sessionRunner = createReviewSessionAttendant(ReviewSessionType.QUIZ, flashcardSet, currDay)

const { elapsedTime } = sessionRunner

const reviewStore = useReviewStore(ReviewSessionType.QUIZ, flashcardSet)

const {
  reviewQueue,
  flashcardsTotal,
  currFlashcard,
  flashcardsRemaining,
  flashcardsSeen,
  progress,
  noNextAvailable,
} = storeToRefs(reviewStore)

const { runOnce: startReviewOnce, isPending: reviewStarting } = useRunOnce(startReview)
const { runOnce: finishReviewOnce } = useRunOnce(finishReview)

const flashcardSetName = computed(() => flashcardSet.value?.name || '')

const spaceDeck = ref<InstanceType<typeof SpaceDeck>>()

const quizRound = ref(1)
const quizOverallTotal = ref(0)
const quizOverallCorrect = ref(0)
const incorrectFlashcards = ref<Flashcard[]>([])

async function startReview() {
  Log.log(LogTag.LOGIC, `Starting review: ${ReviewSessionType.QUIZ}`)
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
    reviewStore.loadState(createReviewQueueForStages(flashcards.value, props.stages, currDay.value))
    quizOverallTotal.value = flashcardsTotal.value

    await sessionRunner.loadOrCreate({
      sessionId: props.sessionId,
      metadata: {
        currRoundFlashcardIds: reviewQueue.value.remainingFlashcards().map(f => f.id),
        overallTotalCount: reviewQueue.value.remaining(),
      },
      onboarding: onboardSession,
    })
    await reviewStore.nextFlashcard()

    Log.log(LogTag.LOGIC, `Flashcards TOTAL: ${flashcardsTotal.value}`)
  } finally {
    await stopLoading()
  }
  spaceDeck.value?.setDeckReady()
}

function resetState() {
  sessionRunner.clear()
  reviewStore.$reset()
  incorrectFlashcards.value = []
  quizOverallTotal.value = 0
  quizOverallCorrect.value = 0
}

async function finishReview() {
  Log.log(LogTag.LOGIC, `Finishing review: ${ReviewSessionType.QUIZ}`)
  if (reviewStarting.value) await startReviewOnce()
  await sessionRunner.flush({ all: true })
  Log.log(LogTag.LOGIC, `Finished review: ${ReviewSessionType.QUIZ}`)
}

async function finishReviewAndLeave() {
  await finishReviewOnce()
  await router.push({ name: routeNames.controlPanel })
}

async function quizAnswer(know: boolean) {
  if (!currFlashcard.value) return

  sessionRunner.track(currFlashcard.value.id)
  if (know) {
    quizOverallCorrect.value = quizOverallCorrect.value + 1
    await sessionRunner.flush({
      metadata: {
        nextRoundFlashcardIds: [],
        overallCorrectCount: quizOverallCorrect.value,
      },
    })
  } else {
    incorrectFlashcards.value.push(currFlashcard.value)
    await sessionRunner.flush({
      metadata: {
        nextRoundFlashcardIds: [currFlashcard.value.id],
        overallCorrectCount: quizOverallCorrect.value,
      },
    })
  }

  await reviewStore.nextFlashcard()
}

async function startNextQuizRound() {
  if (!flashcardSet.value || !sessionRunner.sessionId) return
  if (incorrectFlashcards.value.length === 0) {
    Log.error(LogTag.LOGIC, 'Cannot start new round: no flashcards')
    return
  }

  try {
    const response = await sendReviewSessionChildCreateRequest(flashcardSet.value.id, sessionRunner.sessionId, {
      type: ReviewSessionType.QUIZ,
      chronodayId: currDay.value.id,
    })

    Log.log(LogTag.LOGIC, `Child review session ${response.data.id} created, parent: ${sessionRunner.sessionId}`)
    await router.replace({
      query: {
        ...router.currentRoute.value.query,
        sessionId: response.data.id,
      },
    })
    resetState()
    onboardSession(response.data)
    sessionRunner.init(response.data)
    spaceDeck.value?.setDeckReady()
    return reviewStore.nextFlashcard()
  } catch (error) {
    Log.error(LogTag.LOGIC, `Failed to create child review session`, error)
    toaster.bakeError(userApiErrors.QUIZ_SESSION__NEXT_ROUND_FAILED, errorResponseData(error))
  }
}

function onboardSession(session: ReviewSession) {
  quizRound.value = session.metadata?.round ?? 1
  quizOverallCorrect.value = session.metadata?.overallCorrectCount ?? 0
  quizOverallTotal.value = session.metadata?.overallTotalCount ?? 0

  const reviewedFlashcardIdSet = new Set(session.flashcardIds ?? [])
  const nextRoundFlashcardIdSet = new Set(session.metadata?.nextRoundFlashcardIds ?? [])
  const currRoundFlashcardIdSet = new Set(session.metadata?.currRoundFlashcardIds ?? [])
  const currRoundFlashcards = flashcards.value.filter(f => currRoundFlashcardIdSet.has(f.id))
  const flashcardsForReview = currRoundFlashcards.filter(f => !reviewedFlashcardIdSet.has(f.id))

  incorrectFlashcards.value = currRoundFlashcards.filter(f => nextRoundFlashcardIdSet.has(f.id))

  reviewStore.loadState(new MonoStageReviewQueue(flashcardsForReview))
  reviewStore.setFlashcardsTotal(currRoundFlashcards.length)

  Log.log(LogTag.LOGIC, `Quiz session ${session.id} onboarded`)
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
  resetState()
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
