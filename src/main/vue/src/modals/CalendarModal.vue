<template>
  <Modal
    :visible="toggleStore.calendarOpen"
    :on-press-exit="exit"
    overflow="hidden"
  >
    <div class="calendar calendar--theme">
      <div class="calendar-month">
        <AwesomeButton
          ref="prevMonthButton"
          icon="fa-solid fa-angle-left"
          :on-click="navigatePrevMonth"
        />
        <span>{{ formattedCurrMonth }}</span>
        <AwesomeButton
          ref="nextMonthButton"
          icon="fa-solid fa-angle-right"
          :on-click="navigateNextMonth"
        />
      </div>
      <div class="calendar-weekdays">
        <div
          v-for="day in weekdays"
          :key="day"
          class="calendar-weekday"
        >
          {{ day }}
        </div>
      </div>
      <SwipeTape
        ref="swipeTape"
        :frames="calendarMonths"
        :frame-key="calendarKey"
        :snap-duration="TAPE_SNAP_DURATION"
        :threshold="80"
        :page-gap="10"
        :on-swipe-left="onSwipeRight"
        :on-swipe-right="onSwipeLeft"
      >
        <template #default="{ frame }">
          <div class="calendar-days">
            <div
              v-for="day in frame"
              :key="day.date"
              class="calendar-day"
              :class="dayCssClasses(day)"
            >
              <div v-if="canShowSeq(day)" class="calendar-cell-seq">
                {{ day.seqNumber }}
              </div>
              <div v-if="isVacationDay(day)" class="calendar-cell-vacation">
                🌴
              </div>
              <div v-else-if="canShowStages(day)" class="calendar-cell-stages">
                {{ day.stages }}
              </div>
              <span class="calendar-cell-number"> {{ day.number }} </span>
            </div>
          </div>
        </template>
      </SwipeTape>
    </div>
    <div v-if="hasAccess" class="modal-control-buttons">
      <SmartButton
        class="dangerous-button"
        text="Prev"
        :on-click="goPrevDay"
        :disabled="!isDaySwitchPossible"
        auto-blur
      />
      <SmartButton
        class="dangerous-button"
        text="Next"
        :on-click="goNextDay"
        :disabled="!isDaySwitchPossible"
        auto-blur
      />
    </div>
  </Modal>
</template>

<script setup lang="ts">
import Modal from '@/components/Modal.vue'
import SmartButton from '@/components/SmartButton.vue'
import AwesomeButton from '@/components/AwesomeButton.vue'
import {
  computed,
  onMounted,
  onUnmounted,
  ref,
  watch
} from 'vue'
import { useChronoStore } from '@/stores/chrono-store.ts'
import { storeToRefs } from 'pinia'
import {
  calcCalendarPage,
  type CalendarDay,
  chronodayStatuses
} from '@/core-logic/chrono-logic.ts'
import { useFlashcardStore } from '@/stores/flashcard-store.ts'
import { useToggleStore } from '@/stores/toggle-store.ts'
import { sendChronoSyncNextDay, sendChronoSyncPrevDay } from '@/api/api-client.ts'
import { useSpaceToaster } from '@/stores/toast-store.ts'
import { useAuthStore, UserRole } from '@/stores/auth-store.ts'
import { dateNextMonth, datePrevMonth, parseLocalDate } from '@/utils/utils.ts'
import { Log, LogTag } from '@/utils/logger.ts'
import { userApiErrors } from '@/api/user-api-error.ts'
import SwipeTape from '@/components/SwipeTape.vue'
import { ComponentExposed } from 'vue-component-type-helpers'

const toggleStore = useToggleStore()
const toaster = useSpaceToaster()
const chronoStore = useChronoStore()
const flashcardStore = useFlashcardStore()
const authStore = useAuthStore()

const { flashcardSet, isStarted } = storeToRefs(flashcardStore)
const { chronodays, currDay } = storeToRefs(chronoStore)

const hasAccess = computed(() => authStore.hasAccess(UserRole.COMMANDER))

const prevMonthButton = ref<HTMLButtonElement>()
const nextMonthButton = ref<HTMLButtonElement>()
const swipeTape = ref<ComponentExposed<typeof SwipeTape>>()

const weekdays = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat']

const currMonth = ref(parseLocalDate(currDay.value.chronodate))
const prevMonth = computed(() => datePrevMonth(currMonth.value))
const nextMonth = computed(() => dateNextMonth(currMonth.value))

const calendarMonths = computed(() => [
  calcCalendarPage(prevMonth.value, currDay.value, chronodays.value),
  calcCalendarPage(currMonth.value, currDay.value, chronodays.value),
  calcCalendarPage(nextMonth.value, currDay.value, chronodays.value),
])

function calendarKey(days: CalendarDay[], index: number) {
  return days.length > 0 ? `${days[0].date}` : index.toString()
}

const TAPE_SNAP_DURATION = 250

