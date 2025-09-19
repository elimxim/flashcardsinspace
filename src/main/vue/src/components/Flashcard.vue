<template>
  <div
    class="flashcard"
    :class="{ 'flashcard-no-background': reviewFinished }"
    ref="flashcardButton"
    @click="flipFlashcard"
  >
    <div class="top-row">
      <span class="corner-text" style="flex: 100">
        {{ currFlashcard?.stage }}
      </span>
      <button
        class="corner-button"
        ref="flashcardEditButton"
        @click.stop="globalStore.toggleFlashcardEditModalForm()"
        :disabled="reviewFinished"
        :hidden="reviewFinished"
      >
        <font-awesome-icon icon="fa-solid fa-pen-to-square"/>
      </button>
    </div>
    <div
      class="flashcard-text"
      :class="{
        'flashcard-text-color-dark': !reviewFinished,
        'flashcard-text-color-light': reviewFinished
      }"
    >
      {{ currFlashcardText }}
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

const currFlashcardText = computed(() => {
  if (reviewFinished.value) {
    return 'No more cards for review'
  } else if (isFrontSide.value) {
    return currFlashcard.value?.frontSide
  } else {
    return currFlashcard.value?.backSide
  }
})

const flashcardWasRemoved = ref(false)

const flashcardButton = ref<HTMLDivElement>()
const flashcardEditButton = ref<HTMLButtonElement>()

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
  if (event.key === 'e' || event.key === 'E') {
    event.stopPropagation()
    flashcardEditButton.value?.click()
  }
}

</script>

<style scoped>
.flashcard {
  position: relative;
  width: clamp(200px, 90vw, 600px);
  height: clamp(250px, 50vh, 450px);
  background-color: #f0f0f0;
  border-radius: 8px;
  overflow: hidden;
  overflow-wrap: break-word;
  cursor: pointer;
}

.flashcard-no-background {
  background: none;
}

.top-row {
  flex: 1;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}


.corner-text {
  background: none;
  color: #9f9f9f;
  font-size: 1.2em;
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
</style>
