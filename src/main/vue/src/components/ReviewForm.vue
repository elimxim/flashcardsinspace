<template>
  <div class="main-area" v-if="started">
    <div class="top-row">
        <span class="corner-text">
          {{ settings.topic }}
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
             :class="{ 'flashcard-no-background': reviewStateStore.isNoCardsForReview() }"
             ref="flashcardButton"
             @click="flipFlashcard">
          <div class="top-row">
              <span class="corner-text"
                    :hidden="reviewStateStore.isNoCardsForReview()">
                {{ currFlashcard?.level }}
              </span>
            <button id="flashcard-edit-button"
                    class="corner-button"
                    ref="flashcardEditButton"
                    @click.stop="globalStateStore.toggleFlashcardEditModalForm()"
                    :disabled="reviewStateStore.isNoCardsForReview()"
                    :hidden="reviewStateStore.isNoCardsForReview()">
              <font-awesome-icon icon="fa-solid fa-pen-to-square"/>
            </button>
          </div>
          <div class="flashcard-text"
               :class="{ 'flashcard-text-color-dark': !reviewStateStore.isNoCardsForReview(), 'flashcard-text-color-light': reviewStateStore.isNoCardsForReview() }">
            {{ currFlashcardText }}
          </div>
        </div>
        <div class="flashcard-nav" v-if="settings.mode === ReviewMode.LEITNER">
          <button class="nav-button nav-red-button"
                  :disabled="reviewStateStore.isNoCardsForReview()"
                  :hidden="reviewStateStore.isNoCardsForReview()"
                  ref="levelDownButton"
                  @click="levelDown">
            Don't know
          </button>
          <button class="nav-button nav-green-button"
                  :class="{ 'nav-button-disabled': editFormWasOpened }"
                  :disabled="editFormWasOpened || reviewStateStore.isNoCardsForReview()"
                  :hidden="reviewStateStore.isNoCardsForReview()"
                  ref="levelUpButton"
                  @click="levelUp">
            Know
          </button>
        </div>
        <div class="flashcard-nav-single" v-if="settings.mode === ReviewMode.SPECIAL">
          <button class="nav-button nav-blue-button"
                  :disabled="reviewStateStore.isNoCardsForReview()"
                  :hidden="reviewStateStore.isNoCardsForReview()"
                  ref="nextButton"
                  @click="next">
            Next
          </button>
        </div>
        <div class="flashcard-nav" v-if="settings.mode === ReviewMode.SPACE">
          <button class="nav-button nav-black-button"
                  :disabled="reviewStateStore.isNoCardsForReview()"
                  :hidden="reviewStateStore.isNoCardsForReview()"
                  ref="moveBackButton"
                  @click="moveBack">
            Move back
          </button>
          <button class="nav-button nav-blue-button"
                  :disabled="reviewStateStore.isNoCardsForReview()"
                  :hidden="reviewStateStore.isNoCardsForReview()"
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
import FlashcardModificationModalForm from '@/components/modal/FlashcardModificationModalForm.vue'
import { useFlashcardStateStore } from '@/stores/flashcard-state.ts'
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { useReviewStateStore } from '@/stores/review-state.ts'
import { useGlobalStateStore } from '@/stores/global-state.ts'
import { updateFlashcard } from '@/core-logic/flashcard-logic.ts'
import { ReviewMode } from '@/models/store.ts'
import { getLevel, levels, nextLevel, prevLevel } from '@/core-logic/level-logic.ts'

const flashcardStateStore = useFlashcardStateStore()
const reviewStateStore = useReviewStateStore()
const globalStateStore = useGlobalStateStore()

const { flashcardEditModalFormOpen } = storeToRefs(globalStateStore)
const {
  settings,
  started,
  isFrontSide,
  currFlashcard,
  editFormWasOpened
} = storeToRefs(reviewStateStore)

const currFlashcardText = computed(() => {
  if (reviewStateStore.isNoCardsForReview()) {
    return 'No more cards for review'
  } else if (isFrontSide.value) {
    return currFlashcard.value?.frontSide
  } else {
    return currFlashcard.value?.backSide
  }
})

const escapeButton = ref<HTMLButtonElement>()
const flashcardButton = ref<HTMLDivElement>()
const levelDownButton = ref<HTMLButtonElement>()
const levelUpButton = ref<HTMLButtonElement>()
const nextButton = ref<HTMLButtonElement>()
const moveBackButton = ref<HTMLButtonElement>()
const flashcardEditButton = ref<HTMLButtonElement>()

function finishReview() {
  reviewStateStore.finishReview()
}

function flipFlashcard() {
  reviewStateStore.flipFlashcard()
}

function levelDown() {
  if (currFlashcard.value !== null) {
    const flashcard = currFlashcard.value
    updateFlashcard(flashcard, prevLevel(flashcard.level))
    flashcardStateStore.updateFlashcard(flashcard)
    reviewStateStore.setEditFormWasOpened(false)
    reviewStateStore.setFrontSide(true)
    reviewStateStore.nextFlashcard()
  }
  levelDownButton.value?.blur()
}

function levelUp() {
  if (currFlashcard.value !== null) {
    const flashcard = currFlashcard.value
    updateFlashcard(flashcard, nextLevel(flashcard.level))
    flashcardStateStore.updateFlashcard(flashcard)
    reviewStateStore.setFrontSide(true)
    reviewStateStore.nextFlashcard()
  }
  levelUpButton.value?.blur()
}

function next() {
  if (currFlashcard.value !== null) {
    const flashcard = currFlashcard.value
    updateFlashcard(flashcard, getLevel(flashcard.level))
    flashcardStateStore.updateFlashcard(flashcard)
    reviewStateStore.setFrontSide(true)
    reviewStateStore.nextFlashcard()
  }
  nextButton.value?.blur()
}

function moveBack() {
  if (currFlashcard.value !== null) {
    const flashcard = currFlashcard.value
    updateFlashcard(flashcard, levels.ATTEMPTED)
    flashcardStateStore.updateFlashcard(flashcard)
    reviewStateStore.setFrontSide(true)
    reviewStateStore.nextFlashcard()
  }
  moveBackButton.value?.blur()
}

const flashcardWasRemoved = ref(false)

watch(flashcardWasRemoved, (newValue) => {
  if (newValue) {
    reviewStateStore.setEditFormWasOpened(false)
    reviewStateStore.nextFlashcard()
  }
})

onMounted(() => {
  document.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown)
})

function handleKeydown(event: KeyboardEvent) {
  if (globalStateStore.isAnyModalFormOpen()) {
    return
  }

  if (event.key == 'Escape') {
    escapeButton.value?.click()
  } else if (event.key === ' ' || ['Space', 'ArrowUp', 'ArrowDown'].includes(event.code)) {
    flashcardButton.value?.click()
  } else if (event.key === 'ArrowLeft') {
    levelDownButton.value?.click()
  } else if (event.key === 'ArrowRight') {
    levelUpButton.value?.click()
    nextButton.value?.click()
  } else if (event.shiftKey && (event.key === 'e' || event.key === 'E')) {
    flashcardEditButton.value?.click()
  }
}

</script>

<style scoped>
.main-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  height: 100%;
  width: 100%;
  padding: 0;
  margin: 0;
}

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

.corner-text {
  background: none;
  color: #9f9f9f;
  font-size: 1.2em;
  padding: 8px;
}
</style>
