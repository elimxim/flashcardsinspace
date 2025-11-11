<template>
  <div class="calendar-widget calendar-widget--theme">
    <AwesomeButton
      :icon="calendarIcon"
      class="calendar-widget-button"
      :disabled="!flashcardSet"
      :on-click="toggleStore.toggleCalendar"
      fill-space
      square
    >
      <template #below>
        <div class="calendar-button-slot">
          <div class="calendar-button-text">
            Day
          </div>
          <div class="calendar-button-number">
            <template v-if="isOnVacation">
              🌴
            </template>
            <template v-else>
              {{ currDayNumber }}
            </template>
          </div>
        </div>
      </template>
    </AwesomeButton>
    <AwesomeButton
      v-if="hasNotCompletedPreviousDays"
      class="calendar-info-button"
      icon="fa-solid fa-circle-exclamation"
      :on-hover="togglePreviousDaysPopup"
    />
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
              <div class="calendar-button-number">
                {{ previousDaysTo?.seqNumber }}
              </div>
            </div>
          </template>
          <div
            v-if="prevDaysReviewTotal > 0"
            class="calendar-popup-review-row"
          >
            <div class="calendar-popup-text">
              With the total number of flashcards to review
            </div>
            <div class="calendar-popup-review-total">
              {{ prevDaysReviewTotal }}
            </div>
          </div>
          <template v-else-if="previousDaysFrom !== previousDaysTo">
            <div class="calendar-popup-text">
              They will be completed once<br>you complete the current day
            </div>
          </template>
          <template v-else>
            <div class="calendar-popup-text">
              It will be completed once<br>you complete the current day
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
import { calcStageReviews } from '@/core-logic/review-logic.ts'
import { useFlashcardStore } from '@/stores/flashcard-store.ts'
import { useChronoStore } from '@/stores/chrono-store.ts'
import { useToggleStore } from '@/stores/toggle-store.ts'
import { storeToRefs } from 'pinia'
import {
  chronodayStatuses,
  chronodayStatusesToCompleteDay,
  selectConsecutiveDaysBefore
} from '@/core-logic/chrono-logic.ts'

const flashcardStore = useFlashcardStore()
const chronoStore = useChronoStore()
const toggleStore = useToggleStore()

const { flashcardSet, flashcards, isSuspended } = storeToRefs(flashcardStore)
const { currDay, isDayOff, chronodays } = storeToRefs(chronoStore)

const showPreviousDaysPopup = ref(false)

const togglePreviousDaysPopup = () => {
  showPreviousDaysPopup.value = !showPreviousDaysPopup.value
}

const isOnVacation = computed(() => isSuspended.value || isDayOff.value)
const currDayNumber = computed(() =>
  isOnVacation.value ? '🌴' : currDay.value?.seqNumber ?? '?'
)

const previousDays = computed(() => {
  if (!currDay.value) return []
  return selectConsecutiveDaysBefore(
    chronoStore.chronodays,
    currDay.value,
    chronodayStatusesToCompleteDay,
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
  return calcStageReviews(flashcards.value, uniqueStages, currDay.value, chronodays.value)
    .reduce((acc, v) => acc + v.count, 0)
})

const calendarIcon = computed(() => {
  if (isOnVacation.value || currDay.value.status === chronodayStatuses.INITIAL) {
    return 'fa-solid fa-calendar'
  } else if (currDay.value.status === chronodayStatuses.COMPLETED) {
    return 'fa-solid fa-calendar-check'
  } else {
    return 'fa-solid fa-calendar-days'
  }
})

</script>

<style scoped>
.calendar-widget--theme {
  --c-widget--border-color: var(--calendar-widget--border-color, rgba(128, 128, 128, 0.62));
  --c-widget--popup--text--color: var(--calendar-widget--popup--text--color, rgb(29, 68, 151));
  --c-widget--popup--button--color: var(--calendar-widget--popup--button--color, rgb(253, 107, 76));
  --c-widget--popup--button--color--hover: var(--calendar-widget--popup--button--color--hover, rgb(255, 66, 61));
  --c-widget--popup--number--bg: var(--calendar-widget--popup--number--bg, rgba(255, 255, 255, 0.6));
  --c-widget--popup--number--color: var(--calendar-widget--popup--number-color, rgba(20, 27, 106, 0.82));
  --c-widget--popup--shadow-color: var(--calendar-widget--popup--shadow-color, rgba(0, 0, 0, 0.15));
}

.calendar-widget {
  position: relative;
  height: 100%;
  width: fit-content;
  aspect-ratio: 1 / 1; /* Fallback for old browsers that miscalculate the width for absolute button positioning */
}

.calendar-button-slot {
  margin-top: 8px;
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}

.calendar-info-button {
  --awesome-button--border-radius: 50%;
  --awesome-button--icon--size: 22px;
  --awesome-button--icon--color: var(--c-widget--popup--button--color);
  --awesome-button--icon--color--hover: var(--c-widget--popup--button--color--hover);
  position: absolute;
  top: -14px;
  right: 1px;
  transform: translateX(calc(100% - 12px));
  z-index: 10;
  width: 28px;
  height: 28px;
}

.calendar-button-text {
  font-size: 0.9rem;
  font-weight: 600;
  word-spacing: 0.05rem;
  letter-spacing: 0.05rem;
  text-transform: uppercase;
  white-space: nowrap;
}

.calendar-day-number {
  font-size: 0.85rem;
  font-weight: 600;
  border: 1px solid var(--c-widget--border-color);
  color: var(--c-widget--popup--number--color);
  border-radius: 3px;
  padding: 2px;
  width: 40px;
  text-align: center;
}

.calendar-popup {
  position: absolute;
  top: 100%;
  left: 0;
  margin-top: 10px;
  background-color: transparent;
  backdrop-filter: blur(40px);
  border: 1px solid var(--c-widget--border-color);
  border-radius: 6px;
  box-shadow: 0 4px 12px var(--c-widget--popup--shadow-color);
  z-index: 800;
}

.calendar-popup-layout {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 4px;
  border-radius: 6px;
  width: fit-content;
  height: fit-content;
}

.calendar-popup-header {
  font-size: 0.7rem;
  color: var(--c-widget--popup--text--color);
  letter-spacing: 0.05rem;
  word-spacing: 0.05rem;
  text-transform: uppercase;
  text-align: center;
  white-space: nowrap;
}

.calendar-popup-text {
  font-size: 0.7rem;
  color: var(--c-widget--popup--text--color);
  word-spacing: 0.05rem;
  letter-spacing: 0.05rem;
  text-transform: uppercase;
  text-align: center;
}

.calendar-button-number {
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--c-widget--popup--number--color);
  background: var(--c-widget--popup--number--bg);
  border-radius: 3px;
  padding: 2px;
  width: 40px;
  text-align: center;
}


.calendar-popup-centered-row {
  display: flex;
  flex-direction: row;
  justify-content: center;
  align-items: center;
  gap: 6px;
}

.calendar-popup-review-row {
  display: flex;
  flex-direction: row;
  justify-content: center;
  align-items: center;
  gap: 6px;
}

.calendar-popup-review-total {
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--c-widget--popup--number--color);
  background: var(--c-widget--popup--number--bg);
  border-radius: 3px;
  padding: 1px;
  min-width: 32px;
  margin-right: 2px;
  text-align: center;
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
