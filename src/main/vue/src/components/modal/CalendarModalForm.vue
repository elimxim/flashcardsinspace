<template>
  <ModalForm :visible="visible" :onExit="exit">
    <div class="calendar">
      <div class="calendar-month">
        <button class="calendar-nav-button"
                ref="prevMonthButton"
                @click="navigate(MonthNav.PREV)">
          <font-awesome-icon icon="fa-solid fa-angle-left"/>
        </button>
        <span>{{ formattedCurrMonth }}</span>
        <button class="calendar-nav-button"
                ref="nextMonthButton"
                @click="navigate(MonthNav.NEXT)">
          <font-awesome-icon icon="fa-solid fa-angle-right"/>
        </button>
      </div>
      <div class="calendar-weekdays">
        <div class="calendar-weekday"
             v-for="day in weekdays"
             :key="day">
          {{ day }}
        </div>
      </div>
      <div class="calendar-dates">
        <div class="calendar-day"
             :class="cellClass(day)"
             v-for="day in calendarPage"
             :key="day.date">
          <div class="calendar-day-number">
            {{ day.number }}
          </div>
          <div v-if="day.isCurrMonth && day.stages !== null" class="calendar-day-stages">
            {{ day.stages }}
          </div>
        </div>
      </div>
    </div>
    <div class="calendar-bottom-nav">
      <button class="modal-button modal-danger-button"
              :class="{ 'modal-button-disabled': isTimelineSuspended }"
              ref="updateButton"
              :disabled="isTimelineSuspended"
              @click="switchToPrevDay">
        Prev
      </button>
      <button class="modal-button modal-danger-button"
              :class="{ 'modal-button-disabled': isTimelineSuspended }"
              ref="updateButton"
              :disabled="isTimelineSuspended"
              @click="switchToNextDay">
        Next
      </button>
    </div>
  </ModalForm>
</template>

<script setup lang="ts">
import '@/assets/modal.css'
import ModalForm from '@/components/modal/ModalForm.vue'
import { computed, type ComputedRef, defineEmits, onMounted, onUnmounted, ref, watch } from 'vue'
import { useTimelineStore } from '@/stores/timeline-store.ts'
import { storeToRefs } from 'pinia'
import {
  calcCalendarPage,
  type CalendarDay,
  chronodayStatuses
} from '@/core-logic/calendar-logic.ts'

defineProps({
  visible: Boolean
})

const emit = defineEmits([
  'update:visible',
])

const timelineStore = useTimelineStore()
const { timeline, chronodays, currDay } = storeToRefs(timelineStore)

// calendar>

const weekdays = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat']

const chronodayMap = computed(() => new Map(chronodays.value.map(day => [day.chronodate, day])))
const currMonth = ref(new Date(currDay.value.chronodate))

watch(currDay, (newValue) => {
  currMonth.value = new Date(newValue.chronodate)
})

const calendarPage = computed(() =>
  calcCalendarPage(currMonth.value, currDay.value, chronodayMap.value)
)

const userDateFormatter = new Intl.DateTimeFormat('en', { month: 'long', year: 'numeric' })
const formattedCurrMonth = computed(() => {
  return userDateFormatter.format(currMonth.value)
})

enum MonthNav { NEXT, PREV }

function navigate(direction: MonthNav) {
  const newMonth = new Date(currMonth.value)
  if (direction === MonthNav.PREV) {
    newMonth.setMonth(currMonth.value.getMonth() - 1)
  } else {
    newMonth.setMonth(currMonth.value.getMonth() + 1)
  }
  currMonth.value = newMonth
}

function cellClass(day: CalendarDay): string {
  switch (day?.status) {
    case chronodayStatuses.INITIAL:
      return 'calendar-start-day'
    case chronodayStatuses.COMPLETED:
      return 'calendar-completed-day'
    case chronodayStatuses.IN_PROGRESS:
      return 'calendar-in-progress-day'
    case chronodayStatuses.NOT_STARTED:
      return 'calendar-not-started-day'
    case chronodayStatuses.OFF:
      return 'calendar-off-day'
  }

  if (!day.isCurrMonth) {
    return 'calendar-empty-day'
  } else if (day.isCurrDay) {
    return 'calendar-current-day'
  } else {
    return 'calendar-another-day'
  }
}

// <calendar

function exit() {
  emit('update:visible', false)
  currMonth.value = new Date(currDay.value.chronodate)
}

function switchToNextDay() {
  timelineStore.switchToNextDay()
  currMonth.value = new Date(currDay.value.chronodate)
}

function switchToPrevDay() {
  timelineStore.switchToPrevDay()
  currMonth.value = new Date(currDay.value.chronodate)
}

function isTimelineSuspended(): boolean {
  return timeline !== null && timeline.value.status === 'SUSPENDED'
}

const prevMonthButton = ref<HTMLButtonElement>()
const nextMonthButton = ref<HTMLButtonElement>()

onMounted(() => {
  document.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown)
})

function handleKeydown(event: KeyboardEvent) {
  if (event.key === 'ArrowLeft') {
    prevMonthButton.value?.click()
  } else if (event.key === 'ArrowRight') {
    nextMonthButton.value?.click()
  }
}

</script>

<style scoped>
.calendar {
  flex: 100;
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

.calendar-nav-button {
  color: #ccc;
  background: none;
  border: none;
  cursor: pointer;
  font-size: 1em;
  outline: none;
}

.calendar-nav-button:hover {
  color: #535353;
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
  background-color: #884393;
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

.calendar-bottom-nav {
  flex: 1;
  display: flex;
  justify-content: center;
  gap: 0.5em;
  align-items: center;
  margin-top: 1em;
}
</style>
