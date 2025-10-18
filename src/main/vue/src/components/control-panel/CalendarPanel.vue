<template>
  <div class="calendar-panel calendar-panel--theme">
    <div class="calendar-panel-content">
      <div class="calendar-panel-label">
        Current day
      </div>
      <AwesomeButton
        v-if="hasNotCompletedPreviousDays"
        class="calendar-panel-info-button calendar-panel-info-button-layout"
        icon="fa-solid fa-circle-exclamation"
        :on-hover="togglePreviousDaysPopup"
      />
      <div class="calendar-panel-layout">
        <div class="calendar-panel-left-area">
          <AwesomeButton
            :icon="calendarIcon"
            class="calendar-panel-button"
            :disabled="!flashcardSet"
            :on-click="modalStore.toggleCalendar"
            fill-space
            square
          />
          <div class="calendar-panel-day">
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
        <div class="calendar-panel-right-area">
          <div class="calendar-panel-review-header">
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
        <div class="calendar-panel-launch-area">
          <LaunchButton
            :on-click="startReview"
          />
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
import LaunchButton from '@/components/control-panel/LaunchButton.vue'
import { computed, ref } from 'vue'
import { calcStageReviews, StageReview } from '@/core-logic/review-logic.ts'
import { useFlashcardStore } from '@/stores/flashcard-store.ts'
import { useChronoStore } from '@/stores/chrono-store.ts'
import { useModalStore } from '@/stores/modal-store.ts'
import { storeToRefs } from 'pinia'
import {
  chronodayStatuses,
  isCompleteAvailable,
  selectConsecutiveDaysBefore
} from '@/core-logic/chrono-logic.ts'
import { useRouter } from 'vue-router'
import { routeNames } from '@/router'

const router = useRouter()
const flashcardStore = useFlashcardStore()
const chronoStore = useChronoStore()
const modalStore = useModalStore()

const { flashcardSet, flashcards, isSuspended } = storeToRefs(flashcardStore)
const { currDay, isDayOff } = storeToRefs(chronoStore)

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

function startReview() {
  router.push({ name: routeNames.review })
}

</script>

<style scoped>
.calendar-panel--theme {
  --d-panel--label-color: var(--calendar-panel--label-color, #555555);
  --d-panel--text-color: var(--calendar-panel--text-color, rgba(57, 57, 57, 0.92));
  --d-panel--number-color: var(--calendar-panel--number-color, rgba(17, 33, 85, 0.92));
  --d-panel--border-color: var(--calendar-panel--border-color, rgba(128, 128, 128, 0.62));
  --d-panel--review--header-color: var(--calendar-panel--review--header-color, rgba(43, 69, 142, 0.88));
  --d-panel--review--bg: var(--calendar-panel--review--bg, rgba(88, 114, 209, 0.13));
}

.calendar-panel {
  position: relative;
  display: flex;
  flex-direction: row;
}

.calendar-panel-label {
  font-size: clamp(0.5rem, 1.8vw, 0.6rem);
  color: var(--d-panel--label-color);
  letter-spacing: 0.1rem;
  word-spacing: 0.1rem;
  text-transform: uppercase;
  padding: 0 0 0 6px;
}

.calendar-panel-info-button-layout {
  position: absolute;
  top: -2px;
  right: -12px;
  z-index: 10;
}

.calendar-panel-content {
  position: relative;
  display: flex;
  flex-direction: column;
  padding: 1px;
}

.calendar-panel-layout {
  display: flex;
  flex-direction: row;
  padding: 4px;
  gap: 4px;
  border: 1px solid var(--d-panel--border-color);
  border-radius: 6px;
  width: clamp(280px, 40vw, 380px);
  height: clamp(110px, 20vw, 120px);
}

.calendar-panel-left-area {
  display: grid;
  grid-template-rows: 1fr auto;
  flex-direction: column;
  justify-content: space-between;
  gap: 4px;
  height: 100%;
}

.calendar-panel-right-area {
  display: flex;
  flex-direction: column;
  gap: 1px;
  height: 100%;
}

.calendar-panel-launch-area {
  flex: 1;
  display: flex;
  align-items: center;
  height: 100%;
}

.calendar-panel-day {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  padding: 4px;
  gap: clamp(4px, 1vw, 8px);
}

.calendar-day-text {
  font-size: clamp(0.75rem, 1.5vw, 0.9rem);
  color: var(--d-panel--text-color);
  white-space: nowrap;
}

.calendar-day-number {
  font-size: clamp(0.75rem, 2vw, 0.9rem);
  font-weight: 500;
  border: 1px solid var(--d-panel--border-color);
  color: var(--d-panel--number-color);
  border-radius: 3px;
  padding: 1px;
  width: clamp(30px, 4vw, 40px);
  text-align: center;
}

.calendar-day-vacation {
  font-size: clamp(0.70rem, 2vw, 0.8rem);
  font-weight: 600;
  border: 1px solid var(--d-panel--border-color);
  color: var(--d-panel--number-color);
  border-radius: 3px;
  padding: 1px;
  width: clamp(30px, 4vw, 40px);
  text-align: center;
}

.calendar-panel-review-header {
  font-size: clamp(0.5rem, 1vw, 0.6rem);
  color: var(--d-panel--review--header-color);
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
  gap: 0 4px;
  border: 2px solid var(--d-panel--review--bg);
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
  border-radius: 2px;
  height: 100%;
  min-height: 0;
  background: var(--d-panel--review--bg);
}

.review-stage {
  font-size: clamp(0.75rem, 1.5vw, 0.9rem);
  color: var(--d-panel--text-color);
  white-space: nowrap;
  padding: 3px 0;
}

.review-number {
  font-size: clamp(0.75rem, 2vw, 0.9rem);
  font-weight: 500;
  border: 1px solid var(--d-panel--border-color);
  color: var(--d-panel--number-color);
  border-radius: 3px;
  padding: 2px;
  width: 30px;
  text-align: center;
}

.calendar-popup {
  position: absolute;
  top: 100%;
  left: 0;
  margin-top: 4px;
  background-color: transparent;
  backdrop-filter: blur(40px);
  border: 1px solid var(--d-panel--border-color);
  border-radius: 6px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 800;
}

.calendar-popup-layout {
  display: flex;
  flex-direction: column;
  padding: 4px;
  border-radius: 6px;
  width: clamp(280px, 40vw, 380px);
  height: fit-content;
}

.calendar-popup-header {
  font-size: clamp(0.5rem, 1vw, 0.6rem);
  color: var(--d-panel--review--header-color);
  letter-spacing: 0.05rem;
  word-spacing: 0.05rem;
  text-transform: uppercase;
  text-align: center;
  white-space: nowrap;
}

.calendar-popup-text {
  font-size: clamp(0.5rem, 1vw, 0.6rem);
  color: var(--d-panel--review--header-color);
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
  background: var(--d-panel--review--bg);
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
