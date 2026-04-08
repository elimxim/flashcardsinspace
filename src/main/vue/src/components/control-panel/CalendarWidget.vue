<template>
  <div class="calendar-widget">
    <AwesomeButton
      :icon="calendarIcon"
      class="cp-widget"
      :disabled="!flashcardSet"
      :on-click="toggleStore.toggleCalendar"
      fill-space
      square
    >
      <template #below>
        <div class="calendar-button-slot">
          <div class="cp-text">
            Day
          </div>
          <div class="cp-count-box">
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
      v-if="isUserCloseToLoseDayStreak"
      class="calendar-info-button"
      icon="fa-solid fa-circle-exclamation"
      :on-hover="togglePreviousDaysPopup"
    />
    <transition name="slide-fade">
      <div
        v-if="isUserCloseToLoseDayStreak && showPreviousDaysPopup"
        class="calendar-popup"
      >
        <div class="calendar-popup-layout">
          <div class="cp-text cp-text--sub cp-text--nowrap">
            Don't forget to do your review
          </div>
          <div class="cp-text cp-text--sub">
            You just have {{ timeBeforeMidnight }} before the midnight
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import AwesomeButton from '@/components/AwesomeButton.vue'
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useFlashcardStore } from '@/stores/flashcard-store.ts'
import { useChronoStore } from '@/stores/chrono-store.ts'
import { useToggleStore } from '@/stores/toggle-store.ts'
import { storeToRefs } from 'pinia'
import { chronodayStatuses } from '@/core-logic/chrono-logic.ts'
import { tomorrowMidnight } from '@/utils/utils.ts'

const flashcardStore = useFlashcardStore()
const chronoStore = useChronoStore()
const toggleStore = useToggleStore()

const { flashcardSet, isSuspended } = storeToRefs(flashcardStore)
const { currDay, isDayOff } = storeToRefs(chronoStore)

const showPreviousDaysPopup = ref(false)
const minutesBeforeMidnight = ref(-1)
let alarmInterval: ReturnType<typeof setInterval> | null = null

const togglePreviousDaysPopup = () => {
  showPreviousDaysPopup.value = !showPreviousDaysPopup.value
}

const isOnVacation = computed(() => isSuspended.value || isDayOff.value)
const currDayNumber = computed(() =>
  isOnVacation.value ? '🌴' : currDay.value?.seqNumber ?? '?'
)

const isUserCloseToLoseDayStreak = computed(() => {
  if (currDay.value.status === chronodayStatuses.COMPLETED) {
    return false
  } else {
    const minutes = minutesBeforeMidnight.value
    return minutes > 0 && minutes < 6 * 60 // 6 hours
  }
})

const timeBeforeMidnight = computed(() => {
  const minutes = minutesBeforeMidnight.value
  if (minutes > 60) {
    const hours = Math.floor(minutes / 60)
    return `${hours} hours`
  } else {
    return `${minutes} minutes`
  }
})

function calcMinutesBeforeMidnight() {
  const now = new Date()
  const tomorrow = tomorrowMidnight(now)
  minutesBeforeMidnight.value = (tomorrow.getTime() - now.getTime()) / (60 * 1000)
}

const calendarIcon = computed(() => {
  if (isOnVacation.value || currDay.value.status === chronodayStatuses.INITIAL) {
    return 'fa-solid fa-calendar'
  } else if (currDay.value.status === chronodayStatuses.COMPLETED) {
    return 'fa-solid fa-calendar-check'
  } else {
    return 'fa-solid fa-calendar-days'
  }
})

onMounted(() => {
  calcMinutesBeforeMidnight()
  alarmInterval = setInterval(calcMinutesBeforeMidnight, 60000)
})

onUnmounted(() => {
  if (alarmInterval) {
    clearInterval(alarmInterval)
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
  top: 35%;
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
