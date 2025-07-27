<template>
  <ModalFormContainer :visible="visible" :onExit="exit">
      <div class="calendar">
        <div class="calendar-month">
          <button class="calendar-nav-button"
                  @click="navigate(NavMonthDirection.PREV)">
            <font-awesome-icon icon="fa-solid fa-angle-left"/>
          </button>
          <span>{{ formattedCurrMonth }}</span>
          <button class="calendar-nav-button"
                  @click="navigate(NavMonthDirection.NEXT)">
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
               :class="day.cssClass"
               v-for="day in monthDays"
               :key="day.date">
            <div class="calendar-day-number">
              {{ day.number }}
            </div>
            <div v-if="day.stages" class="calendar-day-stages">
              {{ day.stages }}
            </div>
          </div>
        </div>
      </div>
  </ModalFormContainer>
</template>

<script setup lang="ts">
import ModalFormContainer from '@/components/modal/ModalFormContainer.vue'
import { computed, defineEmits, ref } from 'vue'
import { StudyStatus } from '@/models/calendar.ts'
import { useGlobalStateStore } from '@/stores/global-state.ts'
import { useCalendarDataStore } from '@/stores/calendar-data.ts'
import { storeToRefs } from 'pinia'
import type { Stage } from '@/core-logic/stage-logic.ts'
import { asIsoDate } from '@/utils/date.ts';

defineProps({
  visible: Boolean
})

const emit = defineEmits([
  'update:visible',
])

const globalStateStore = useGlobalStateStore()
const calendarDataStore = useCalendarDataStore()
const { lightStartDate, currLightDay, lightDays } = storeToRefs(calendarDataStore)

// calendar>

const weekdays = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat']

const lightDayMap = ref(new Map(lightDays.value.map(day => [day.isoDate, day])))
const calendarCurrDate = ref(new Date(currLightDay.value.isoDate))

interface CalendarDay {
  number: number
  date: string
  cssClass: string
  stages: string
}

const currMonth = ref(calendarCurrDate.value)

const monthDays = computed(() => {
  const year = currMonth.value.getFullYear()
  const month = currMonth.value.getMonth()

  const firstMonthDay = new Date(year, month, 1)
  const firstWeekDay = firstMonthDay.getDay()
  const prevYear = month === 0 ? year - 1 : year
  const prevMonth = month === 0 ? 11 : month - 1
  const totalDaysInPrevMonth = totalDays(prevYear, prevMonth + 1)

  // filling empty slots at the start
  const result: CalendarDay[] = []
  for (let i = firstWeekDay - 1; i >= 0; i--) {
    const date = new Date(prevYear, prevMonth, totalDaysInPrevMonth - i)
    const lightDay = lightDayMap.value.get(asIsoDate(date))

    result.push({
      number: date.getDate(),
      date: date.toDateString(),
      cssClass: 'calendar-empty-day',
      stages: (lightDay?.stages ?? []).map(v => v.order).toString(),
    })
  }

  const totalDaysInMonth = totalDays(year, month + 1)

  // filling the current month
  for (let i = 1; i <= totalDaysInMonth; i++) {
    const date = new Date(year, month, i)
    const lightDay = lightDayMap.value.get(asIsoDate(date))
    const isStartDay = asIsoDate(date) === lightStartDate.value
    const isCurrDay = date.toDateString() === calendarCurrDate.value.toDateString()

    result.push({
      number: i,
      date: date.toDateString(),
      cssClass: getCellCssClass(isStartDay, isCurrDay, lightDay?.status ?? null),
      stages: (lightDay?.stages ?? []).map(v => v.order).toString(),
    })
  }

  const lastMonthDay = new Date(year, month + 1, 0)
  const lastWeekDay = lastMonthDay.getDay()
  const nextMonth = month === 11 ? 0 : month + 1
  const nextYear = month === 11 ? year + 1 : year

  // filling empty slots at the end
  for (let i = 1; i <= 6 - lastWeekDay; i++) {
    const date = new Date(nextYear, nextMonth, i)
    const lightDay = lightDayMap.value.get(asIsoDate(date))

    result.push({
      number: i,
      date: date.toDateString(),
      cssClass: 'calendar-empty-day',
      stages: (lightDay?.stages ?? []).map(v => v.order).toString(),
    })
  }

  return result
})

function totalDays(year: number, month: number): number {
  return new Date(year, month, 0).getDate()
}

const dateFormatter = new Intl.DateTimeFormat('en', { month: 'long', year: 'numeric' })
const formattedCurrMonth = computed(() => {
  return dateFormatter.format(currMonth.value)
})

enum NavMonthDirection { NEXT, PREV }

function navigate(direction: NavMonthDirection) {
  const newMonth = new Date(currMonth.value)
  if (direction === NavMonthDirection.PREV) {
    newMonth.setMonth(currMonth.value.getMonth() - 1)
  } else {
    newMonth.setMonth(currMonth.value.getMonth() + 1)
  }
  currMonth.value = newMonth
}

function getCellCssClass(
  isStartDay: boolean,
  isCurrDay: boolean,
  status: StudyStatus | null
): string {
  if (isStartDay) {
    return 'calendar-start-day'
  } else if (isCurrDay) {
    return 'calendar-current-day'
  }

  switch (status) {
    case StudyStatus.COMPLETED:
      return 'calendar-completed-day'
    case StudyStatus.IN_PROGRESS:
      return 'calendar-in-progress-day'
    case StudyStatus.NOT_STARTED:
      return 'calendar-not-started-day'
    default:
      return 'calendar-another-day'
  }
}

// <calendar

function exit() {
  currMonth.value = calendarCurrDate.value
  emit('update:visible', false)
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
</style>
