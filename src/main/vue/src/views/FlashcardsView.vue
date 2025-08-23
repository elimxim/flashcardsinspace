<template>
  <div class="flashcards-container">
    <div class="menu-area">
      <MainMenu/>
    </div>

    <div class="main-area" v-if="!reviewStarted">
      <ReviewFormStarter/>
    </div>

    <div class="main-area" v-if="reviewStarted">
      <ReviewForm/>
    </div>
  </div>
</template>

<script setup lang="ts">
import MainMenu from '@/components/MainMenu.vue'
import ReviewFormStarter from '@/components/ReviewFormStarter.vue'
import ReviewForm from '@/components/ReviewForm.vue'
import { onBeforeRouteLeave } from 'vue-router'
import { useReviewStore } from '@/stores/review-store.ts'
import { storeToRefs } from 'pinia'
import { useFlashcardDataStore } from '@/stores/flashcard-data-store.ts'
import { useChronoStore } from '@/stores/chrono-store.ts'
import { useFlashcardSetStore } from '@/stores/flashcard-set-store.ts'

const flashcardDataStore = useFlashcardDataStore()
const flashcardSetStore = useFlashcardSetStore()
const chronoStore = useChronoStore()
const reviewStore = useReviewStore()

const { started: reviewStarted } = storeToRefs(reviewStore)
const { firstFlashcardSet } = storeToRefs(flashcardDataStore)
const { flashcardSet } = storeToRefs(flashcardSetStore)

flashcardDataStore.loadData().then(async () => {
  console.log('flashcard data loaded')
  if (firstFlashcardSet.value !== null) {
    await flashcardSetStore.loadData(firstFlashcardSet.value)
  } else {
    flashcardSetStore.resetState()
  }
}).then(async () => {
  console.log('flashcard set initialized')
  if (flashcardSet.value !== null) {
    await chronoStore.loadData(flashcardSet.value)
    console.log('chrono data loaded')
  } else {
    console.log('flashcard set hasn\'t been initialized')
  }
})

onBeforeRouteLeave(() => {
  reviewStore.finishReview()
})

</script>

<style scoped>
.flashcards-container {
  display: flex;
  flex-direction: row;
  min-height: 92vh;
}

.menu-area {
  flex: 1;
  background-color: #f0f0f0;
}

.main-area {
  flex: 10;
  background: none;
}

</style>
