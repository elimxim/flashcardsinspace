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
        {{ settings.topic }}
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
        :on-click="finishReview"
      />
    </div>
    <div class="review-body">
      <SpaceDeck
        v-if="loaded"
        ref="spaceDeck"
        v-model:flashcard="currFlashcard"
        :on-flashcard-removed="onFlashcardRemoved"
      />
      <div class="review-nav">
        <SmartButton
          v-if="settings.mode === ReviewMode.LIGHTSPEED"
          ref="stageDownButton"
          text="Don't know"
          class="review-button remove-button"
          :disabled="reviewFinished"
          :hidden="reviewFinished"
          :on-click="stageDown"
          auto-blur
          rounded
        />
        <SmartButton
          v-if="settings.mode === ReviewMode.LIGHTSPEED"
          ref="stageUpButton"
          text="Know"
          class="review-button create-button"
          :disabled="editFormWasOpened || reviewFinished"
          :hidden="reviewFinished"
          :on-click="stageUp"
          auto-blur
          rounded
        />
        <SmartButton
          v-if="settings.mode === ReviewMode.SPECIAL"
          ref="prevButton"
          class="review-button update-button"
          text="Prev"
          :on-click="prev"
          auto-blur
          rounded
        />
        <SmartButton
          v-if="settings.mode === ReviewMode.SPECIAL"
          ref="nextButton"
          class="review-button update-button"
          text="Next"
          :disabled="reviewFinished"
          :hidden="reviewFinished"
          :on-click="next"
          auto-blur
          rounded
        />
        <SmartButton
          v-if="settings.mode === ReviewMode.SPACE"
          ref="prevButton"
          class="review-button update-button"
          text="Prev"
          :on-click="prev"
          auto-blur
          rounded
        />
        <SmartButton
          v-if="settings.mode === ReviewMode.SPACE"
          class="review-button move-back-button"
          text="Move back"
          :disabled="reviewFinished"
          :hidden="reviewFinished"
          :on-click="moveBack"
          :hold-time="1.2"
          auto-blur
          rounded
        />
        <SmartButton
          v-if="settings.mode === ReviewMode.SPACE"
          ref="nextButton"
          class="review-button update-button"
          text="Next"
          :disabled="reviewFinished"
          :hidden="reviewFinished"
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
import { onMounted, onUnmounted, ref } from 'vue'
import { storeToRefs } from 'pinia'
import { useReviewStore } from '@/stores/review-store.ts'
import { updateFlashcard } from '@/core-logic/flashcard-logic.ts'
import { nextStage, prevStage, Stage, stages } from '@/core-logic/stage-logic.ts'
import { useChronoStore } from '@/stores/chrono-store.ts'
import { ReviewMode } from '@/core-logic/review-logic.ts'
import { routeNames } from '@/router'
import { onBeforeRouteLeave, useRouter } from 'vue-router'
import { loadSelectedSetId } from '@/cookies/cookies.ts'
import { useModalStore } from '@/stores/modal-store.ts'

const props = defineProps<{
  stage?: Stage,
}>()

const router = useRouter()
const modalStore = useModalStore()
const chronoStore = useChronoStore()
const reviewStore = useReviewStore()
const flashcardSetStore = useFlashcardSetStore()

const { flashcardSet } = storeToRefs(flashcardSetStore)
const {
  loaded,
  settings,
  currFlashcard,
  editFormWasOpened,
  reviewFinished,
  remainingFlashcards,
} = storeToRefs(reviewStore)

const flashcardsTotal = ref(0)
const progress = ref(0)

const spaceDeck = ref<InstanceType<typeof SpaceDeck>>()
const escapeButton = ref<InstanceType<typeof AwesomeButton>>()
const stageDownButton = ref<InstanceType<typeof SmartButton>>()
const stageUpButton = ref<InstanceType<typeof SmartButton>>()
const prevButton = ref<InstanceType<typeof SmartButton>>()
const nextButton = ref<InstanceType<typeof SmartButton>>()

