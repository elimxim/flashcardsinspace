<template>
  <div
    class="flashcard"
    :class="{
      'flashcard--bg-none': reviewFinished,
      'flashcard--bg-front': isFrontSide,
      'flashcard--bg-back': !isFrontSide,
    }"
    ref="flashcardButton"
    @click="flipFlashcard"
  >
    <div v-if="!reviewFinished" class="flashcard__top">
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
      {{ currFlashcardText }}
    </div>
    <div v-if="!reviewFinished" class="flashcard__bottom">
      <span>
        <font-awesome-icon icon="fa-regular fa-eye"/>
        {{ viewedTimes }}
      </span>
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

const currFlashcardText = computed(() => {
  if (reviewFinished.value) {
    return 'No more cards for review'
  } else if (isFrontSide.value) {
    return currFlashcard.value?.frontSide
  } else {
    return currFlashcard.value?.backSide
  }
})

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
  border-radius: 16px;
  overflow: hidden;
  overflow-wrap: break-word;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  padding: 0.5rem;
}

.flashcard--bg-front {
  background-color: #f0f0f0;
}

.flashcard--bg-back {
  background-color: #dddddd;
}

.flashcard--bg-none {
  background: none;
  cursor: default;
}

.flashcard__top {
  height: clamp(1.5rem, 4vh, 2rem);
  font-size: clamp(01rem, 4vw, 1.2rem);
  color: #cdcdcd;
  display: flex;
  align-items: center;
  justify-content: space-between;
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

.flashcard__bottom {
  height: clamp(1.5rem, 4vh, 2rem);
  font-size: clamp(01rem, 4vw, 1.2rem);
  color: #9f9f9f;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
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
