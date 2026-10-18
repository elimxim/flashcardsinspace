<template>
  <ReviewSummary :title="title">
    <div class="special-summary">
      <div class="special-clock">
        <Clock :time="elapsedTime" top-label="Elapsed" />
      </div>
      <div class="special-chart-wrapper">
        <div class="special-chart">
          <DoughnutChart :total="total" :left="total - viewed" :show-legend="false" />
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
        <SmartButton text="Back" class="calm-button" :on-click="onBack" fill-width auto-blur />
      </div>
    </div>
  </ReviewSummary>
</template>

<script setup lang="ts">
import DoughnutChart from '@/components/common/DoughnutChart.vue'
import SmartButton from '@/components/common/SmartButton.vue'
import Clock from '@/components/common/Clock.vue'
import ReviewSummary from '@/components/review/summary/ReviewSummary.vue'
import { deckEmptyMessage } from '@/core-logic/review-logic.ts'

withDefaults(
  defineProps<{
    total: number
    viewed: number
    elapsedTime: number
    onBack?: () => void
    onFinish?: () => void
  }>(),
  {
    onBack: undefined,
    onFinish: () => {},
  },
)

const title = deckEmptyMessage()
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
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border-radius: 6px;
  padding: clamp(4px, 1.8cqw, 10px);
  --doughnut-chart--arc--right--color: #3b82f6;
  --doughnut-chart--arc--left--color: rgb(225, 228, 240);
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
