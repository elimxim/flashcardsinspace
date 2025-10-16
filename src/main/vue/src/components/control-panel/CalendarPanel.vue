<template>
  <div class="calendar-panel calendar-panel--theme">
    <div class="current-day-panel">
      <div class="calendar-panel-label">
        Current day
      </div>
      <div class="current-day-panel-layout">
        <div class="current-day-panel__left-area">
          <AwesomeButton
            :icon="calendarIcon"
            class="calendar-panel-button"
            :disabled="!flashcardSet"
            :on-click="modalStore.toggleCalendar"
          />
          <div class="current-calendar-day">
            <div class="calendar-day-text">
              Day
            </div>
            <div v-if="isOnVacation" class="calendar-day-vacation">
              🌴
            </div>
            <div v-else class="calendar-day-number">
              {{ currDayNumber }}
            </div>
          </div>
        </div>
        <div class="current-day-panel__right-area">
          <div class="review-stages-header"/>
          <div class="review-numbers-header">
            To review
          </div>
          <div class="review-stages-wrapper">
            <div
              v-for="review in currDayStageReviews"
              :key="review.stage"
              class="review-stage"
            >
              {{ review.stage }}
            </div>
            <div class="review-stage">
              Total
            </div>
          </div>
          <div class="review-numbers-wrapper">
            <div
              v-for="review in currDayStageReviews"
              :key="review.stage"
              class="review-number"
            >
              {{ review.count }}
            </div>
            <div class="review-number">
              {{ currDayReviewTotal }}
            </div>
          </div>
        </div>
      </div>
    </div>
    <div v-if="hasNotCompletedPreviousDays" class="previous-days-panel">
      <div class="calendar-panel-label">
        Previous days
      </div>
      <div class="previous-days-layout">
        <div class="previous-days-header">
          You have uncompleted days
        </div>
        <div v-if="previousDaysFrom !== previousDaysTo" class="previous-days-range">
          <div class="calendar-day-text">
            From
          </div>
          <div class="calendar-day-number">
            {{ previousDaysFrom?.seqNumber }}
          </div>
          <div class="calendar-day-text">
            To
          </div>
          <div class="calendar-day-number">
            {{ previousDaysTo?.seqNumber }}
          </div>
        </div>
        <div v-else class="previous-days-range">
          <div class="calendar-day-text">
            Day
          </div>
          <div class="calendar-day-number">
            {{ previousDaysTo?.seqNumber }}
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
import { calcStageReviews, StageReview } from '@/core-logic/review-logic.ts'
import { useFlashcardStore } from '@/stores/flashcard-store.ts'
import { useChronoStore } from '@/stores/chrono-store.ts'
import { useModalStore } from '@/stores/modal-store.ts'
import { storeToRefs } from 'pinia'
import { isCompleteAvailable, selectConsecutiveDaysBefore } from '@/core-logic/chrono-logic.ts';

const flashcardStore = useFlashcardStore()
const chronoStore = useChronoStore()
const modalStore = useModalStore()

const { flashcardSet, flashcards, isSuspended } = storeToRefs(flashcardStore)
const { currDay, isDayOff } = storeToRefs(chronoStore)

const isOnVacation = computed(() => isSuspended.value || isDayOff.value)
const currDayNumber = computed(() =>
  isOnVacation.value ? '🌴' : currDay.value?.seqNumber ?? '?'
)

const currDayStageReviews = computed<StageReview[]>(() => {
  if (!currDay.value) return []
  return calcStageReviews(flashcards.value, currDay.value.stages)
})

const currDayReviewTotal = computed(() =>
  currDayStageReviews.value.reduce((acc, v) => acc + v.count, 0)
)

const previousDays = computed(() => {
  if (!currDay.value) return []
  return selectConsecutiveDaysBefore(
    chronoStore.chronodays,
    currDay.value,
    isCompleteAvailable,
    false,
  )
})

const hasNotCompletedPreviousDays = computed(() => {
  return previousDays.value.length !== 0
})

