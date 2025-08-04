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
import { useReviewStore } from '@/stores/review.ts'
import { storeToRefs } from 'pinia'
import { useFlashcardDataStore } from '@/stores/flashcard-data.ts'
import { useCalendarStore } from '@/stores/calendar.ts'
import { useFlashcardSetStore } from '@/stores/flashcard-set.ts'

const flashcardDataStore = useFlashcardDataStore()
const flashcardSetStore = useFlashcardSetStore()
const calendarStore = useCalendarStore()
const reviewStore = useReviewStore()

const { started: reviewStarted } = storeToRefs(reviewStore)
const { flashcardSets } = storeToRefs(flashcardDataStore)

flashcardDataStore.loadData().then(() => {
  flashcardSetStore.initFromList(flashcardSets.value)
})
calendarStore.loadData()

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
