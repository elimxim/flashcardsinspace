<template>
  <div class="day-streak-widget">
    <div class="day-streak select-none">
      <div class="day-steak-number">
        {{ dayStreak }}
      </div>
      <div class="control-panel--widget--text control-panel--widget--text--active">
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
  border: 1px solid var(--main-panel--widget--border-color);
  background: var(--main-panel--widget--color--active);
  height: 100%;
}

.day-steak-number {
  margin-top: 5px;
  color: var(--main-panel--widget--text--color--active);
  font-family: var(--day-streak--font-family);
  font-weight: 600;
  font-variant-numeric: tabular-nums lining-nums;
  font-size: 3rem;
  white-space: nowrap;
  text-align: center;
  line-height: 1;
}

</style>
