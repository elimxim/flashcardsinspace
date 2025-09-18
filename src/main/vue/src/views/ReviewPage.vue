<template>
  <div class="page-center-container">
    <div class="top-row">
      <span class="corner-text">
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
      <button class="corner-button"
              ref="escapeButton"
              @click="finishReview">
        <font-awesome-icon icon="fa-solid fa-xmark"/>
      </button>
    </div>
    <div class="review-body">
      <div class="flashcard-container">
        <div class="flashcard"
             :class="{ 'flashcard-no-background': reviewFinished }"
             ref="flashcardButton"
             @click="flipFlashcard">
          <div class="top-row">
            <span class="corner-text" style="flex: 100">
              {{ currFlashcard?.stage }}
            </span>
            <button class="corner-button"
                    ref="flashcardEditButton"
                    @click.stop="globalStore.toggleFlashcardEditModalForm()"
                    :disabled="reviewFinished"
                    :hidden="reviewFinished">
              <font-awesome-icon icon="fa-solid fa-pen-to-square"/>
            </button>
          </div>
          <div class="flashcard-text"
               :class="{ 'flashcard-text-color-dark': !reviewFinished, 'flashcard-text-color-light': reviewFinished }">
            {{ currFlashcardText }}
          </div>
        </div>
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
  </div>


  <FlashcardModificationModalForm
    editMode
    v-model:visible="flashcardEditModalFormOpen"
    v-model:flashcard="currFlashcard"
    v-model:removed="flashcardWasRemoved"/>
</template>

<script setup lang="ts">
import Progressbar from '@/components/Progressbar.vue'
import FlashcardModificationModalForm from '@/components/modal/FlashcardModificationModalForm.vue'
import { useFlashcardSetStore } from '@/stores/flashcard-set-store.ts'
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { useReviewStore } from '@/stores/review-store.ts'
import { useGlobalStore } from '@/stores/global-store.ts'
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

const globalStore = useGlobalStore()
const chronoStore = useChronoStore()
const reviewStore = useReviewStore()
const flashcardSetStore = useFlashcardSetStore()

const router = useRouter()
const { flashcardSet } = storeToRefs(flashcardSetStore)
const { flashcardEditModalFormOpen } = storeToRefs(globalStore)
const {
  settings,
  isFrontSide,
  currFlashcard,
  editFormWasOpened,
  reviewFinished,
  flashcardsRunningTotal,
} = storeToRefs(reviewStore)

const flashcardsTotal = ref(0);
const progress = computed(() =>
  Math.max(0, Math.min(1, (flashcardsTotal.value - flashcardsRunningTotal.value) / flashcardsTotal.value))
)

const escapeButton = ref<HTMLButtonElement>()
const flashcardButton = ref<HTMLDivElement>()
const stageDownButton = ref<HTMLButtonElement>()
const stageUpButton = ref<HTMLButtonElement>()
const prevButton = ref<HTMLButtonElement>()
const nextButton = ref<HTMLButtonElement>()
const moveBackButton = ref<HTMLButtonElement>()
const flashcardEditButton = ref<HTMLButtonElement>()

const flashcardWasRemoved = ref(false)

const currFlashcardText = computed(() => {
  if (reviewFinished.value) {
    return 'No more cards for review'
  } else if (isFrontSide.value) {
    return currFlashcard.value?.frontSide
  } else {
    return currFlashcard.value?.backSide
  }
})

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

function flipFlashcard() {
  reviewStore.flipFlashcard()
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

watch(flashcardWasRemoved, (newValue) => {
  if (newValue) {
    reviewStore.setEditFormWasOpened(false)
    reviewStore.setFrontSide(true)
    reviewStore.nextFlashcard()
    flashcardWasRemoved.value = false
  }
})

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
.review-body {
  flex: 100;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.flashcard-container {
  flex: 10;
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 20px;
  min-width: 600px;
  max-width: 600px;
  min-height: 400px;
  max-height: 400px;
  margin-bottom: 5%;
}

.flashcard {
  flex: 100;
  display: flex;
  flex-direction: column;
  background-color: #f0f0f0;
  border-radius: 8px;
  overflow: hidden;
  overflow-wrap: break-word;
  user-select: none;
  cursor: pointer;
}

.flashcard-no-background {
  background: none;
}

.top-row {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.flashcard-text {
  flex: 100;
  font-size: 1.8em;
  text-align: center;
  align-content: center;
}

.flashcard-text-color-dark {
  color: #686868;
}

.flashcard-text-color-light {
  color: #aeaeae;
}

.flashcard-nav {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.flashcard-nav-single {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
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

.corner-button {
  flex: 1;
  border: none;
  outline: none;
  background: none;
  cursor: pointer;
  font-size: 1.5em;
  color: #c5c5c5;
}

.corner-button:hover {
  color: #9f9f9f;
}

.flashcard-progressbar {
  flex: 98;
  --progressbar--from: var(--review-progressbar--from);
  --progressbar--via: var(--review-progressbar--via);
  --progressbar--to: var(--review-progressbar--to);
  --progressbar--bg-color: var(--review-progressbar--bg-color);
}

.corner-text {
  flex: 1;
  background: none;
  color: #9f9f9f;
  font-size: 1.2em;
  padding: 8px;
}
</style>
