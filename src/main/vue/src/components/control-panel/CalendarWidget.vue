<template>
  <div class="calendar-widget calendar-widget--theme">
    <div class="calendar-main-area">
      <div class="calendar-button-wrapper">
        <AwesomeButton
          :icon="calendarIcon"
          class="calendar-widget-button"
          :disabled="!flashcardSet"
          :on-click="toggleStore.toggleCalendar"
          fill-space
          square
        />
        <AwesomeButton
          v-if="hasNotCompletedPreviousDays"
          class="calendar-info-button"
          icon="fa-solid fa-circle-exclamation"
          :on-hover="togglePreviousDaysPopup"
        />
      </div>
      <div class="calendar-day-area">
        <div class="calendar-day-text">
          Day
        </div>
        <div class="calendar-day-number">
          <template v-if="isOnVacation">
            🌴
          </template>
          <template v-else>
            {{ currDayNumber }}
          </template>
        </div>
      </div>
    </div>
    <div v-if="!isInitialDay" class="calendar-review-area">
      <div class="calendar-review-header">
        To review
      </div>
      <div class="review-grid">
        <div class="review-stage-column">
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
        <div class="review-number-column">
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
    <transition name="slide-fade">
      <div
        v-if="hasNotCompletedPreviousDays && showPreviousDaysPopup"
        class="calendar-popup"
      >
        <div class="calendar-popup-layout">
          <template v-if="previousDaysFrom !== previousDaysTo">
            <div class="calendar-popup-header">
              You have uncompleted previous days
            </div>
            <div class="calendar-popup-centered-row">
              <div class="calendar-popup-text">
                From
              </div>
              <div class="calendar-day-number">
                {{ previousDaysFrom?.seqNumber }}
              </div>
              <div class="calendar-popup-text">
                To
              </div>
              <div class="calendar-day-number">
                {{ previousDaysTo?.seqNumber }}
              </div>
            </div>
          </template>
          <template v-else>
            <div class="calendar-popup-header">
              You have an uncompleted previous day
            </div>
            <div class="calendar-popup-centered-row">
              <div class="calendar-day-number">
                {{ previousDaysTo?.seqNumber }}
              </div>
            </div>
          </template>
          <div
            v-if="prevDaysReviewTotal > 0"
            class="calendar-popup-review"
          >
            <div class="calendar-popup-text">
              With the total number of flashcards to review
            </div>
            <div class="calendar-popup-review-total">
              <div class="review-number">
                {{ prevDaysReviewTotal }}
              </div>
            </div>
          </div>
          <template v-else-if="previousDaysFrom !== previousDaysTo">
            <div class="calendar-popup-text">
              They will be completed since
            </div>
            <div class="calendar-popup-text">
              you complete the current day
            </div>
          </template>
          <template v-else>
            <div class="calendar-popup-text">
              It will be completed since
            </div>
            <div class="calendar-popup-text">
              you complete the current day
            </div>
          </template>
        </div>
      </div>
    </transition>
  </div>
  <CalendarModal/>
</template>

<script setup lang="ts">
import AwesomeButton from '@/components/AwesomeButton.vue'
import CalendarModal from '@/views/modal/CalendarModal.vue'
import { computed, ref } from 'vue'
import { calcStageReviews, StageReview } from '@/core-logic/review-logic.ts'
import { useFlashcardStore } from '@/stores/flashcard-store.ts'
import { useChronoStore } from '@/stores/chrono-store.ts'
import { useToggleStore } from '@/stores/toggle-store.ts'
import { storeToRefs } from 'pinia'
import {
  chronodayStatuses,
  isCompleteAvailable,
  selectConsecutiveDaysBefore
} from '@/core-logic/chrono-logic.ts'

const flashcardStore = useFlashcardStore()
const chronoStore = useChronoStore()
const toggleStore = useToggleStore()

const { flashcardSet, flashcards, isSuspended } = storeToRefs(flashcardStore)
const {
  currDay,
  isDayOff,
  isInitialDay,
} = storeToRefs(chronoStore)

const showPreviousDaysPopup = ref(false)

const togglePreviousDaysPopup = () => {
  showPreviousDaysPopup.value = !showPreviousDaysPopup.value
}

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
  } else {
    return currDay.value
  }
})

