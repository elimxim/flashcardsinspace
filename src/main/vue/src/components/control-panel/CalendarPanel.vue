<template>
  <div class="calendar-panel calendar-panel--theme">
    <div class="calendar-panel-label">
      Current day
    </div>
    <div class="calendar-panel-layout">
      <div class="calendar-left-area">
        <AwesomeButton
          icon="fa-solid fa-calendar-days"
          class="calendar-panel-button"
          :disabled="!flashcardSet"
          :on-click="modalStore.toggleCalendar"
        />
        <div class="calendar-day">
          <div class="calendar-day__text">
            Day
          </div>
          <div v-if="isOnVacation" class="calendar-day__vacation">
            🌴
          </div>
          <div v-else class="calendar-day__number">
            {{ dayNumber }}
          </div>
        </div>
      </div>
      <div class="calendar-right-area">
        <div class="calendar-stages-header"></div>
        <div class="calendar-review-numbers-header">To review</div>
        <div class="calendar-stages-wrapper">
          <div
            v-for="review in stageReviews"
            :key="review.stage"
            class="calendar-stage"
          >
            {{ review.stage }}
          </div>
          <div class="calendar-stage">
            Total
          </div>
        </div>
        <div class="calendar-review-numbers-wrapper">
          <div
            v-for="review in stageReviews"
            :key="review.stage"
            class="calendar-review-number"
          >
            {{ review.count }}
          </div>
          <div class="calendar-review-number">
            {{ reviewTotal }}
          </div>
        </div>
      </div>
    </div>
  </div>
  <CalendarModal/>
</template>

<script setup lang="ts">
import AwesomeButton from '@/components/AwesomeButton.vue'
import CalendarModal from '@/views/modal/CalendarModal.vue'
import { computed } from 'vue'
import { flashcardSetStatuses } from '@/core-logic/flashcard-logic.ts'
import { chronodayStatuses } from '@/core-logic/chrono-logic.ts'
import { calcStageReviews, StageReview } from '@/core-logic/review-logic.ts'
import { useFlashcardStore } from '@/stores/flashcard-store.ts'
import { useChronoStore } from '@/stores/chrono-store.ts'
import { useModalStore } from '@/stores/modal-store.ts'
import { storeToRefs } from 'pinia'

const flashcardStore = useFlashcardStore()
const chronoStore = useChronoStore()
const modalStore = useModalStore()

const { flashcardSet, flashcards } = storeToRefs(flashcardStore)
const { currDay } = storeToRefs(chronoStore)

const isOnVacation = computed(() =>
  flashcardSet.value?.status === flashcardSetStatuses.SUSPENDED || currDay.value?.status === chronodayStatuses.OFF
)
const dayNumber = computed(() => isOnVacation.value ? '🌴' : currDay.value?.seqNumber ?? '?')

const stageReviews = computed<StageReview[]>(() => {
  if (!currDay.value) return []
  return calcStageReviews(flashcards.value, currDay.value)
})
const reviewTotal = computed<number>(() => stageReviews.value.reduce((acc, v) => acc + v.count, 0))
</script>

<style scoped>
.calendar-panel--theme {
  --panel--label-color: var(--calendar-panel--label-color, #555555);
  --panel--text-color: var(--calendar-panel--text-color, rgba(57, 57, 57, 0.92));
  --panel--number-color: var(--calendar-panel--number-color, rgba(17, 33, 85, 0.92));
  --panel--border-color: var(--calendar-panel--border-color, rgba(128, 128, 128, 0.62));
  --panel--review--header-color: var(--calendar-panel--review--header-color, rgba(43, 69, 142, 0.88));
  --panel--review--bg: var(--calendar-panel--review--bg, rgba(88, 114, 209, 0.13));
}

.calendar-panel {
  display: flex;
  flex-direction: column;
  padding: 1px;
}

.calendar-panel-label {
  font-size: clamp(0.5rem, 1.8vw, 0.6rem);
  color: var(--panel--label-color);
  letter-spacing: 0.1rem;
  word-spacing: 0.1rem;
  text-transform: uppercase;
  padding: 0 0 0 6px;
}

.calendar-panel-layout {
  display: flex;
  flex-direction: row;
  padding: 2px 6px 4px 4px;
  gap: 10px;
  border: 1px solid var(--panel--border-color);
  border-radius: 6px;
}

.calendar-left-area {
  display: grid;
  grid-template-rows: 1fr auto;
  flex-direction: column;
  justify-content: space-between;
  gap: 6px;
  margin: 4px;
}

.calendar-day {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  height: 20px;
}

.calendar-day__text {
  font-size: clamp(0.9rem, 1.8vw, 1rem);
  color: var(--panel--text-color);
  white-space: nowrap;
}

.calendar-day__number {
  font-size: clamp(0.85rem, 1.8vw, 0.9rem);
  font-weight: 600;
  border: 1px solid var(--panel--border-color);
  color: var(--panel--number-color);
  border-radius: 3px;
  padding: 2px;
  width: 40px;
  text-align: center;
}

.calendar-day__vacation {
  font-size: clamp(0.85rem, 1.8vw, 0.9rem);
  width: 40px;
  text-align: center;
}

.calendar-right-area {
  display: grid;
  grid-template-columns: auto auto;
  grid-template-rows: auto repeat(3, 1fr);
  gap: 1px 10px;
  align-items: center;
}

.calendar-stages-header {
  font-size: clamp(0.5rem, 1.5vw, 0.55rem);
}

.calendar-review-numbers-header {
  font-size: clamp(0.5rem, 1.5vw, 0.55rem);
  color: var(--panel--review--header-color);
  letter-spacing: 0.05rem;
  word-spacing: 0.05rem;
  text-transform: uppercase;
  text-align: center;
}

.calendar-stages-wrapper {
  grid-row: 2 / 5;
  grid-column: 1 / 2;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  align-items: flex-start;
  gap: 6px;
  padding: 10px 0;
  border-radius: 4px;
}

.calendar-stage {
  font-size: clamp(0.85rem, 1.8vw, 0.9rem);
  color: var(--panel--text-color);
  white-space: nowrap;
  padding: 3px 0;
}

.calendar-review-numbers-wrapper {
  grid-row: 2 / 5;
  grid-column: 2 / 3;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  align-items: center;
  gap: 6px;
  padding: 10px 0;
  border-radius: 4px;
  background: var(--panel--review--bg);
}

.calendar-review-number {
  font-size: clamp(0.85rem, 1.8vw, 0.9rem);
  font-weight: 600;
  border: 1px solid var(--panel--border-color);
  color: var(--panel--number-color);
  border-radius: 3px;
  padding: 2px;
  width: 30px;
  text-align: center;
}
</style>
