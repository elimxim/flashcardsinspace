<template>
  <div class="review-info-widget">
    <div class="cp-text cp-text--nowrap">
      {{ title }}
    </div>
    <div class="review-list">
      <div
        v-for="rs in reviewStages"
        :key="rs.stage"
        class="review-item"
      >
        <div class="cp-text cp-text--light cp-text--left cp-text--nowrap">
          {{ rs.stage }}
        </div>
        <div class="cp-count-box">
          {{ rs.count }}
        </div>
      </div>
      <div class="review-item review-item--total">
        <div class="cp-text cp-text--left cp-text--nowrap">
          Total
        </div>
        <div class="cp-count-box">
          {{ reviewTotal }}
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
import { calcStageReviews, ReviewStage } from '@/core-logic/review-logic.ts'
import { chronodayStatuses, nextChronoday } from '@/core-logic/chrono-logic.ts'

const flashcardStore = useFlashcardStore()
const chronoStore = useChronoStore()

const { flashcards } = storeToRefs(flashcardStore)
const { currDay, chronodays } = storeToRefs(chronoStore)

const shouldShowTomorrow = computed(() =>
  currDay.value.status === chronodayStatuses.COMPLETED || currDay.value.status === chronodayStatuses.OFF
)

const reviewDay = computed(() => {
  if (shouldShowTomorrow.value) {
    return nextChronoday(chronodays.value, currDay.value)
  }
  return currDay.value
})

const title = computed(() =>
  shouldShowTomorrow.value ? 'Tomorrow' : 'Today'
)

const reviewStages = computed<ReviewStage[]>(() => {
  if (!reviewDay.value) return []
  return calcStageReviews(flashcards.value, reviewDay.value)
})

const reviewTotal = computed(() =>
  reviewStages.value.reduce((acc, v) => acc + v.count, 0)
)
</script>

<style scoped>
.review-info-widget {
  position: relative;
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
  background: var(--cp--widget--color);
  border: 1px solid var(--cp--border-color);
  border-radius: 6px;
  padding: 4px;
  gap: 4px;
}

.review-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 1;
  min-height: 0;
  padding: 2px 6px 6px 6px;
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

</style>