const previousDaysFrom = computed(() => {
  const days = previousDays.value
  if (days.length !== 0) {
    return days[days.length - 1]
  }
})

const previousDaysTo = computed(() => {
  const days = previousDays.value
  if (days.length !== 0) {
    return days[0]
  }
})

const prevDaysReviewTotal = computed(() => {
  if (!currDay.value) return 0
  const currStages = new Set(currDay.value.stages)
  const prevStages = previousDays.value.map(d => d.stages).flat()
  const uniqueStages = [...new Set(prevStages)].filter(v => !currStages.has(v))
  return calcStageReviews(flashcards.value, uniqueStages)
    .reduce((acc, v) => acc + v.count, 0)
})

const calendarIcon = computed(() => {
  if (isOnVacation.value) {
    return 'fa-solid fa-calendar'
  } else if (currDayReviewTotal.value === 0) {
    return 'fa-solid fa-calendar-check'
  } else {
    return 'fa-solid fa-calendar-days'
  }
})

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
  flex-direction: row;
  gap: 10px;
}

.current-day-panel {
  display: flex;
  flex-direction: column;
  padding: 1px;
}

.previous-days-panel {
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

.current-day-panel-layout {
  display: flex;
  flex-direction: row;
  padding: 2px 6px 4px 4px;
  gap: 10px;
  border: 1px solid var(--panel--border-color);
  border-radius: 6px;
}

.previous-days-layout {
  display: flex;
  flex-direction: column;
  padding: 2px 6px;
  gap: 2px;
  border: 1px solid var(--panel--border-color);
  border-radius: 6px;
}

.current-day-panel__left-area {
  display: grid;
  grid-template-rows: 1fr auto;
  flex-direction: column;
  justify-content: space-between;
  gap: 6px;
  margin: 4px;
}

.current-calendar-day {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  height: 20px;
}

.calendar-day-text {
  font-size: clamp(0.85rem, 1.8vw, 0.9rem);
  color: var(--panel--text-color);
  white-space: nowrap;
}

.calendar-day-number {
  font-size: clamp(0.85rem, 1.8vw, 0.9rem);
  font-weight: 600;
  border: 1px solid var(--panel--border-color);
  color: var(--panel--number-color);
  border-radius: 3px;
  padding: 2px;
  width: 40px;
  text-align: center;
}

.calendar-day-vacation {
  font-size: clamp(0.85rem, 1.8vw, 0.9rem);
  width: 40px;
  text-align: center;
}

.current-day-panel__right-area {
  display: grid;
  grid-template-columns: auto auto;
  grid-template-rows: auto repeat(3, 1fr);
  gap: 1px 10px;
  align-items: center;
}

.review-stages-header {
  font-size: clamp(0.5rem, 1.5vw, 0.6rem);
}

.review-numbers-header {
  font-size: clamp(0.5rem, 1.5vw, 0.6rem);
  color: var(--panel--review--header-color);
  letter-spacing: 0.05rem;
  word-spacing: 0.05rem;
  text-transform: uppercase;
  text-align: center;
  white-space: nowrap;
}

.review-stages-wrapper {
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

.review-stage {
  font-size: clamp(0.85rem, 1.8vw, 0.9rem);
  color: var(--panel--text-color);
  white-space: nowrap;
  padding: 3px 0;
}

.review-numbers-wrapper {
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

.review-number {
  font-size: clamp(0.85rem, 1.8vw, 0.9rem);
  font-weight: 600;
  border: 1px solid var(--panel--border-color);
  color: var(--panel--number-color);
  border-radius: 3px;
  padding: 2px;
  width: 30px;
  text-align: center;
}

.previous-days-header {
  font-size: clamp(0.5rem, 1.5vw, 0.6rem);
  color: var(--panel--review--header-color);
  letter-spacing: 0.05rem;
  word-spacing: 0.05rem;
  text-transform: uppercase;
  text-align: start;
  white-space: nowrap;
}

.previous-days-range {
  display: flex;
  flex-direction: row;
  justify-content: start;
  align-items: center;
  gap: 6px;
}
</style>
