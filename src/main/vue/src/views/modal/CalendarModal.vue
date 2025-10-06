<template>
  <Modal
    :visible="modalStore.calendarOpen"
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
          :class="dayCssClasses(day)"
        >
          <div class="calendar-day__top">
            <div class="calendar-day__top__number">
              {{ day.number }}
            </div>
            <div v-if="canShowSeq(day)" class="calendar-day__top__seq">
              {{ day.seqNumber }}
            </div>
          </div>
          <div v-if="canShowStages(day)" class="calendar-day__stages">
            {{ day.stages }}
          </div>
        </div>
      </div>
    </div>
    <div class="modal-control-buttons">
      <SmartButton
        class="calendar-button"
        text="Prev"
        :on-click="goPrevDay"
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
  <SpaceToast/>
</template>

<script setup lang="ts">
import Modal from '@/components/Modal.vue'
import SmartButton from '@/components/SmartButton.vue'
import AwesomeButton from '@/components/AwesomeButton.vue'
import SpaceToast from '@/components/SpaceToast.vue'
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useChronoStore } from '@/stores/chrono-store.ts'
import { storeToRefs } from 'pinia'
import {
  calcCalendarPage,
  type CalendarDay,
  chronodayStatuses
} from '@/core-logic/chrono-logic.ts'
import { useFlashcardSetStore } from '@/stores/flashcard-set-store.ts'
import { useModalStore } from '@/stores/modal-store.ts'
import { sendChronoSyncNextDay, sendChronoSyncPrevDay } from '@/api/api-client.ts'
import { useSpaceToaster } from '@/stores/toast-store.ts';

const modalStore = useModalStore()
const toaster = useSpaceToaster()
const chronoStore = useChronoStore()
const flashcardSetStore = useFlashcardSetStore()

const {
  flashcardSet,
  isStarted,
  isSuspended,
} = storeToRefs(flashcardSetStore)
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
  return day.isCurrMonth && day.seqNumber !== null && day.seqNumber !== 0
}

function canShowStages(day: CalendarDay): boolean {
  return day.isCurrMonth && day.stages !== null
}

function exit() {
  currMonth.value = new Date(currDay.value.chronodate)
  modalStore.toggleCalendar()
}

async function goPrevDay() {
  if (!flashcardSet.value) return
  if (!chronoStore.canGoPrev()) return
  await sendChronoSyncPrevDay(flashcardSet.value.id)
    .then((response) => {
      chronoStore.loadState(
        response.data.chronodays,
        response.data.currDay,
      )
      currMonth.value = new Date(response.data.currDay.chronodate)
    })
    .catch((error) => {
      console.error('Failed to sync prev day:', error)
      toaster.bakeError(`Couldn't go to prev day`, error.response?.data)
    })
}

async function switchToNextDay() {
  if (!flashcardSet.value) return
  if (!chronoStore.canGoNext()) return
  await sendChronoSyncNextDay(flashcardSet.value.id)
    .then((response) => {
      chronoStore.loadState(
        response.data.chronodays,
        response.data.currDay,
      )
      currMonth.value = new Date(response.data.currDay.chronodate)
    })
    .catch((error) => {
      console.error('Failed to sync next day:', error)
      toaster.bakeError(`Couldn't go to next day`, error.response?.data)
    })
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
  --day--off--bg-color: var(--calendar--day--off--bg-color, #43938a);
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
  right: 6px;
  bottom: 4px;
  font-size: 0.8rem;
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
  background-color: var(--day--off--bg-color);
}

.calendar-day:hover:not(.calendar-day--another) {
  transform: scale(1.08);
}

.calendar-day__top {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  gap: 6px;
}

.calendar-day__top__number {
  font-weight: 800;
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
  font-weight: 550;
  font-size: clamp(0.7rem, 1.8vw, 0.8rem);
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
