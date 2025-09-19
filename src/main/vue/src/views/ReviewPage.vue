<template>
  <div
    :class="[
      'page',
      'page--x-centered',
      'page--min-padded',
    ]"
  >
    <div class="top-row">
      <span class="flashcard__corner-text">
        {{ settings.topic }}
      </span>
      <span class="flashcard-progressbar">
        <Progressbar
          :progress="progress"
          height="0.5rem"
          trackRounded
          barRounded
        />
      </span>
      <button class="flashcard__edit-button"
              ref="escapeButton"
              @click="finishReview">
        <font-awesome-icon icon="fa-solid fa-xmark"/>
      </button>
    </div>
    <div class="review-body">
      <Flashcard/>
      <div class="flashcard-nav" v-if="settings.mode === ReviewMode.LIGHTSPEED">
        <button class="nav-button nav-red-button"
                :disabled="reviewFinished"
                :hidden="reviewFinished"
                ref="stageDownButton"
                @click="stageDown">
          Don't know
        </button>
        <button class="nav-button nav-green-button"
                :class="{ 'nav-button-disabled': editFormWasOpened }"
                :disabled="editFormWasOpened || reviewFinished"
                :hidden="reviewFinished"
                ref="stageUpButton"
                @click="stageUp">
          Know
        </button>
      </div>
      <div class="flashcard-nav" v-if="settings.mode === ReviewMode.SPECIAL">
        <button class="nav-button nav-blue-button"
                :hidden="reviewFinished"
                ref="prevButton"
                @click="prev">
          Prev
        </button>
        <button class="nav-button nav-blue-button"
                :disabled="reviewFinished"
                :hidden="reviewFinished"
                ref="nextButton"
                @click="next">
          Next
        </button>
      </div>
      <div class="flashcard-nav" v-if="settings.mode === ReviewMode.SPACE">
        <button class="nav-button nav-blue-button"
                :hidden="reviewFinished"
                ref="prevButton"
                @click="prev">
          Prev
        </button>
        <button class="nav-button nav-black-button"
                :disabled="reviewFinished"
                :hidden="reviewFinished"
                ref="moveBackButton"
                @click="moveBack">
          Move back
        </button>
        <button class="nav-button nav-blue-button"
                :disabled="reviewFinished"
                :hidden="reviewFinished"
                ref="nextButton"
                @click="next">
          Next
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import Progressbar from '@/components/Progressbar.vue'
import Flashcard from '@/components/Flashcard.vue'
import { useFlashcardSetStore } from '@/stores/flashcard-set-store.ts'
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { storeToRefs } from 'pinia'
import { useReviewStore } from '@/stores/review-store.ts'
import { updateFlashcard } from '@/core-logic/flashcard-logic.ts'
import { nextStage, prevStage, Stage, stages } from '@/core-logic/stage-logic.ts'
import { useChronoStore } from '@/stores/chrono-store.ts'
import { ReviewMode } from '@/core-logic/review-logic.ts'
import { routeNames } from '@/router'
import { onBeforeRouteLeave, useRouter } from 'vue-router'
import { loadSelectedSetId } from '@/cookies/cookies.ts'

const props = defineProps<{
  stage?: Stage,
}>()

const chronoStore = useChronoStore()
const reviewStore = useReviewStore()
const flashcardSetStore = useFlashcardSetStore()

const router = useRouter()
const { flashcardSet } = storeToRefs(flashcardSetStore)
const {
  settings,
  currFlashcard,
  editFormWasOpened,
  reviewFinished,
  flashcardsRunningTotal,
} = storeToRefs(reviewStore)

const flashcardsTotal = ref(0);
const progress = computed(() => {
  const total = flashcardsTotal.value
  const running = flashcardsRunningTotal.value
  if (!total || total <= 0) return 0
  const going = (total - running) / total
  const safeGoing = Number.isFinite(going) ? going : 0
  return Math.max(0, Math.min(1, safeGoing))
})

const escapeButton = ref<HTMLButtonElement>()
const stageDownButton = ref<HTMLButtonElement>()
const stageUpButton = ref<HTMLButtonElement>()
const prevButton = ref<HTMLButtonElement>()
const nextButton = ref<HTMLButtonElement>()
const moveBackButton = ref<HTMLButtonElement>()
const flashcardButton = ref<HTMLDivElement>()
const flashcardEditButton = ref<HTMLButtonElement>()

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
    reviewStore.setFrontSide(true)
    if (!reviewStore.nextFlashcard() && settings.value.mode === ReviewMode.LIGHTSPEED) {
      await chronoStore.markLastDaysAsCompleted(flashcardSet.value)
    }
  }
  stageDownButton.value?.blur()
}

