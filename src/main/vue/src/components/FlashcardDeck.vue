<template>
  <div class="flashcard-wrapper">
    <div class="flashcard-deck">
      <transition :name="flashcardTransition">
        <Flashcard :key="currFlashcard ? currFlashcard.id : 0"/>
      </transition>
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
import Flashcard from '@/components/Flashcard.vue'
import FlashcardModificationModalForm from '@/components/modal/FlashcardModificationModalForm.vue'
import { ref, watch } from 'vue'
import { useReviewStore } from '@/stores/review-store.ts'
import { storeToRefs } from 'pinia'
import { useGlobalStore } from '@/stores/global-store.ts';

const globalStore = useGlobalStore()
const reviewStore = useReviewStore()

const { flashcardEditModalFormOpen } = storeToRefs(globalStore)
const {
  currFlashcard,
  reviewFinished,
} = storeToRefs(reviewStore)

const deckReady = ref(false)
const flashcardWasRemoved = ref(false)
const flashcardTransition = ref('slide-next')

function toggleDeckReady() {
  console.log(`Deck ready from ${deckReady.value} to ${!deckReady.value}`)
  deckReady.value = !deckReady.value
}

function preparePrev() {
  if (!deckReady.value || reviewFinished.value) {
    flashcardTransition.value = ''
  } else {
    flashcardTransition.value = 'slide-prev'
  }
}

function prepareNext() {
  if (!deckReady.value || reviewFinished.value) {
    flashcardTransition.value = ''
  } else {
    flashcardTransition.value = 'slide-next'
  }
}

defineExpose({
  toggleDeckReady,
  preparePrev,
  prepareNext
})

watch(flashcardWasRemoved, (newValue) => {
  if (newValue) {
    reviewStore.setEditFormWasOpened(false)
    reviewStore.setFrontSide(true)
    reviewStore.nextFlashcard()
    flashcardWasRemoved.value = false
  }
})

</script>

<style scoped>
.flashcard-wrapper {
  display: grid;
  place-items: center;

}

.flashcard-deck {
  grid-area: 1 / 1;
  position: relative;
  width: clamp(200px, 90vw, 600px);
  height: clamp(250px, 50vh, 450px);
  background-color: transparent;
}

/* sliding animation> */

.slide-next-enter-active,
.slide-next-leave-active,
.slide-prev-enter-active,
.slide-prev-leave-active {
  transition: all 0.6s cubic-bezier(0.25, 1, 0.5, 1);
  position: absolute;
}

.slide-next-leave-active,
.slide-prev-leave-active {
  z-index: 1;
}

.slide-next-enter-from,
.slide-prev-enter-from {
  transform: scale(0.90);
}

.slide-next-leave-to {
  transform: translateX(200%) rotate(10deg);
}

.slide-prev-leave-to {
  transform: translateX(-200%) rotate(-10deg);
}

/* <sliding animation */

</style>
