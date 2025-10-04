<template>
  <Modal
    :visible="visible"
    :on-press-exit="exit"
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
      <div class="calendar-days">
        <div
          v-for="day in calendarPage"
          :key="day.date"
          class="calendar-day"
          :class="cellClasses(day)"
        >
          <div class="calendar-day__top">
            <div class="calendar-day__top__number">
              {{ day.number }}
            </div>
            <div v-if="day.isCurrMonth && day.seqNumber !== null" class="calendar-day__top__seq">
              {{ day.seqNumber }}
            </div>
          </div>
          <div v-if="day.isCurrMonth && day.stages !== null" class="calendar-day__stages">
            {{ day.stages }}
          </div>
        </div>
      </div>
    </div>
    <div class="modal-control-buttons">
      <SmartButton
        class="calendar-button"
        text="Prev"
        :on-click="switchToPrevDay"
        :disabled="!isDaySwitchPossible"
        auto-blur
      />
      <SmartButton
        class="calendar-button"
        text="Next"
        :on-click="switchToNextDay"
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
import { computed, defineEmits, onMounted, onUnmounted, ref, watch } from 'vue'
import { useChronoStore } from '@/stores/chrono-store.ts'
import { storeToRefs } from 'pinia'
import {
  calcCalendarPage,
  type CalendarDay,
  chronodayStatuses
} from '@/core-logic/chrono-logic.ts'
import { useFlashcardSetStore } from '@/stores/flashcard-set-store.ts'
import { useGlobalStore } from '@/stores/global-store.ts'

const visible = defineModel<boolean>('visible', { default: false })

const globalStore = useGlobalStore()
const chronoStore = useChronoStore()
const flashcardSetStore = useFlashcardSetStore()

const { isStarted, isSuspended } = storeToRefs(flashcardSetStore)
const { chronodays, currDay } = storeToRefs(chronoStore)

const prevMonthButton = ref<HTMLButtonElement>()
const nextMonthButton = ref<HTMLButtonElement>()

const weekdays = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat']
const currMonth = ref(new Date(currDay.value.chronodate))

const calendarPage = computed(() =>
  calcCalendarPage(currMonth.value, currDay.value, chronodays.value)
)

const userDateFormatter = new Intl.DateTimeFormat('en', { month: 'long', year: 'numeric' })
const formattedCurrMonth = computed(() => {
  return userDateFormatter.format(currMonth.value)
})

function navigatePrevMonth() {
  const newMonth = new Date(currMonth.value)
  newMonth.setMonth(currMonth.value.getMonth() - 1)
  currMonth.value = newMonth
}

function navigateNextMonth() {
  const newMonth = new Date(currMonth.value)
  newMonth.setMonth(currMonth.value.getMonth() + 1)
  currMonth.value = newMonth
}

function cellClasses(day: CalendarDay): string {
  if (!day.isCurrMonth) {
    return 'calendar-day--empty'
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

function exit() {
  currMonth.value = new Date(currDay.value.chronodate)
  globalStore.toggleCalendarModalForm()
}

function switchToPrevDay() {
  chronoStore.switchToPrevDay()
  currMonth.value = new Date(currDay.value.chronodate)
}

function switchToNextDay() {
  chronoStore.switchToNextDay()
  currMonth.value = new Date(currDay.value.chronodate)
}

const isDaySwitchPossible = computed(() =>
  isStarted.value && !isSuspended.value
)

watch(currDay, (newValue) => {
  currMonth.value = new Date(newValue.chronodate)
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
  --day--color: var(--calendar--day--color, #1f2937);
  --day--stages--color: var(--calendar--day--stages--color, #374151);
  --day--seq--bg: var(--calendar--day--seq--bg, rgba(0, 0, 0, 0.12));
  --checkmark--color: var(--calendar--checkmark--color, #166534);
  --today-ring: var(--calendar--today-ring, #7c3aed);
  --initial--bg: var(--calendar--initial--bg, #f59e0b);
  --completed--bg: var(--calendar--completed--bg, #34d399);
  --in-progress--bg: var(--calendar--in-progress--bg, #7188bc);
  --not-started--bg: var(--calendar--not-started--bg, #e5e7eb);
  --off--bg: var(--calendar--off--bg, #43938a);
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
  --awesome-button--font-size: clamp(0.9rem, 2vw, 1.1rem);
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
  flex: 1;
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  grid-template-rows: repeat(6, 1fr);
  min-height: 0;
  gap: 4px;
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
  border: 1px solid var(--day--border-color);
  position: relative;
  overflow: hidden;
}

.calendar-day--empty {
  background: none;
  opacity: 0.4;
}

.calendar-day--initial {
  background-color: var(--initial--bg);
}

.calendar-day--completed {
  background-color: var(--completed--bg);
}

.calendar-day--completed::before {
  content: "✔";
  position: absolute;
  right: 6px;
  bottom: 4px;
  font-size: 0.95rem;
  line-height: 1;
  color: var(--checkmark--color);
  pointer-events: none;
}

.calendar-day--in-progress {
  background-color: var(--in-progress--bg);
}

.calendar-day--not-started {
  background-color: var(--not-started--bg);
}

.calendar-day--current {
  box-shadow: 0 0 0 2px var(--today-ring) inset
}

.calendar-day--off {
  background-color: var(--off--bg);
}

.calendar-day:hover:not(.calendar-day--empty) {
  transform: scale(1.08);
}

.calendar-day__top {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  gap: 6px;
}

.calendar-day__top__number {
  font-weight: bold;
  font-size: clamp(0.8rem, 2vw, 1rem);
}

.calendar-day__top__seq {
  font-size: clamp(0.65rem, 1.6vw, 0.85rem);
  padding: 2px 6px;
  border-radius: 10px;
  background-color: var(--day--seq--bg);
  line-height: 1;
}

.calendar-day__stages {
  font-size: clamp(0.7rem, 1.8vw, 0.9rem);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-top: 2px;
  color: var(--day--stages--color);
}

.modal-control-buttons {
  display: flex;
  flex-direction: row;
  justify-content: center;
  gap: 10px;
}

</style>
