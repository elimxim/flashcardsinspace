<template>
  <div class="flashcard-wrapper">
    <div v-if="!reviewFinished" class="flashcard-deck">
      <transition :name="flashcardTransition">
        <Flashcard
          ref="flashcard"
          :key="currFlashcard ? currFlashcard.id : 0"
          :stage="currFlashcard?.stage"
          :frontSide="currFlashcard?.frontSide"
          :backSide="currFlashcard?.backSide"
          :viewedTimes="viewedTimes"
          :editFormWasOpened="editFormWasOpened"
          :onEdit="globalStore.toggleFlashcardEditModalForm"
        />
      </transition>
    </div>
    <div v-else class="flashcard-deck">
      <Flashcard
        frontSide="No more flashcards for review"
        unflippable
        textOnly
        transparent
      />
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
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { useReviewStore } from '@/stores/review-store.ts'
import { useGlobalStore } from '@/stores/global-store.ts'

const globalStore = useGlobalStore()
const reviewStore = useReviewStore()

const { flashcardEditModalFormOpen } = storeToRefs(globalStore)
const {
  currFlashcard,
  reviewFinished,
  editFormWasOpened,
} = storeToRefs(reviewStore)

const deckReady = ref(false)
const flashcard = ref<InstanceType<typeof Flashcard>>()
const flashcardWasRemoved = ref(false)
const flashcardTransition = ref('slide-next')

const viewedTimes = computed(() => {
  return (currFlashcard.value?.reviewCount ?? 0) + 1
})

function toggleDeckReady() {
  console.log(`FlashcardDeck.ready: ${deckReady.value} => ${!deckReady.value}`)
  deckReady.value = !deckReady.value
}

async function preparePrev() {
  await flashcard.value?.flipToFrontAndWait()
  if (!deckReady.value || reviewFinished.value) {
    flashcardTransition.value = ''
  } else {
    flashcardTransition.value = 'slide-prev'
  }
}

async function prepareNext() {
  await flashcard.value?.flipToFrontAndWait()
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
    flashcard.value?.flipToFront()
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
  if (globalStore.isAnyModalFormOpen()) return

  if (event.key === ' ' || ['Space', 'ArrowUp', 'ArrowDown'].includes(event.code)) {
    event.stopPropagation()
    flashcard.value?.flip()
  }
}

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
