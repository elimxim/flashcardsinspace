<template>
  <div
    :class="[
      'page',
      { 'page--bg--light': !isSpaceMode },
      'flex-column',
      'flex-center',
      'scroll-none',
      'review-page--theme',
    ]"
  >
    <ControlBar :title="flashcardSetName" center-title>
      <template v-if="reviewTopic" #left>
        <div class="review-mode">
          {{ reviewTopic }}
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
        v-if="isSpaceMode"
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
        />
        <div class="review-nav">
          <template v-if="isLightspeedMode">
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
          <template v-if="isSpecialOrSpaceMode">
            <SmartButton
              class="calm-button"
              text="Prev"
              :disabled="noPrevAvailable"
              :on-click="prev"
              auto-blur
              rounded
            />
            <SmartButton
              v-if="isSpaceMode"
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
import { useFlashcardStore } from '@/stores/flashcard-store.ts'
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { storeToRefs } from 'pinia'
import {
  copyFlashcard,
  fetchFlashcardAudio,
  updateFlashcard
} from '@/core-logic/flashcard-logic.ts'
import { nextStage, prevStage, Stage, stages } from '@/core-logic/stage-logic.ts'
import { useChronoStore } from '@/stores/chrono-store.ts'
import {
  createReviewQueue,
  createReviewQueueForStage,
  EmptyReviewQueue,
  ReviewMode,
  ReviewQueue,
  toReviewMode
} from '@/core-logic/review-logic.ts'
import { routeNames } from '@/router'
import { onBeforeRouteLeave, useRouter } from 'vue-router'
import { loadSelectedSetId } from '@/shared/cookies.ts'
import { useToggleStore } from '@/stores/toggle-store.ts'
import { Flashcard, FlashcardSet } from '@/model/flashcard.ts'
import { loadFlashcardRelatedStoresById } from '@/shared/stores.ts'
import { sendFlashcardUpdateRequest } from '@/api/api-client.ts'
import { useSpaceToaster } from '@/stores/toast-store.ts'
import { markDaysAsCompleted, markDaysAsInProgress } from '@/core-logic/chrono-logic.ts'
import Starfield from '@/components/Starfield.vue';

const props = defineProps<{
  stage?: Stage,
}>()

const router = useRouter()
const toaster = useSpaceToaster()
const toggleStore = useToggleStore()
const chronoStore = useChronoStore()
const flashcardStore = useFlashcardStore()

const { flashcardSet, flashcards } = storeToRefs(flashcardStore)
const { chronodays, currDay } = storeToRefs(chronoStore)

const reviewMode = computed(() => toReviewMode(props.stage))
const isLightspeedMode = computed(() => reviewMode.value === ReviewMode.LIGHTSPEED)
const isSpaceMode = computed(() => reviewMode.value === ReviewMode.SPACE)
const isSpecialMode = computed(() => reviewMode.value === ReviewMode.SPECIAL)
const isSpecialOrSpaceMode = computed(() => isSpecialMode.value || isSpaceMode.value)

const spaceDeck = ref<InstanceType<typeof SpaceDeck>>()

const reviewTopic = computed(() => props.stage?.displayName)
const flashcardSetName = computed(() => flashcardSet.value?.name || '')

const reviewQueue = ref<ReviewQueue>(new EmptyReviewQueue())
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

const currFlashcard = ref<Flashcard>()
const flashcardFrontSideAudioBlob = ref<Blob | undefined>()
const flashcardBackSideAudioBlob = ref<Blob | undefined>()

const autoPlayVoice = ref(false)
const autoRepeatVoice = ref(false)

async function prevFlashcard(): Promise<boolean> {
  currFlashcard.value = reviewQueue.value.prev()
  await fetchAudio()
  return currFlashcard.value !== undefined
}

async function nextFlashcard(): Promise<boolean> {
  currFlashcard.value = reviewQueue.value.next()
  await fetchAudio()
  return currFlashcard.value !== undefined
}

function startReview() {
  console.log(`Starting review on stage: ${props.stage?.displayName ?? 'default'}`)
  if (props.stage) {
    reviewQueue.value = createReviewQueueForStage(flashcards.value, props.stage, currDay.value)
  } else {
    reviewQueue.value = createReviewQueue(flashcards.value, currDay.value, chronodays.value)
  }
  flashcardsTotal.value = reviewQueue.value.remaining()
  nextFlashcard()
  console.log(`Flashcards TOTAL: ${flashcardsTotal.value}`)
}

