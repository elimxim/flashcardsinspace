<template>
  <div class="app-container">
    <div class="menu">
      <FlashcardMenu/>
    </div>

    <div class="flashcards-review">
      <FlashcardReviewForm/>
    </div>
  </div>
</template>

<script setup lang="ts">
import FlashcardMenu from '@/components/FlashcardMenu.vue'
import FlashcardReviewForm from '@/components/FlashcardReviewForm.vue';
import { onBeforeRouteLeave } from 'vue-router';
import { useReviewStateStore } from '@/stores/review-state.ts';

const reviewStateStore = useReviewStateStore()

onBeforeRouteLeave((to, from, next) => {
  reviewStateStore.finishReview()
  next()
})
</script>

<style scoped>
.app-container {
  display: flex;
  flex-direction: row;
}

.menu {
  flex: 1;
  background-color: #f0f0f0;
}

.flashcards-review {
  flex: 10;
  background: none;
}

</style>
