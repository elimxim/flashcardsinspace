<template>
  <div
    class="flashcard"
    :class="{
      'flashcard--none': reviewFinished,
      'flashcard--flipped': !isFrontSide && !reviewFinished,
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
            <font-awesome-icon icon="fa-solid fa-pen-to-square"/>
          </button>
        </div>
        <div class="flashcard__body">
          {{ currFlashcard?.frontSide }}
        </div>
        <div class="flashcard__strip">
          <span>
            <font-awesome-icon icon="fa-regular fa-eye"/>
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
            <font-awesome-icon icon="fa-solid fa-pen-to-square"/>
          </button>
        </div>
        <div class="flashcard__body">
          {{ currFlashcard?.backSide }}
        </div>
        <div class="flashcard__strip">
          <span>
            <font-awesome-icon icon="fa-regular fa-eye"/>
            {{ viewedTimes }}
          </span>
        </div>
      </div>
    </div>
  </div>

</template>

<script setup lang="ts">
import { storeToRefs } from 'pinia'
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useReviewStore } from '@/stores/review-store.ts'
import { useGlobalStore } from '@/stores/global-store.ts'

const globalStore = useGlobalStore()
const reviewStore = useReviewStore()

const {
  isFrontSide,
  reviewFinished,
  currFlashcard,
} = storeToRefs(reviewStore)

const flashcardButton = ref<HTMLDivElement>()

const viewedTimes = computed(() => {
  return (currFlashcard.value?.reviewCount ?? 0) + 1
})

function flipFlashcard() {
  reviewStore.flipFlashcard()
}

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
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  cursor: pointer;
  will-change: transform;
  transform-style: preserve-3d;
}

.flashcard--none {
  background: none;
  cursor: default;
  perspective: none;
}

.flashcard--flipped .flashcard__flipper {
  transform: rotateY(180deg);
}

.flashcard__flipper {
  flex: 1;
  transition: transform 0.5s cubic-bezier(0.25, 1, 0.5, 1);
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
  overflow-wrap: break-word;
  box-shadow: 0 8px 24px hsla(0, 0%, 0%, 0.15);
}

.flashcard__front {
  background-color: #fdfdfd;
  background-image: linear-gradient(0deg, rgba(34, 34, 34, 0.15) 1px, transparent 1px);
  background-size: 100% 28px;
}

.flashcard__back {
  background-color: #fdfdfd;
  transform: rotateY(180deg);
}

.flashcard__strip {
  height: fit-content;
  font-size: clamp(1rem, 2vw, 1.2rem);
  color: #9f9f9f;
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
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
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
  color: #686868;
}
</style>