async function finishReview() {
  console.log(`Finishing review on stage: ${props.stage?.displayName ?? 'default'}`)
  reviewQueue.value = new EmptyReviewQueue()
  flashcardsTotal.value = 0
  flashcardFrontSideAudioBlob.value = undefined
  flashcardBackSideAudioBlob.value = undefined
  autoPlayVoice.value = false
  autoRepeatVoice.value = false
  if (flashcardSet.value) {
    if (noNextAvailable.value && isLightspeedMode.value) {
      await markDaysAsCompleted(flashcardSet.value)
    }
  } else {
    console.error(`Can't gracefully finish review: flashcard set is undefined`)
  }
}

async function finishReviewAndLeave() {
  await finishReview()
    .then(() =>
      router.push({ name: routeNames.controlPanel })
    )
}

async function stageDown() {
  if (!flashcardSet.value || !currFlashcard.value) {
    console.error(`stageDown's impossible:`,
      `flashcard set ${flashcardSet.value?.id ?? 'undefined'}`,
      `flashcard ${currFlashcard.value?.id ?? 'undefined'}`
    )
    return
  }
  spaceDeck.value?.willSlideToLeft()
  const flashcard = copyFlashcard(currFlashcard.value)
  updateFlashcard(flashcard, prevStage(flashcard.stage), currDay.value.chronodate)
  const success = await sendUpdatedFlashcard(flashcardSet.value, flashcard)
  if (success) {
    await getNextAndMarkDays(flashcardSet.value)
  }
}

async function stageUp() {
  if (!flashcardSet.value || !currFlashcard.value) {
    console.error(`stageUp's impossible:`,
      `flashcard set ${flashcardSet.value?.id ?? 'undefined'}`,
      `flashcard ${currFlashcard.value?.id ?? 'undefined'}`
    )
    return
  }
  spaceDeck.value?.willSlideToRight()
  const flashcard = copyFlashcard(currFlashcard.value)
  updateFlashcard(flashcard, nextStage(flashcard.stage), currDay.value.chronodate)
  const success = await sendUpdatedFlashcard(flashcardSet.value, flashcard)
  if (success) {
    await getNextAndMarkDays(flashcardSet.value)
  }
}

async function prev() {
  spaceDeck.value?.willSlideToLeft()
  await prevFlashcard()
}

async function next() {
  spaceDeck.value?.willSlideToRight()
  await nextFlashcard()
}

async function moveBack() {
  if (!flashcardSet.value || !currFlashcard.value) {
    console.error(`moveBack's impossible:`,
      `flashcard set ${flashcardSet.value?.id ?? 'undefined'}`,
      `flashcard ${currFlashcard.value?.id ?? 'undefined'}`
    )
    return
  }
  spaceDeck.value?.willSlideToLeft()
  const flashcard = copyFlashcard(currFlashcard.value)
  updateFlashcard(flashcard, stages.S1, currDay.value.chronodate)
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
      console.error(`Failed to update flashcard ${flashcard.id}`, error.response?.data)
      toaster.bakeError(`Couldn't move a flashcard`, error.response?.data)
      return false
    })
}

async function getNextAndMarkDays(flashcardSet: FlashcardSet) {
  if (await nextFlashcard() && isLightspeedMode.value) {
    await markDaysAsInProgress(flashcardSet)
  } else if (isLightspeedMode.value) {
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

function onFlashcardRemoved() {
  nextFlashcard()
}

function onAudioChanged() {
  fetchAudio()
}

onMounted(async () => {
  if (!flashcardStore.loaded) {
    console.log('Flashcard set not loaded, loading...')
    const selectedSetId = loadSelectedSetId()
    if (selectedSetId) {
      await loadFlashcardRelatedStoresById(selectedSetId)
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
    finishReviewAndLeave()
  } else if (event.key === 'ArrowLeft') {
    event.stopPropagation()
    if (isLightspeedMode.value) stageDown()
    if (isSpecialOrSpaceMode.value) prev()
  } else if (event.key === 'ArrowRight') {
    event.stopPropagation()
    if (isLightspeedMode.value) stageUp()
    if (isSpecialOrSpaceMode.value) next()
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
