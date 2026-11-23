<template>
  <ReviewSummary
    :title="title"
    :with-shadow="!outerSpace"
    :class="{ 'special-summary--outer-space': outerSpace }"
  >
    <CometBorder v-if="outerSpace" />
    <div class="special-summary">
      <div class="special-clock">
        <Clock :time="elapsedTime" top-label="Elapsed" :with-shadow="!outerSpace" />
      </div>
      <div class="special-chart-wrapper">
        <div :class="['special-chart', { 'special-chart--shadow': !outerSpace }]">
          <DoughnutChart
            :pieces="pieces"
            :segmented="outerSpace"
            :segment-gap="2"
            :segment-count="20"
          />
        </div>
      </div>
      <div :class="onBack ? 'special-left-button' : 'special-central-button'">
        <SmartButton
          text="Finish Review"
          class="off-button"
          :on-click="onFinish"
          fill-width
          auto-blur
        />
      </div>
      <div v-if="onBack" class="special-right-button">
        <SmartButton text="Continue" class="calm-button" :on-click="onBack" fill-width auto-blur />
      </div>
    </div>
  </ReviewSummary>
</template>

<script setup lang="ts">
import CometBorder from '@/components/common/CometBorder.vue'
import DoughnutChart, { type DoughnutPiece } from '@/components/common/DoughnutChart.vue'
import SmartButton from '@/components/common/SmartButton.vue'
import Clock from '@/components/common/Clock.vue'
import ReviewSummary from '@/components/review/summary/ReviewSummary.vue'
import { deckEmptyMessage, deckNotEmptyMessage } from '@/core-logic/review-logic.ts'
import { computed } from 'vue'

const props = withDefaults(
  defineProps<{
    outerSpace?: boolean
    total: number
    viewed: number
    elapsedTime: number
    completed?: boolean
    onBack?: () => void
    onFinish?: () => void
  }>(),
  {
    outerSpace: false,
    completed: false,
    onBack: undefined,
    onFinish: () => {},
  },
)

const title = computed(() => (props.completed ? deckEmptyMessage() : deckNotEmptyMessage()))

const pieces = computed<DoughnutPiece[]>(() => [
  {
    value: props.viewed,
    legend: 'Viewed',
    color: props.outerSpace ? '#8b5cf6' : '#3b82f6',
  },
  {
    value: props.total - props.viewed,
    legend: 'Remaining',
    color: props.outerSpace ? 'rgba(255, 255, 255, 0.25)' : 'rgb(225, 228, 240)',
    liftOnHover: false,
  },
])
</script>

<style scoped>
.special-summary {
  width: 100%;
  height: auto;
  display: grid;
  grid-template-rows: 1fr auto;
  grid-template-columns: repeat(4, 1fr);
  align-items: start;
  gap: clamp(4px, 1.5cqw, 8px);
  padding: clamp(8px, 1.2cqw, 12px);
  container-type: inline-size;
}

.special-summary--outer-space {
  --review-summary--title--color: #ffffff;
  --stopwatch--text--color: #ffffff;
  --stopwatch--label--color: rgba(255, 255, 255, 0.8);
  --stopwatch--separator--color: rgba(255, 255, 255, 0.3);
  --doughnut-chart--text--color: rgba(255, 255, 255, 0.8);
  --doughnut-chart--total--color: #ffffff;
  --special-summary--viewed--color: #8b5cf6;
  --special-summary--unviewed--color: rgba(255, 255, 255, 0.25);
  --calm-button--bg-color: #6d28d9;
  --calm-button--bg-color--hover: #7c3aed;
  --calm-button--bg-color--loading: #a78bfa;
}

.special-clock {
  grid-column: 1 / 3;
  align-self: center;
  display: flex;
  justify-content: center;
  --stopwatch--bg: none;
}

.special-chart-wrapper {
  grid-column: 3 / -1;
  width: 100%;
  height: 100%;
}

.special-chart {
  width: 100%;
  height: 100%;
  aspect-ratio: 1 / 1;
  border-radius: 6px;
  padding: clamp(4px, 1.8cqw, 10px);
}

.special-chart--shadow {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.special-left-button {
  margin-top: 4px;
  grid-column: 1 / 3;
}

.special-central-button {
  margin-top: 4px;
  grid-column: 1 / -1;
}

.special-right-button {
  margin-top: 4px;
  grid-column: 3 / -1;
}
</style>
