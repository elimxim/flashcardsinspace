<template>
  <div v-if="!isInitialDay" class="review-info-widget review-info-widget--theme">
    <div class="review-info-title">
      To review
    </div>
    <div class="review-list">
      <div
        v-for="review in currDayStageReviews"
        :key="review.stage"
        class="review-item"
      >
        <div class="review-item-label">
          {{ review.stage }}
        </div>
        <div class="review-item-count">
          {{ review.count }}
        </div>
      </div>
      <div class="review-item review-item--total">
        <div class="review-item-label">
          Total
        </div>
        <div class="review-item-count">
          {{ currDayReviewTotal }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useFlashcardStore } from '@/stores/flashcard-store.ts'
import { useChronoStore } from '@/stores/chrono-store.ts'
import { storeToRefs } from 'pinia'
import { computed } from 'vue'
import { calcStageReviews, StageReview } from '@/core-logic/review-logic.ts'

const flashcardStore = useFlashcardStore()
const chronoStore = useChronoStore()

const { flashcards } = storeToRefs(flashcardStore)
const { currDay, isInitialDay } = storeToRefs(chronoStore)

const currDayStageReviews = computed<StageReview[]>(() => {
  if (!currDay.value) return []
  return calcStageReviews(flashcards.value, currDay.value.stages)
})

const currDayReviewTotal = computed(() =>
  currDayStageReviews.value.reduce((acc, v) => acc + v.count, 0)
)
</script>

<style scoped>
.review-info-widget--theme {
  --r-widget--bg: var(--review-info-widget--bg, linear-gradient(135deg, rgba(102, 126, 234, 0.66) 0%, rgba(118, 75, 162, 0.68) 100%));
  --r-widget--border-color: var(--review-info-widget--border-color, rgba(128, 128, 128, 0.62));
  --r-widget--title--color: var(--review-info-widget--title--color, rgba(13, 18, 74, 0.6));
  --r-widget--label--color: var(--review-info-widget--label--color, rgba(13, 18, 74, 0.6));
  --r-widget--number--color: var(--review-info-widget--number--color, rgba(20, 27, 106, 0.82));
  --r-widget--number--bg: var(--review-info-widget--number--bg, rgba(255, 255, 255, 0.6));
}

.review-info-widget {
  position: relative;
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
  background: var(--r-widget--bg);
  border: 1px solid var(--r-widget--border-color);
  border-radius: 6px;
  padding: 4px;
}

.review-info-title {
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--r-widget--title--color);
  letter-spacing: 0.05rem;
  word-spacing: 0.05rem;
  text-transform: uppercase;
  text-align: center;
  white-space: nowrap;
  padding: 2px;
  flex-shrink: 0;
}

.review-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 1;
  min-height: 0;
  padding: 6px;
}

.review-item {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.review-item--total {
  font-weight: 600;
}

.review-item-label {
  font-size: 0.9rem;
  word-spacing: 0.05rem;
  letter-spacing: 0.05rem;
  text-transform: uppercase;
  white-space: nowrap;
  color: var(--r-widget--label--color);
  flex: 1;
  text-align: left;
}

.review-item-count {
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--r-widget--number--color);
  background: var(--r-widget--number--bg);
  border-radius: 4px;
  padding: 2px;
  width: 40px;
  text-align: center;
}
</style>
