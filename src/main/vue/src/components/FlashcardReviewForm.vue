<template>
  <div class="review-form">
    <div v-if="!started" class="review-start-container">
      <button class="review-button review-start-button"
              @click="startReview">
        Start review
      </button>
    </div>
    <div v-if="started" class="review-container">
      <div class="review-top">
        <button class="corner-button corner-close-button" @click="finishReview">
          <font-awesome-icon icon="fa-solid fa-xmark"/>
        </button>
      </div>
      <div class="review-body">
        <div class="flashcard-container">
          <div class="flashcard" @click="flipFlashcard">
            <div class="flashcard-top">
              <span v-if="currFlashcard !== undefined" class="corner-text">Level: {{ currFlashcard?.level }}</span>
              <button id="flashcard-edit-button" class="corner-button corner-edit-button"
                      @click.stop="globalStateStore.toggleFlashcardEditModalForm()">
                <font-awesome-icon icon="fa-solid fa-pen-to-square"/>
              </button>
            </div>
            <div class="flashcard-text">
              {{ currFlashcardText }}
            </div>
          </div>
          <div class="flashcard-nav">
            <button class="nav-button nav-left-button" @click="levelDown">Don't know</button>
            <button class="nav-button nav-right-button" @click="levelUp"
                    :class="{ 'nav-button-disabled': editFormWasOpened }"
                    :disabled="editFormWasOpened">
              Know
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>

  <FlashcardModificationModalForm editMode
                                  v-model:visible="globalStateStore.flashcardEditModalFormOpen"
                                  v-model:flashcard="currFlashcard"
                                  v-model:removed="flashcardWasRemoved"/>
</template>

<script setup lang="ts">
// todo behavior if there are no more flashcards to review
// todo confirmation modal form on closing review

import FlashcardModificationModalForm from '@/components/FlashcardModificationModalForm.vue';
import { useFlashcardStateStore } from '@/stores/flashcard-state.ts';
import { computed, onMounted, onUnmounted, type Ref, ref, watch } from 'vue';
import { storeToRefs } from 'pinia';
import { type Flashcard, Level, type ReviewInfo } from '@/models/flashcard.ts';
import { useReviewStateStore } from '@/stores/review-state.ts';
import { useGlobalStateStore } from '@/stores/global-state.ts';

const stateStore = useFlashcardStateStore()
const reviewStateStore = useReviewStateStore()
const { started, isFrontSide, currFlashcard, editFormWasOpened } = storeToRefs(reviewStateStore)

const currFlashcardText = computed(() => {
  if (isFrontSide.value) {
    return currFlashcard.value?.frontSide
  } else {
    return currFlashcard.value?.backSide
  }
})

function startReview() {
  reviewStateStore.startReview()
}

function finishReview() {
  reviewStateStore.finishReview()
}

function flipFlashcard() {
  reviewStateStore.flipFlashcard()
}

function levelDown() {
  if (currFlashcard.value !== undefined) {
    const flashcard = currFlashcard.value
    updateFlashcard(flashcard, Level.FIRST)
    stateStore.updateFlashcard(flashcard)
    reviewStateStore.setEditFormWasOpened(false)
    reviewStateStore.setFrontSide(true)
    reviewStateStore.nextFlashcard()
  }
}

function levelUp() {
  if (currFlashcard.value !== undefined) {
    const flashcard = currFlashcard.value
    updateFlashcard(flashcard, nextLevel(flashcard.level))
    stateStore.updateFlashcard(flashcard)
    reviewStateStore.setFrontSide(true)
    reviewStateStore.nextFlashcard()
  }
}

function updateFlashcard(flashcard: Flashcard, level: Level) {
  console.debug(`Updating flashcard ${flashcard.id} to level ${level}`)

  const now = new Date().toISOString()
  const info: ReviewInfo = {
    level: level,
    reviewedAt: now,
  }

  flashcard.level = level
  flashcard.reviewedAt = now
  flashcard.reviewCount += 1
  flashcard.reviewHistory.push(info)
  flashcard.lastUpdatedAt = now
}

function nextLevel(currLevel: Level): Level {
  if (currLevel === Level.SEVENTH) {
    // todo
    throw new Error('NEXT LEVEL IS NOT IMPLEMENTED YET')
  }
  const levels = Object.values(Level).filter(value => typeof value === 'number') as Level[];
  const currIdx = levels.indexOf(currLevel)
  return levels[currIdx + 1]
}

function handleKeydown(event: KeyboardEvent) {
  if (globalStateStore.isAnyModalOpen()) {
    return
  }

  if (event.key == 'Enter' && !started.value) {
    startReview();
  } else if (event.key === ' ' || ['Space', 'ArrowUp', 'ArrowDown'].includes(event.code)) {
    flipFlashcard();
  } else if (event.key === 'ArrowLeft') {
    levelDown();
  } else if (event.key === 'ArrowRight') {
    levelUp();
  }
}

onMounted(() => {
  document.addEventListener('keydown', handleKeydown);
});

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown);
});

const flashcardWasRemoved = ref(false)

watch(flashcardWasRemoved, (newValue) => {
  if (newValue) {
    reviewStateStore.setEditFormWasOpened(false)
    reviewStateStore.nextFlashcard()
  }
})

const globalStateStore = useGlobalStateStore()

</script>

<style scoped>
.review-form {
  display: flex;
  flex-direction: row;
  height: 100%;
  width: 100%;
  padding: 0;
  margin: 0;
}

.review-start-container {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
}

.review-button {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1em;
  margin-bottom: 8%;
}

.review-start-button {
  font-size: 1.5em;
  background-color: #6db37c;
  color: white;
}

.review-start-button:hover {
  background-color: #519c53;
}

.review-container {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.review-top {
  flex: 1;
  align-items: center;
  text-align: end;
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

.flashcard-top {
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

.flashcard-nav {
  flex: 1;
  display: flex;
  justify-content: space-between;
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

.nav-right-button {
  background-color: #6db37c;
  color: white;
}

.nav-right-button:not(.nav-button-disabled):hover {
  background-color: #519c53;
}

.nav-left-button {
  background-color: #b36d6d;
  color: white;
}

.nav-left-button:not(.nav-button-disabled):hover {
  background-color: #9c5151;
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
  color: #c5c5c5;
}

.corner-button:hover {
  color: #9f9f9f;
}

.corner-close-button {
  font-size: 2em;
}

.corner-edit-button {
  font-size: 1.5em;
}

.corner-text {
  background: none;
  color: #9f9f9f;
  font-size: 1.2em;
  padding: 8px;
}
</style>
