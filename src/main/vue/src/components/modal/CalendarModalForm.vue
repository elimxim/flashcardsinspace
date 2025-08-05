<template>
  <ModalForm :visible="visible" :onExit="exit">
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
             :class="cellClass(day)"
             v-for="day in monthDays"
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
      <button class="modal-button modal-update-button"
              ref="updateButton"
              @click="switchCalendarDay">
        Switch day
      </button>
    </div>
  </ModalForm>
</template>

<script setup lang="ts">
import '@/assets/modal.css'
import ModalForm from '@/components/modal/ModalForm.vue'
import { computed, defineEmits, ref } from 'vue'
import { StudyStatus } from '@/model/calendar.ts'
import { useCalendarStore } from '@/stores/calendar-store.ts'
import { storeToRefs } from 'pinia'
import { asIsoDate } from '@/utils/date.ts'

defineProps({
  visible: Boolean
})

const emit = defineEmits([
  'update:visible',
])

const calendarStore = useCalendarStore()
const { lightStartDate, currLightDay, lightDays } = storeToRefs(calendarStore)

// calendar>

const weekdays = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat']

const lightDayMap = ref(new Map(lightDays.value.map(day => [day.isoDate, day])))
const currDate = ref(new Date(currLightDay.value.isoDate))

interface CalendarDay {
  number: number
  date: string
  status: StudyStatus | null
  stages: string | null
  isCurrMonth: boolean
  isStartDay: boolean
  isCurrDay: boolean
}

const monthDays = computed(() => {
  const year = currDate.value.getFullYear()
  const month = currDate.value.getMonth()

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
      status: null,
      stages: lightDay?.stages.map(v => v.order).toString() ?? null,
      isCurrMonth: false,
      isStartDay: false,
      isCurrDay: false,
    })
  }

  const totalDaysInMonth = totalDays(year, month + 1)

  // filling the current month
  for (let i = 1; i <= totalDaysInMonth; i++) {
    const date = new Date(year, month, i)
    const lightDay = lightDayMap.value.get(asIsoDate(date))

    result.push({
      number: i,
      date: date.toDateString(),
      status: lightDay?.status ?? null,
      stages: lightDay?.stages.map(v => v.order).toString() ?? null,
      isCurrMonth: true,
      isStartDay: asIsoDate(date) === lightStartDate.value,
      isCurrDay: date.toDateString() === currDate.value.toDateString(),
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
      status: null,
      stages: lightDay?.stages.map(v => v.order).toString() ?? null,
      isCurrMonth: false,
      isStartDay: false,
      isCurrDay: false,
    })
  }

  return result
})

function totalDays(year: number, month: number): number {
  return new Date(year, month, 0).getDate()
}

const dateFormatter = new Intl.DateTimeFormat('en', { month: 'long', year: 'numeric' })
const formattedCurrMonth = computed(() => {
  return dateFormatter.format(currDate.value)
})

enum NavMonthDirection { NEXT, PREV }

function navigate(direction: NavMonthDirection) {
  const newMonth = new Date(currDate.value)
  if (direction === NavMonthDirection.PREV) {
    newMonth.setMonth(currDate.value.getMonth() - 1)
  } else {
    newMonth.setMonth(currDate.value.getMonth() + 1)
  }
  currDate.value = newMonth
}

function cellClass(day: CalendarDay): string {
  if (!day.isCurrMonth) {
    return 'calendar-empty-day'
  } if (day.isStartDay) {
    return 'calendar-start-day'
  } else if (day.isCurrDay) {
    return 'calendar-current-day'
  }

  switch (day?.status) {
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
  emit('update:visible', false)
}

function switchCalendarDay() {
  calendarStore.switchLightDay()
  currDate.value = new Date(currLightDay.value.isoDate)
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

.calendar-bottom-nav {
  flex: 1;
  display: flex;
  justify-content: right;
  align-items: center;
  margin-top: 1em;
}
</style>
