<template>
  <Modal
    :visible="visible"
    :on-press-exit="exit"
  >
    <div class="modal-main-area">
      <div class="calendar">
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
        <div class="calendar-dates">
          <div
            v-for="day in calendarPage"
            :key="day.date"
            class="calendar-day"
            :class="cellClasses(day)"
          >
            <div class="calendar-day-number">
              {{ day.number }}
            </div>
            <div v-if="day.isCurrMonth && day.stages !== null" class="calendar-day-stages">
              {{ day.stages }}
            </div>
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
    return 'calendar-empty-day'
  }

  const result: string[] = []
  switch (day?.status) {
    case chronodayStatuses.INITIAL:
      result.push('calendar-start-day')
      break
    case chronodayStatuses.COMPLETED:
      result.push('calendar-completed-day')
      break
    case chronodayStatuses.IN_PROGRESS:
      result.push('calendar-in-progress-day')
      break
    case chronodayStatuses.NOT_STARTED:
      result.push('calendar-not-started-day')
      break
    case chronodayStatuses.OFF:
      result.push('calendar-off-day')
      break
    default:
      result.push('calendar-another-day')
      break
  }

  if (day.isCurrDay) {
    result.push('calendar-current-day')
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
.calendar {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 2px;
}

.calendar-month {
  grid-column: span 7;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5em;
  font-size: 1.2em;
}

.calendar-weekdays, .calendar-dates {
  display: contents;
}

.calendar-day {
  display: flex;
  flex-direction: column;
  border-radius: 5px;
  padding: 1em;
  text-align: center;
  min-width: 2em;
  min-height: 2em;
}

.calendar-empty-day {
  background: none;
}

.calendar-start-day {
  background-color: #ac7224;
}

.calendar-completed-day {
  background-color: #9bbf99;
}

.calendar-in-progress-day {
  background-color: #7188bc;
}

.calendar-not-started-day {
  background-color: #a5a5a5;
}

.calendar-current-day {
  border-color: #884393;
  border-style: solid;
  border-width: 1px;
}

.calendar-off-day {
  background-color: #43938a;
}

.calendar-another-day {
  background-color: #e8e8e8;
}

.calendar-weekday {
  display: flex;
  flex-direction: column;
  padding: 0.5em;
  text-align: center;
  min-width: 2em;
  min-height: 1em;
}

.calendar-day-number {
  font-weight: bold;
  font-size: 1em;
}

.calendar-day-stages {
  font-size: 0.8em;
}

.modal-control-buttons {
  display: flex;
  flex-direction: row;
  justify-content: center;
  gap: 10px;
}
</style>