const previousDaysTo = computed(() => {
  const days = previousDays.value
  if (days.length !== 0) {
    return days[0]
  } else {
    return currDay.value
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
  if (isOnVacation.value || currDay.value.status === chronodayStatuses.INITIAL) {
    return 'fa-solid fa-calendar'
  } else if (currDayReviewTotal.value === 0) {
    return 'fa-solid fa-calendar-check'
  } else {
    return 'fa-solid fa-calendar-days'
  }
})

</script>

<style scoped>
.calendar-widget--theme {
  --d-widget--text-color: var(--calendar-widget--text-color, rgba(57, 57, 57, 0.92));
  --d-widget--number-color: var(--calendar-widget--number-color, rgba(17, 33, 85, 0.92));
  --d-widget--border-color: var(--calendar-widget--border-color, rgba(128, 128, 128, 0.62));
  --d-widget--review--header-color: var(--calendar-widget--review--header-color, rgba(43, 69, 142, 0.88));
  --d-widget--review--bg: var(--calendar-widget--review--bg, rgba(88, 114, 209, 0.13));
  --d-widget--info-button--color: var(--calendar-widget--info-button--color, rgb(253, 107, 76));
  --d-widget--info-button--color--hover: var(--calendar-widget--info-button--color--hover, rgb(255, 66, 61));
}

.calendar-widget {
  position: relative;
  display: flex;
  flex-direction: row;
  padding: 1px;
  gap: 10px;
  width: fit-content;
  height: 100%;
}

.calendar-button-wrapper {
  position: relative;
  height: 100%;
}

.calendar-info-button {
  --awesome-button--border-radius: 50%;
  --awesome-button--font-size: 22px;
  --awesome-button--color: var(--d-widget--info-button--color);
  --awesome-button--color--hover: var(--d-widget--info-button--color--hover);
  position: absolute;
  top: -14px;
  right: -14px;
  z-index: 10;
}

.calendar-main-area {
  display: grid;
  grid-template-rows: minmax(0, 1fr) auto;
  gap: 4px;
  height: 100%;
}

.calendar-review-area {
  display: flex;
  flex-direction: column;
  gap: 1px;
  height: 100%;
}

.calendar-day-area {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  padding: 0 4px;
  gap: 4px;
}

.calendar-day-text {
  font-size: 0.9rem;
  color: var(--d-widget--text-color);
  white-space: nowrap;
}

.calendar-day-number {
  font-size: 0.8rem;
  font-weight: 500;
  border: 1px solid var(--d-widget--border-color);
  color: var(--d-widget--number-color);
  border-radius: 3px;
  padding: 1px;
  width: 40px;
  text-align: center;
}

.calendar-review-header {
  font-size: 0.7rem;
  color: var(--d-widget--review--header-color);
  letter-spacing: 0.05rem;
  word-spacing: 0.05rem;
  text-transform: uppercase;
  text-align: center;
  white-space: nowrap;
}

.review-grid {
  flex: 1;
  min-width: 0;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  grid-template-rows: repeat(3, 1fr);
  grid-auto-flow: column;
  align-items: center;
  gap: 4px;
  border: 2px solid var(--d-widget--review--bg);
  border-radius: 4px;
  padding-left: 4px;
}

.review-stage-column {
  grid-column: 1 / 2;
  grid-row: 1 / 4;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  align-items: flex-start;
  padding: 10px 0;
  border-radius: 4px;
  height: 100%;
  min-height: 0;
}

.review-number-column {
  grid-column: 2 / 3;
  grid-row: 1 / 4;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  align-items: center;
  padding: 10px 4px;
  border-radius: 0 2px 2px 0;
  height: 100%;
  min-height: 0;
  background: var(--d-widget--review--bg);
}

.review-stage {
  font-size: 0.9rem;
  color: var(--d-widget--text-color);
  white-space: nowrap;
  padding: 3px 0;
}

.review-number {
  font-size: 0.8rem;
  font-weight: 500;
  border: 1px solid var(--d-widget--border-color);
  color: var(--d-widget--number-color);
  border-radius: 3px;
  padding: 1px;
  width: 30px;
  text-align: center;
}

.calendar-popup {
  position: absolute;
  top: 100%;
  left: 0;
  margin-top: 10px;
  background-color: transparent;
  backdrop-filter: blur(40px);
  border: 1px solid var(--d-widget--border-color);
  border-radius: 6px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 800;
}

.calendar-popup-layout {
  display: flex;
  flex-direction: column;
  padding: 4px;
  border-radius: 6px;
  width: fit-content;
  height: fit-content;
}

.calendar-popup-header {
  font-size: 0.6rem;
  color: var(--d-widget--review--header-color);
  letter-spacing: 0.05rem;
  word-spacing: 0.05rem;
  text-transform: uppercase;
  text-align: center;
  white-space: nowrap;
}

.calendar-popup-text {
  font-size: 0.6rem;
  color: var(--d-widget--review--header-color);
  word-spacing: 0.05rem;
  letter-spacing: 0.05rem;
  text-transform: uppercase;
  text-align: center;
}

.calendar-popup-centered-row {
  display: flex;
  flex-direction: row;
  justify-content: center;
  align-items: center;
  gap: 6px;
}

.calendar-popup-review {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  gap: 6px;
}

.calendar-popup-review-total {
  padding: 4px;
  border-radius: 4px;
  background: var(--d-widget--review--bg);
}

.slide-fade-enter-active {
  transition: opacity 0.2s ease-out;
}

.slide-fade-leave-active {
  transition: opacity 0.2s ease-in;
}

.slide-fade-enter-from {
  opacity: 0;
}

.slide-fade-leave-to {
  opacity: 0;
}

</style>
