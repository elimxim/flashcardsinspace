<template>
  <div class="stopwatch stopwatch--theme">
    <template v-if="clockStyle === 'classic'">
      <div class="classic-display">
        <div class="classic-time">
          <span v-if="moreThanOneHour || showHours" class="classic-segment">{{ hours }}</span>
          <span v-if="moreThanOneHour || showHours" class="classic-colon">:</span>
          <span class="classic-segment">{{ minutes }}</span>
          <span class="classic-colon">:</span>
          <span class="classic-segment">{{ seconds }}</span>
        </div>
        <div class="classic-label">ELAPSED</div>
      </div>
    </template>
    <template v-else>
      <div class="modern-display">
        <div class="modern-elapsed-label">ELAPSED</div>
        <div class="modern-time">
          <div v-if="moreThanOneHour || showHours" class="modern-segment-group">
            <span class="modern-value">{{ hours }}</span>
            <span class="modern-label">HRS</span>
          </div>
          <span v-if="moreThanOneHour || showHours" class="modern-separator"/>
          <div class="modern-segment-group">
            <span class="modern-value">{{ minutes }}</span>
            <span class="modern-label">MIN</span>
          </div>
          <span class="modern-separator"/>
          <div class="modern-segment-group">
            <span class="modern-value">{{ seconds }}</span>
            <span class="modern-label">SEC</span>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  time?: number
  clockStyle?: 'classic' | 'modern'
  showHours?: boolean
}>(), {
  time: 0,
  clockStyle: 'modern',
  showHours: false,
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
  --sw--bg: var(--stopwatch--bg, none);
  --sw--classic--bg: var(--stopwatch--classic--bg, #f5f0e6);
  --sw--classic--border--color: var(--stopwatch--classic--border--color, #8b7355);
  --sw--classic--text--color: var(--stopwatch--classic--text--color, #2c1810);
  --sw--modern--bg: var(--stopwatch--modern--bg, #ffffff);
  --sw--modern--text-color: var(--stopwatch--modern--text--color, #1a1a2e);
  --sw--modern--label--color: var(--stopwatch--modern--label--color, #9fa4b6);
  --sw--modern--separator--color: var(--stopwatch--modern--separator--color, #e1e4f0);
}

.stopwatch {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: auto;
  background: var(--sw--bg);
  container-type: inline-size;
}

.classic-display {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: clamp(0.3rem, 50cqmin, 1.5rem);
  gap: clamp(0.1rem, 2cqmin, 0.5rem);
  background: var(--sw--classic--bg);
  border: clamp(1px, 1cqmin, 3px) solid var(--sw--classic--border--color);
  border-radius: 50%;
  aspect-ratio: 1 / 1;
  box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.1),
  0 4px 8px rgba(0, 0, 0, 0.15),
  0 0 0 clamp(1px, 1.5cqmin, 4px) rgba(139, 115, 85, 0.3);
}

.classic-time {
  display: flex;
  align-items: center;
  gap: 0.05em;
}

.classic-segment {
  display: inline-block;
  width: 2ch;
  text-align: center;
  font-size: clamp(0.8rem, 12cqmin, 2.5rem);
  font-weight: 700;
  color: var(--sw--classic--text--color);
  font-family: 'Georgia', serif;
}

.classic-colon {
  display: inline-block;
  width: 0.5ch;
  text-align: center;
  font-size: clamp(0.8rem, 12cqmin, 2.5rem);
  font-weight: 700;
  color: var(--sw--classic--text--color);
  font-family: 'Georgia', serif;
}

.classic-label {
  font-size: clamp(0.5rem, 5cqmin, 0.75rem);
  font-weight: 600;
  letter-spacing: 0.2em;
  color: var(--sw--classic--border--color);
  text-transform: uppercase;
}

.modern-display {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: clamp(2px, 3cqi, 8px);
  padding: clamp(4px, 6cqi, 14px) clamp(6px, 8cqi, 18px);
  background: var(--sw--modern--bg);
  border-radius: clamp(4px, 6cqi, 14px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  width: 100%;
  height: fit-content;
}

.modern-time {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: clamp(4px, 5cqi, 12px);
  width: 100%;
}

.modern-segment-group {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: clamp(0.05rem, 1cqi, 0.2rem);
  flex: 1;
  container-type: inline-size;
}

.modern-value {
  font-size: clamp(0.75rem, 50cqi, 3rem);
  font-weight: 300;
  color: var(--sw--modern--text-color);
  font-family: 'Helvetica Neue', Arial, sans-serif;
  line-height: 1;
}

.modern-label {
  font-size: clamp(0.5rem, 14cqi, 0.75rem);
  font-weight: 400;
  letter-spacing: 0.1em;
  color: var(--sw--modern--label--color);
}

.modern-separator {
  width: 1px;
  height: clamp(0.75rem, 18cqi, 2.5rem);
  background: var(--sw--modern--separator--color);
}

.modern-elapsed-label {
  font-size: clamp(0.5rem, 5cqi, 0.75rem);
  font-weight: 400;
  letter-spacing: 0.1em;
  color: var(--sw--modern--label--color);
}
</style>