const userDateFormatter = new Intl.DateTimeFormat('en', { month: 'long', year: 'numeric' })
const formattedCurrMonth = computed(() => {
  return userDateFormatter.format(currMonth.value)
})

function onSwipeLeft() {
  currMonth.value = datePrevMonth(currMonth.value)
}

function onSwipeRight() {
  currMonth.value = dateNextMonth(currMonth.value)
}

function navigatePrevMonth() {
  swipeTape.value?.triggerSwipe('right')
}

function navigateNextMonth() {
  swipeTape.value?.triggerSwipe('left')
}

function dayCssClasses(day: CalendarDay): string {
  if (!day.isCurrMonth) {
    return 'calendar-day--another'
  }

  const result: string[] = []
  switch (day?.status) {
    case chronodayStatuses.INITIAL:
      result.push('calendar-day--initial')
      break
    case chronodayStatuses.COMPLETED:
      result.push('calendar-day--completed')
      break
    case chronodayStatuses.IN_PROGRESS:
      result.push('calendar-day--in-progress')
      break
    case chronodayStatuses.NOT_STARTED:
      result.push('calendar-day--not-started')
      break
    case chronodayStatuses.OFF:
      result.push('calendar-day--off')
      break
    default:
      result.push('calendar-day--empty')
      break
  }

  if (day.isCurrDay) {
    result.push('calendar-day--current')
  }

  return result.join(" ")
}

function canShowSeq(day: CalendarDay): boolean {
  return day.isCurrMonth && day.seqNumber !== undefined && day.seqNumber !== 0
}

function canShowStages(day: CalendarDay): boolean {
  return day.isCurrMonth && day.stages !== undefined
}

function isVacationDay(day: CalendarDay): boolean {
  return day.isCurrMonth && day.status === chronodayStatuses.OFF
}

function exit() {
  currMonth.value = parseLocalDate(currDay.value.chronodate)
  toggleStore.toggleCalendar()
}

async function goPrevDay() {
  if (!flashcardSet.value) return
  if (!chronoStore.canGoPrev()) return
  await sendChronoSyncPrevDay(flashcardSet.value.id)
    .then((response) => {
      chronoStore.loadState(
        response.data.chronodays,
        response.data.currDay,
        response.data.dayStreak,
      )
      currMonth.value = parseLocalDate(response.data.currDay.chronodate)
    })
    .catch((error) => {
      Log.log(LogTag.LOGIC, 'Failed to sync prev day:', error)
      toaster.bakeError(userApiErrors.SCHEDULE__PREV_FAILED, error.response?.data)
    })
}

async function goNextDay() {
  if (!flashcardSet.value) return
  if (!chronoStore.canGoNext()) return
  await sendChronoSyncNextDay(flashcardSet.value.id)
    .then((response) => {
      chronoStore.loadState(
        response.data.chronodays,
        response.data.currDay,
        response.data.dayStreak,
      )
      currMonth.value = parseLocalDate(response.data.currDay.chronodate)
    })
    .catch((error) => {
      Log.error(LogTag.LOGIC, 'Failed to sync next day:', error)
      toaster.bakeError(userApiErrors.SCHEDULE__NEXT_FAILED, error.response?.data)
    })
}

const isDaySwitchPossible = computed(() =>
  isStarted.value
)

watch(currDay, (newValue) => {
  currMonth.value = parseLocalDate(newValue.chronodate)
})

onMounted(() => {
  document.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown)
})

function handleKeydown(event: KeyboardEvent) {
  if (event.key === 'ArrowLeft') {
    navigatePrevMonth()
  } else if (event.key === 'ArrowRight') {
    navigateNextMonth()
  }
}

</script>

