<template>
  <div v-if="!isInitialDay" class="day-streak-panel day-streak-panel--theme">
    <div class="day-streak select-none">
      <div class="day-steak-number">
        {{ dayStreakNumber }}
      </div>
      <div class="day-streak-text">
        Day Streak
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useChronoStore } from '@/stores/chrono-store.ts'
import { storeToRefs } from 'pinia'
import { computed } from 'vue';
import { chronodayStatuses } from '@/core-logic/chrono-logic.ts'

const chronoStore = useChronoStore()
const {
  chronodays,
  currDay,
  isInitialDay,
} = storeToRefs(chronoStore)

const dayStreakNumber = computed(() => {
  if (!currDay.value) return 0

  const days = chronodays.value
  const startDay = currDay.value

  const startIndex = days.findIndex(day => day.id === startDay.id)
  if (startIndex === -1) return 0

  let counter = 0
  let startedCounting = false
  for (let i = startIndex; i >= 0; i--) {
    if (!startedCounting) {
      if (days[i].status === chronodayStatuses.COMPLETED) {
        startedCounting = true
      } else if (days[i].status === chronodayStatuses.OFF) {
        break
      }
    } else {
      if (days[i].status === chronodayStatuses.COMPLETED) {
        counter++
      } else {
        break
      }
    }
  }

  return counter
})

</script>

<style scoped>
.day-streak-panel--theme {
  --s-panel--number--color: var(--day-streak-panel--number--color, rgba(233, 238, 255, 0.92));
  --s-panel--text--color: var(--day-streak-panel--text--color, rgba(251, 233, 255, 0.88));
  --s-panel--bg: var(--day-streak-panel--bg, linear-gradient(135deg, rgba(131, 34, 234, 0.66) 0%, rgba(159, 25, 101, 0.68) 100%));
  --s-panel--border-color: var(--day-streak-panel--border-color, rgba(128, 128, 128, 0.62));
}

.day-streak-panel {
  position: relative;
  display: flex;
  padding: 1px;
  width: fit-content;
  height: 100%;
}

.day-streak {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 2px;
  border-radius: 6px;
  border: 1px solid var(--s-panel--border-color);
  background: var(--s-panel--bg);
  padding: 4px;
  font-size: 0.8rem;
  height: 100%;
}

.day-steak-number {
  color: var(--s-panel--number--color);
  font-weight: 800;
  font-family: var(--day-streak--font-family);
  font-variant-numeric: tabular-nums lining-nums;
  font-size: 3.4rem;
  white-space: nowrap;
  text-align: center;
  letter-spacing: 0.04em;
  line-height: 1.1;
}

.day-streak-text {
  font-size: 0.6rem;
  font-weight: 600;
  color: var(--s-panel--text--color);
  text-transform: uppercase;
  letter-spacing: 0.04em;
  text-align: center;
}
</style>
