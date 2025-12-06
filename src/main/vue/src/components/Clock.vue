<template>
  <div class="stopwatch stopwatch--theme">
    <div class="rectangle-display">
      <div v-if="topLabel" class="clock-top-label">{{ topLabel }}</div>
      <div class="clock-time">
        <div v-if="moreThanOneHour || alwaysShowHours" class="clock-segment-group">
          <span class="clock-segment-value">{{ hours }}</span>
          <span class="clock-segment-label">HRS</span>
        </div>
        <span v-if="moreThanOneHour || alwaysShowHours" class="clock-segment-partition"/>
        <div class="clock-segment-group">
          <span class="clock-segment-value">{{ minutes }}</span>
          <span class="clock-segment-label">MIN</span>
        </div>
        <span class="clock-segment-partition"/>
        <div class="clock-segment-group">
          <span class="clock-segment-value">{{ seconds }}</span>
          <span class="clock-segment-label">SEC</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  time?: number
  topLabel?: string
  alwaysShowHours?: boolean
}>(), {
  time: 0,
  topLabel: undefined,
  alwaysShowHours: false,
})

const moreThanOneHour = computed(() => props.time > 3600000)

const hours = computed(() => {
  const h = Math.floor(props.time / 3600000)
  return h.toString().padStart(2, '0')
})

const minutes = computed(() => {
  const m = Math.floor((props.time % 3600000) / 60000)
  return m.toString().padStart(2, '0')
})

const seconds = computed(() => {
  const s = Math.floor((props.time % 60000) / 1000)
  return s.toString().padStart(2, '0')
})

</script>

<style scoped>
.stopwatch--theme {
  --sw--bg: var(--stopwatch--bg, #ffffff);
  --sw--text--color: var(--stopwatch--text--color, #1a1a2e);
  --sw--label--color: var(--stopwatch--label--color, #9fa4b6);
  --sw--separator--color: var(--stopwatch--separator--color, #e1e4f0);
}

.stopwatch {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: auto;
  container-type: inline-size;
}

.rectangle-display {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: clamp(2px, 3cqi, 8px);
  padding: clamp(4px, 6cqi, 14px) clamp(6px, 8cqi, 18px);
  background: var(--sw--bg);
  border-radius: clamp(4px, 6cqi, 14px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  width: 100%;
  height: fit-content;
}

.clock-time {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: clamp(4px, 5cqi, 12px);
  width: 100%;
}

.clock-segment-group {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: clamp(0.05rem, 1cqi, 0.2rem);
  flex: 1;
  container-type: inline-size;
}

.clock-segment-value {
  font-size: clamp(0.75rem, 50cqi, 3rem);
  font-weight: 300;
  color: var(--sw--text--color);
  font-family: 'Helvetica Neue', Arial, sans-serif;
  line-height: 1;
}

.clock-segment-label {
  font-size: clamp(0.5rem, 14cqi, 0.75rem);
  font-weight: 400;
  letter-spacing: 0.1em;
  color: var(--sw--label--color);
}

.clock-segment-partition {
  width: 1px;
  height: clamp(0.75rem, 18cqi, 2.5rem);
  background: var(--sw--separator--color);
}

.clock-top-label {
  font-size: clamp(0.5rem, 5cqi, 0.75rem);
  font-weight: 400;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: var(--sw--label--color);
}
</style>