async function stageUp() {
  if (flashcardSet.value !== null && currFlashcard.value !== null) {
    const flashcard = currFlashcard.value
    updateFlashcard(flashcard, nextStage(flashcard.stage))
    flashcardSetStore.updateFlashcard(flashcard)
    if (settings.value.mode === ReviewMode.LIGHTSPEED) {
      await chronoStore.markLastDaysAsInProgress(flashcardSet.value)
    }
    reviewStore.setFrontSide(true)
    if (!reviewStore.nextFlashcard() && settings.value.mode === ReviewMode.LIGHTSPEED) {
      await chronoStore.markLastDaysAsCompleted(flashcardSet.value)
    }
  }
  stageUpButton.value?.blur()
}

function prev() {
  reviewStore.setFrontSide(true)
  reviewStore.prevFlashcard()
  prevButton.value?.blur()
}

function next() {
  reviewStore.setFrontSide(true)
  reviewStore.nextFlashcard()
  nextButton.value?.blur()
}

function moveBack() {
  if (currFlashcard.value !== null) {
    const flashcard = currFlashcard.value
    updateFlashcard(flashcard, stages.S1)
    flashcardSetStore.updateFlashcard(flashcard)
    reviewStore.setFrontSide(true)
    reviewStore.nextFlashcard()
  }
  moveBackButton.value?.blur()
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
    flashcardsTotal.value = flashcardsRunningTotal.value;
  } else {
    reviewStore.startReview([], props.stage)
    flashcardsTotal.value = 0
  }
}

onMounted(async () => {
  await loadReviewState()
  console.log('Started review',
    props.stage ? `on stage: ${props.stage}` : 'on default stage',
    ', flashcards TOTAL: ', flashcardsTotal.value,
  )
  document.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  console.log('Finishing review...',
    props.stage ? ` on stage: ${props.stage}` : '',
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
  if (event.key === 'Escape') {
    event.stopPropagation()
    escapeButton.value?.click()
  } else if (event.key === ' ' || ['Space', 'ArrowUp', 'ArrowDown'].includes(event.code)) {
    event.stopPropagation()
    flashcardButton.value?.click()
  } else if (event.key === 'ArrowLeft') {
    event.stopPropagation()
    stageDownButton.value?.click()
    prevButton.value?.click()
  } else if (event.key === 'ArrowRight') {
    event.stopPropagation()
    stageUpButton.value?.click()
    nextButton.value?.click()
  } else if (event.key === 'e' || event.key === 'E') {
    event.stopPropagation()
    flashcardEditButton.value?.click()
  }
}

</script>

<style scoped>
.top-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  gap: 10px;
}

.review-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: fit-content;
  gap: 20px;
  overflow: hidden;
}

.flashcard-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  height: 2rem;
  gap: 20px;
}

.nav-button {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1em;
  width: 120px;
  user-select: none;
}

.nav-green-button {
  background-color: #4CAF50;
  color: white;
}

.nav-green-button:not(.nav-button-disabled):hover {
  background-color: #45a049;
}

.nav-red-button {
  background-color: #f44336;
  color: white;
}

.nav-red-button:not(.nav-button-disabled):hover {
  background-color: #da4437;
}

.nav-blue-button {
  background-color: #2196F3;
  color: white;
}

.nav-blue-button:not(.nav-button-disabled):hover {
  background-color: #1e88e5;
}

.nav-black-button {
  background-color: #505050;
  color: white;
}

.nav-black-button:not(.nav-button-disabled):hover {
  background-color: #333333;
}

.nav-button-disabled {
  background-color: #ededed;
  color: #cacaca;
  cursor: default;
}

.flashcard__edit-button {
  border: none;
  outline: none;
  background: none;
  cursor: pointer;
  font-size: 1.5em;
  color: #c5c5c5;
}

.flashcard__edit-button:hover {
  color: #9f9f9f;
}

.flashcard-progressbar {
  flex: 1;
  --progressbar--from: var(--review-progressbar--from);
  --progressbar--via: var(--review-progressbar--via);
  --progressbar--to: var(--review-progressbar--to);
  --progressbar--bg-color: var(--review-progressbar--bg-color);
}

.flashcard__corner-text {
  background: none;
  color: #9f9f9f;
  font-size: 1.2em;
}
</style>