<style scoped>
.calendar--theme {
  --weekday--color: var(--calendar--weekday--color, #6b7280);
  --weekday--border-color: var(--calendar--weekday--border-color, rgba(0, 0, 0, 0.06));
  --day--border-color: var(--calendar--day--border-color, rgba(0, 0, 0, 0.06));
  --day--color: var(--calendar--day--color, #454545);
  --day--stages--color: var(--calendar--day--stages--color, #374151);
  --day--seq--bg: var(--calendar--day--seq--bg, rgba(0, 0, 0, 0.12));
  --day--checkmark--color: var(--calendar--day--checkmark--color, #166534);
  --today-ring: var(--calendar--today-ring, #7c3aed);
  --day--empty--bg-color: var(--calendar--day--empty--bg-color, rgba(115, 115, 115, 0.4));
  --day--initial--color: var(--calendar--day--initial--color, white);
  --day--initial--bg-color: var(--calendar--day--initial--bg-color, #f59e0b);
  --day--initial--stripe-color: var(--calendar--day--initial--stripe-color, rgba(245, 158, 11, 0.35));
  --day--completed--color: var(--calendar--day--completed--color, white);
  --day--completed--bg-color: var(--calendar--day--completed--bg-color, #34d399);
  --day--completed--stripe-color: var(--calendar--day--completed--stripe-color, rgba(52, 211, 153, 0.35));
  --day--in-progress--color: var(--calendar--day--in-progress--color, white);
  --day--in-progress--bg-color: var(--calendar--day--in-progress--bg-color, #244fac);
  --day--in-progress--stripe-color: var(--calendar--day--in-progress--stripe-color, rgba(14, 49, 126, 0.35));
  --day--not-started--color: var(--calendar--day--not-started--color, white);
  --day--not-started--bg-color: var(--calendar--day--not-started--bg-color, #e5e7eb);
  --day--not-started--stripe-color: var(--calendar--day--not-started--stripe-color, rgba(115, 115, 115, 0.35));
  --day--off--color: var(--calendar--day--off--color, white);
  --day--off--bg-color: var(--calendar--day--off--bg-color, #43938a);
  --day--off--stripe-color: var(--calendar--day--off--stripe-color, rgba(115, 115, 115, 0.35));
}

.calendar {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 4px;
  min-width: 0;
  min-height: 0;
}

.calendar-month {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px;
  font-size: clamp(0.95rem, 2vw, 1.15rem);
  font-weight: 600;
  --awesome-button--icon--size: clamp(0.9rem, 2vw, 1.1rem);
}

.calendar-weekdays {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
  font-size: clamp(0.8rem, 1.8vw, 0.95rem);
  text-transform: uppercase;
  color: var(--weekday--color);
}

.calendar-weekday {
  display: flex;
  flex-direction: column;
  padding: 4px;
  text-align: center;
  min-width: 0;
  min-height: 0;
  font-weight: 600;
  text-transform: uppercase;
  color: var(--weekday--color);
}

.calendar-days {
  height: 100%;
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  grid-template-rows: repeat(6, 1fr);
  gap: 4px;
  min-width: 0;
}

.calendar-day {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  border-radius: 4px;
  padding: 4px;
  text-align: center;
  min-width: 0;
  min-height: 0;
  background: transparent;
  color: var(--day--color);
  border: 2px solid var(--day--border-color);
  position: relative;
  overflow: hidden;
  transition: transform 0.1s ease-in-out;
}

.calendar-day--another {
  background: transparent;
  opacity: 0.4;
}

.calendar-day--empty {
  background-color: var(--day--empty--bg-color);
  opacity: 0.4;
}

.calendar-day--initial {
  color: var(--day--initial--color);
  background-color: var(--day--initial--bg-color);
  background-image: repeating-linear-gradient(
    135deg,
    var(--day--initial--stripe-color) 0 10px,
    transparent 10px 20px
  );
}

.calendar-day--completed {
  color: var(--day--completed--color);
  background-color: var(--day--completed--bg-color);
  background-image: repeating-linear-gradient(
    135deg,
    var(--day--completed--stripe-color) 0 10px,
    transparent 10px 20px
  );
}

.calendar-day--completed::before {
  content: "✔";
  position: absolute;
  top: 0;
  left: 0;
  font-size:clamp(0.45rem, 1.8vw, 0.65rem);
  line-height: 1;
  color: var(--day--checkmark--color);
  pointer-events: none;
}

.calendar-day--in-progress {
  color: var(--day--in-progress--color);
  background-color: var(--day--in-progress--bg-color);
  background-image: repeating-linear-gradient(
    135deg,
    var(--day--in-progress--stripe-color) 0 10px,
    transparent 10px 20px
  );
}

.calendar-day--not-started {
  color: var(--day--not-started--color);
  background-color: var(--day--not-started--bg-color);
  background-image: repeating-linear-gradient(
    135deg,
    var(--day--not-started--stripe-color) 0 10px,
    transparent 10px 20px
  );
}

.calendar-day--current {
  box-shadow: 0 0 0 2px var(--today-ring);
}

.calendar-day--off {
  color: var(--day--off--color);
  background-color: var(--day--off--bg-color);
  background-image: repeating-linear-gradient(
    135deg,
    var(--day--off--stripe-color) 0 10px,
    transparent 10px 20px
  );
}

@media (hover: hover) {
  .calendar-day:hover:not(.calendar-day--another) {
    transform: scale(1.08);
  }
}

.calendar-cell-number {
  position: absolute;
  top: 0;
  left: 0;
  padding-bottom: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.4rem;
  font-weight: 800;
  height: 100%;
  width: 100%;
  opacity: 0.9;
}

.calendar-cell-seq {
  position: absolute;
  top: 1px;
  right: 1px;
  font-size: clamp(0.65rem, 1.8vw, 0.75rem);
  line-height: 1;
  align-self: end;
}

.calendar-cell-stages {
  position: absolute;
  bottom: 2px;
  font-weight: 800;
  font-size: 0.75rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-top: 2px;
  color: var(--day--stages--color);
  align-self: center;
}

.calendar-cell-vacation {
  font-size: clamp(1rem, 1.8vw, 1.6rem);
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 1;
}

.modal-control-buttons {
  display: flex;
  flex-direction: row;
  justify-content: center;
  gap: 10px;
}

</style>
