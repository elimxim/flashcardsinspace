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
</template>

<script setup lang="ts">
import Progressbar from '@/components/Progressbar.vue'
import SpaceDeck from '@/components/SpaceDeck.vue'
import SmartButton from '@/components/SmartButton.vue'
import AwesomeButton from '@/components/AwesomeButton.vue'
import { useFlashcardSetStore } from '@/stores/flashcard-set-store.ts'
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { updateFlashcard } from '@/core-logic/flashcard-logic.ts'
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
import { useModalStore } from '@/stores/modal-store.ts'
import { Flashcard } from '@/model/flashcard.ts'

const props = defineProps<{
  stage?: Stage,
}>()

const router = useRouter()
const modalStore = useModalStore()
const chronoStore = useChronoStore()
const flashcardSetStore = useFlashcardSetStore()

const {
  flashcardSet,
  flashcards,
} = storeToRefs(flashcardSetStore)
const { flashcardEditOpen } = storeToRefs(modalStore)

const spaceDeck = ref<InstanceType<typeof SpaceDeck>>()
const escapeButton = ref<InstanceType<typeof AwesomeButton>>()
const stageDownButton = ref<InstanceType<typeof SmartButton>>()
const stageUpButton = ref<InstanceType<typeof SmartButton>>()
const prevButton = ref<InstanceType<typeof SmartButton>>()
const nextButton = ref<InstanceType<typeof SmartButton>>()

const noPrevAvailable = computed(() => {
  console.log('noPrevAvailable', flashcardsRemaining.value, flashcardsTotal.value)
  return flashcardsTotal.value === flashcardsRemaining.value
})
const noNextAvailable = computed(() => currFlashcard.value === undefined)
const reviewTopic = computed(() => props.stage?.displayName ?? 'Lightspeed')
const reviewMode = computed(() => toReviewMode(props.stage))
const reviewQueue = ref<ReviewQueue>(new EmptyReviewQueue())
const flashcardsTotal = ref(0)
const flashcardsRemaining = computed(() => reviewQueue.value.remaining())
const progress = ref(0)
const editFormWasOpened = ref(false)
const currFlashcard = ref<Flashcard>()

function prevFlashcard(): boolean {
  currFlashcard.value = reviewQueue.value.prev()
  return currFlashcard.value !== undefined
}

function nextFlashcard(): boolean {
  currFlashcard.value = reviewQueue.value.next()
  return currFlashcard.value !== undefined
}

function calcProgress() {
  const total = flashcardsTotal.value
  const remaining = flashcardsRemaining.value + 1
  const completionRate = (total - remaining) / total
  progress.value = Math.max(0, Math.min(1, completionRate))
}

function startReview() {
  if (props.stage) {
    console.log('Creating review queue for stage', props.stage)
    reviewQueue.value = createReviewQueueForStage(flashcards.value, props.stage)
  } else {
    reviewQueue.value = createReviewQueue(flashcards.value)
  }
  flashcardsTotal.value = reviewQueue.value.remaining()
  nextFlashcard()
}

function finishReview() {
  reviewQueue.value = new EmptyReviewQueue()
  flashcardsTotal.value = 0
  if (noNextAvailable.value
    && reviewMode.value === ReviewMode.LIGHTSPEED
    && flashcardSet.value !== null
  ) {
    chronoStore.markLastDaysAsCompleted(flashcardSet.value)
  }
}

function finishReviewAndGoToFlashcards() {
  finishReview()
  router.push({ name: routeNames.flashcards })
}

async function stageDown() {
  if (flashcardSet.value && currFlashcard.value) {
    const flashcard = currFlashcard.value
    updateFlashcard(flashcard, prevStage(flashcard.stage))
    flashcardSetStore.updateFlashcard(flashcard)
    if (reviewMode.value === ReviewMode.LIGHTSPEED) {
      await chronoStore.markLastDaysAsInProgress(flashcardSet.value)
    }
    editFormWasOpened.value = false
    spaceDeck.value?.willSlideToLeft()
    if (!nextFlashcard()) {
      progress.value = 1
      if (reviewMode.value === ReviewMode.LIGHTSPEED) {
        await chronoStore.markLastDaysAsCompleted(flashcardSet.value)
      }
    } else {
      calcProgress()
    }
  }
}

