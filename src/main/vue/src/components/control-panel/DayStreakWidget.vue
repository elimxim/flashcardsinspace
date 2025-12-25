<template>
  <div class="day-streak-widget day-streak-widget--theme">
    <div class="day-streak select-none">
      <div class="day-steak-number">
        {{ dayStreak }}
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

const chronoStore = useChronoStore()
const { dayStreak } = storeToRefs(chronoStore)

</script>

<style scoped>
.day-streak-widget--theme {
  --d-widget--font-family: var(--day-streak--font-family);
  --d-widget--number--color: var(--day-streak-widget--number--color, rgba(255, 255, 255, 0.6));
  --d-widget--text--color: var(--day-streak-widget--text--color, rgba(255, 255, 255, 0.6));
  --d-widget--bg: var(--day-streak-widget--bg, linear-gradient(135deg, rgb(142, 110, 189) 0%, rgb(215, 91, 209) 100%));
  --d-widget--border-color: var(--day-streak-widget--border-color, rgba(128, 128, 128, 0.62));
}

.day-streak-widget {
  position: relative;
  display: flex;
  height: 100%;
  width: fit-content;
}

.day-streak {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 8px;
  padding: 4px;
  border-radius: 6px;
  border: 1px solid var(--d-widget--border-color);
  background: var(--d-widget--bg);
  height: 100%;
}

.day-steak-number {
  margin-top: 5px;
  color: var(--d-widget--number--color);
  font-family: var(--d-widget--font-family);
  font-weight: 600;
  font-variant-numeric: tabular-nums lining-nums;
  font-size: 3rem;
  white-space: nowrap;
  text-align: center;
  line-height: 1;
}

.day-streak-text {
  color: var(--d-widget--text--color);
  font-weight: 600;
  font-size: 0.9rem;
  word-spacing: 0.05rem;
  letter-spacing: 0.05rem;
  text-transform: uppercase;
  text-align: center;
}
</style>
