<template>
  <div class="calendar-widget">
    <AwesomeButton
      :icon="calendarIcon"
      class="cp--widget"
      :disabled="!flashcardSet"
      :on-click="toggleStore.toggleCalendar"
      fill-space
      square
    >
      <template #below>
        <div class="calendar-button-slot">
          <div class="cp--text">
            Day
          </div>
          <div class="cp--count-box">
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
            <div class="cp--text cp--text--sub cp--text--nowrap">
              You have uncompleted previous days
            </div>
            <div class="calendar-popup-centered-row">
              <div class="cp--text cp--text--sub">
                From
              </div>
              <div class="cp--count-box">
                {{ previousDaysFrom?.seqNumber }}
              </div>
              <div class="cp--text cp--text--sub">
                To
              </div>
              <div class="cp--count-box">
                {{ previousDaysTo?.seqNumber }}
              </div>
            </div>
          </template>
          <template v-else>
            <div class="cp--text cp--text--sub cp--text--nowrap">
              You have an uncompleted previous day
            </div>
            <div class="calendar-popup-centered-row">
              <div class="cp--count-box">
                {{ previousDaysTo?.seqNumber }}
              </div>
            </div>
          </template>
          <div
            v-if="prevDaysReviewTotal > 0"
            class="calendar-popup-review-row"
          >
            <div class="cp--text cp--text--sub">
              With the total number of flashcards to review
            </div>
            <div class="cp--count-box">
              {{ prevDaysReviewTotal }}
            </div>
          </div>
          <template v-else-if="previousDaysFrom !== previousDaysTo">
            <div class="cp--text cp--text--sub">
              They will be completed once<br>you complete the current day
            </div>
          </template>
          <template v-else>
            <div class="cp--text cp--text--sub">
              It will be completed once<br>you complete the current day
            </div>
          </template>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import AwesomeButton from '@/components/AwesomeButton.vue'
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
  --awesome-button--icon--color: rgb(253, 107, 76);
  --awesome-button--icon--color--hover: rgb(255, 66, 61);
  position: absolute;
  top: -14px;
  right: 1px;
  transform: translateX(calc(100% - 12px));
  z-index: 10;
  width: 28px;
  height: 28px;
}

.calendar-popup {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translateX(-50%);
  background-color: transparent;
  backdrop-filter: blur(40px);
  border: 1px solid var(--cp--border-color);
  border-radius: 6px;
  box-shadow: 0 4px 12px var(--cp--shadow-color);
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