async function stageUp() {
  if (flashcardSet.value && currFlashcard.value) {
    const flashcard = currFlashcard.value
    updateFlashcard(flashcard, nextStage(flashcard.stage))
    flashcardSetStore.updateFlashcard(flashcard)
    if (reviewMode.value === ReviewMode.LIGHTSPEED) {
      await chronoStore.markLastDaysAsInProgress(flashcardSet.value)
    }
    editFormWasOpened.value = false
    spaceDeck.value?.willSlideToRight()
    if (!nextFlashcard()) {
      progress.value = 1
      if (reviewMode.value === ReviewMode.LIGHTSPEED) {
        await chronoStore.markLastDaysAsCompleted(flashcardSet.value)
      }
    } else {
      calcProgress()
    }
  }
}

async function prev() {
  spaceDeck.value?.willSlideToLeft()
  if (prevFlashcard()) {
    calcProgress()
  } else {
    progress.value = 1
  }
}

async function next() {
  spaceDeck.value?.willSlideToRight()
  if (nextFlashcard()) {
    calcProgress()
  } else {
    progress.value = 1
  }
}

async function moveBack() {
  if (currFlashcard.value) {
    const flashcard = currFlashcard.value
    updateFlashcard(flashcard, stages.S1)
    flashcardSetStore.updateFlashcard(flashcard)
    spaceDeck.value?.willSlideToLeft()
    if (nextFlashcard()) {
      calcProgress()
    } else {
      progress.value = 1
    }
  }
}

function onFlashcardRemoved() {
  editFormWasOpened.value = false
  if (nextFlashcard()) {
    calcProgress()
  } else {
    progress.value = 1
  }
}

async function loadReviewState() {
  console.log('Loading review state...')
  if (!flashcardSetStore.loaded) {
    console.log('flashcardSetStore.loaded:', flashcardSetStore.loaded)
    const selectedSetId = loadSelectedSetId()
    if (selectedSetId) {
      console.log('Loading flashcard set', selectedSetId, '...')
      await flashcardSetStore.loadFlashcardSet(selectedSetId)
        .then(async () => {
          console.log('Loading flashcards for set', selectedSetId, '...')
          await flashcardSetStore.loadFlashcards()
        })
        .then(async () => {
          if (!chronoStore.loaded) {
            console.log('chronoStore.loaded:', chronoStore.loaded)
            if (flashcardSet.value !== null) {
              console.log('Loading chronodays...')
              await chronoStore.loadChronodays(flashcardSet.value)
            } else {
              console.log('Flashcard set not found')
            }
          }
        })
    }
  }
  console.log('Starting review...')
  console.log('flashcardSetStore.flashcards:', flashcardSetStore.flashcards.length)
  //reviewStore.startReview(flashcardSetStore.flashcards, props.stage)
  startReview()
  calcProgress()
}

watch(flashcardEditOpen, (newVal) => {
  if (newVal) {
    editFormWasOpened.value = newVal
  }
})

onMounted(async () => {
  await loadReviewState()
  spaceDeck.value?.setDeckReady()
  console.log('Started review',
    props.stage ? `on stage: ${props.stage.displayName}` : 'on default stage',
    'flashcards TOTAL:', flashcardsTotal.value,
  )
  document.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  console.log('Finishing review',
    props.stage ? `on stage: ${props.stage}` : 'on default stage',
  )
  finishReview()
  document.removeEventListener('keydown', handleKeydown)
})

onBeforeRouteLeave(() => {
  console.log('Finishing review...',
    props.stage ? ` on stage: ${props.stage}` : '',
  )
  finishReview()
})

function handleKeydown(event: KeyboardEvent) {
  if (modalStore.isAnyModalOpen()) return

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
