<template>
  <div
    class="flashcard"
    :class="{
      'flashcard--none': reviewFinished,
      'flashcard-flipped': !isFrontSide && !reviewFinished,
    }"
    ref="flashcardButton"
    @click="flipFlashcard"
  >
    <div v-if="reviewFinished" class="flashcard__body">
      {{ 'No more cards for review' }}
    </div>
    <div v-else class="flashcard__flipper">
      <div class="flashcard__face flashcard__front">
        <div class="flashcard__strip">
          <span class="flashcard__corner-text">
            {{ currFlashcard?.stage }}
          </span>
          <button
            class="flashcard__edit-button"
            @click.stop="globalStore.toggleFlashcardEditModalForm()"
          >
            <font-awesome-icon icon="fa-solid fa-pen-to-square" />
          </button>
        </div>
        <div class="flashcard__body">
          {{ currFlashcard?.frontSide }}
        </div>
        <div class="flashcard__strip">
          <span>
            <font-awesome-icon icon="fa-regular fa-eye" />
            {{ viewedTimes }}
          </span>
        </div>
      </div>
      <div class="flashcard__face flashcard__back">
        <div class="flashcard__strip">
          <span class="flashcard__corner-text">
            {{ currFlashcard?.stage }}
          </span>
          <button
            class="flashcard__edit-button"
            @click.stop="globalStore.toggleFlashcardEditModalForm()"
          >
            <font-awesome-icon icon="fa-solid fa-pen-to-square" />
          </button>
        </div>
        <div class="flashcard__body">
          {{ currFlashcard?.backSide }}
        </div>
        <div class="flashcard__strip">
          <span>
            <font-awesome-icon icon="fa-regular fa-eye" />
            {{ viewedTimes }}
          </span>
        </div>
      </div>
    </div>
  </div>

  <FlashcardModificationModalForm
    editMode
    v-model:visible="flashcardEditModalFormOpen"
    v-model:flashcard="currFlashcard"
    v-model:removed="flashcardWasRemoved"
  />
</template>

<script setup lang="ts">
import FlashcardModificationModalForm from '@/components/modal/FlashcardModificationModalForm.vue'
import { storeToRefs } from 'pinia'
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useReviewStore } from '@/stores/review-store.ts'
import { useGlobalStore } from '@/stores/global-store.ts'

const globalStore = useGlobalStore()
const reviewStore = useReviewStore()

const { flashcardEditModalFormOpen } = storeToRefs(globalStore)
const {
  isFrontSide,
  reviewFinished,
  currFlashcard,
} = storeToRefs(reviewStore)

const flashcardWasRemoved = ref(false)
const flashcardButton = ref<HTMLDivElement>()

const viewedTimes = computed(() => {
  return (currFlashcard.value?.reviewCount ?? 0) + 1
})

function flipFlashcard() {
  reviewStore.flipFlashcard()
}

watch(flashcardWasRemoved, (newValue) => {
  if (newValue) {
    reviewStore.setEditFormWasOpened(false)
    reviewStore.setFrontSide(true)
    reviewStore.nextFlashcard()
    flashcardWasRemoved.value = false
  }
})

onMounted(() => {
  document.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown)
})

function handleKeydown(event: KeyboardEvent) {
  if (event.key === ' ' || ['Space', 'ArrowUp', 'ArrowDown'].includes(event.code)) {
    event.stopPropagation()
    flashcardButton.value?.click()
  }
}

</script>

<style scoped>
.flashcard {
  position: relative;
  width: clamp(200px, 90vw, 600px);
  height: clamp(250px, 50vh, 450px);
  cursor: pointer;
  display: flex;
  flex-direction: column;
  perspective: 1000px;
  background-color: transparent;
}

.flashcard-flipped .flashcard__flipper {
  transform: rotateY(180deg);
}

.flashcard__flipper {
  flex: 1;
  transition: transform 0.6s ease-in-out;
  transform-style: preserve-3d;
  position: relative;
  will-change: transform;
}

.flashcard__face {
  position: absolute;
  width: 100%;
  height: 100%;
  padding: 0.4rem;
  backface-visibility: hidden;
  display: flex;
  flex-direction: column;
  border-radius: 24px;
  overflow: hidden;
  overflow-wrap: break-word;
}

.flashcard__front {
  background-color: #f0f0f0;
}

.flashcard__back {
  background-color: #dddddd;
  transform: rotateY(180deg);
}

.flashcard--none {
  background: none;
  cursor: default;
  perspective: none;
}

.flashcard__strip {
  height: fit-content;
  font-size: clamp(1rem, 2vw, 1.2rem);
  color: #cdcdcd;
  display: flex;
  justify-content: space-between;
  padding-left: 4px;
  padding-right: 4px;
  gap: 10px;
}

.flashcard__body {
  flex: 1;
  width: 100%;
  height: 100%;
  font-size: clamp(1.2rem, 2vw, 1.8rem);
  color: #686868;
  text-align: center;
  display: flex;
  align-items: center;
  justify-content: center;
}

.flashcard__corner-text {
  background: none;
  font-size: inherit;
  color: inherit;
}

.flashcard__edit-button {
  border: none;
  outline: none;
  background: none;
  cursor: pointer;
  font-size: inherit;
  color: inherit;
}

.flashcard__edit-button:hover {
  color: #9f9f9f;
}
</style>