function calcProgress() {
  const total = flashcardsTotal.value
  const remaining = remainingFlashcards.value
  const completionRate = (total - remaining) / total
  progress.value = Math.max(0, Math.min(1, completionRate))
}

function finishReview() {
  flashcardsTotal.value = 0
  if (reviewFinished.value
    && settings.value.mode === ReviewMode.LIGHTSPEED
    && flashcardSet.value !== null
  ) {
    chronoStore.markLastDaysAsCompleted(flashcardSet.value)
  }
  reviewStore.finishReview()
  router.push({ name: routeNames.flashcards })
}

async function stageDown() {
  if (flashcardSet.value !== null && currFlashcard.value !== null) {
    const flashcard = currFlashcard.value
    updateFlashcard(flashcard, prevStage(flashcard.stage))
    flashcardSetStore.updateFlashcard(flashcard)
    if (settings.value.mode === ReviewMode.LIGHTSPEED) {
      await chronoStore.markLastDaysAsInProgress(flashcardSet.value)
    }
    reviewStore.setEditFormWasOpened(false)
    spaceDeck.value?.willSlideToLeft()
    if (!reviewStore.nextFlashcard()) {
      progress.value = 1
      if (settings.value.mode === ReviewMode.LIGHTSPEED) {
        await chronoStore.markLastDaysAsCompleted(flashcardSet.value)
      }
    } else {
      calcProgress()
    }
  }
}

async function stageUp() {
  if (flashcardSet.value !== null && currFlashcard.value !== null) {
    const flashcard = currFlashcard.value
    updateFlashcard(flashcard, nextStage(flashcard.stage))
    flashcardSetStore.updateFlashcard(flashcard)
    if (settings.value.mode === ReviewMode.LIGHTSPEED) {
      await chronoStore.markLastDaysAsInProgress(flashcardSet.value)
    }
    reviewStore.setEditFormWasOpened(false)
    spaceDeck.value?.willSlideToRight()
    if (!reviewStore.nextFlashcard()) {
      progress.value = 1
      if (settings.value.mode === ReviewMode.LIGHTSPEED) {
        await chronoStore.markLastDaysAsCompleted(flashcardSet.value)
      }
    } else {
      calcProgress()
    }
  }
}

async function prev() {
  spaceDeck.value?.willSlideToLeft()
  if (reviewStore.prevFlashcard()) {
    calcProgress()
  } else {
    progress.value = 1
  }
}

async function next() {
  spaceDeck.value?.willSlideToRight()
  if (reviewStore.nextFlashcard()) {
    calcProgress()
  } else {
    progress.value = 1
  }
}

async function moveBack() {
  if (currFlashcard.value !== null) {
    const flashcard = currFlashcard.value
    updateFlashcard(flashcard, stages.S1)
    flashcardSetStore.updateFlashcard(flashcard)
    spaceDeck.value?.willSlideToLeft()
    if (reviewStore.nextFlashcard()) {
      calcProgress()
    } else {
      progress.value = 1
    }
  }
}

function onFlashcardRemoved() {
  reviewStore.setEditFormWasOpened(false)
  if (reviewStore.nextFlashcard()) {
    calcProgress()
  } else {
    progress.value = 1
  }
}

async function loadReviewState() {
  console.log('Loading review state...')
  if (!reviewStore.loaded) {
    console.log('reviewStore.loaded:', reviewStore.loaded)
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
    reviewStore.startReview(flashcardSetStore.flashcards, props.stage)
    flashcardsTotal.value = remainingFlashcards.value;
    calcProgress()
  } else {
    reviewStore.startReview([], props.stage)
    flashcardsTotal.value = 0
    calcProgress()
  }
}

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
  flashcardsTotal.value = 0
  reviewStore.finishReview()
  document.removeEventListener('keydown', handleKeydown)
})

onBeforeRouteLeave(() => {
  console.log('Finishing review...',
    props.stage ? ` on stage: ${props.stage}` : '',
  )
  flashcardsTotal.value = 0
  reviewStore.finishReview()
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
