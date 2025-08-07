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
import { useTimelineStore } from '@/stores/timeline-store.ts'
import { useFlashcardSetStore } from '@/stores/flashcard-set-store.ts'

const flashcardDataStore = useFlashcardDataStore()
const flashcardSetStore = useFlashcardSetStore()
const timelineStore = useTimelineStore()
const reviewStore = useReviewStore()

const { started: reviewStarted } = storeToRefs(reviewStore)
const { flashcardSets } = storeToRefs(flashcardDataStore)
const { flashcardSet } = storeToRefs(flashcardSetStore)

flashcardDataStore.loadData().then(() => {
  flashcardSetStore.loadDataOrResetState(flashcardSets.value).then(() => {
    if (flashcardSet.value !== null) {
      timelineStore.loadData(flashcardSet.value)
    }
  })
})

onBeforeRouteLeave((to, from, next) => {
  reviewStore.finishReview()
  next()
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
