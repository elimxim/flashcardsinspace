<template>
  <ReviewSummary :title="title" with-shadow>
    <div class="lightspeed-summary">
      <div class="lightspeed-info">
        <div class="lightspeed-clock">
          <Clock :time="elapsedTime" top-label="Elapsed" with-shadow />
        </div>
        <SummaryAnswerStats
          class="lightspeed-stats"
          title="Overall"
          :correct="correct"
          :incorrect="incorrect"
          :total="total"
        />
      </div>
      <div class="lightspeed-chart-wrapper">
        <div class="lightspeed-chart">
          <DoughnutChart
            :right="correct"
            :left="incorrect"
            legend-left="Incorrect"
            legend-right="Correct"
          />
        </div>
      </div>
      <div :class="onBack ? 'lightspeed-left-button' : 'lightspeed-central-button'">
        <SmartButton
          text="Finish Review"
          class="off-button"
          :on-click="onFinish"
          fill-width
          auto-blur
        />
      </div>
      <div v-if="onBack" class="lightspeed-right-button">
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
import SummaryAnswerStats from '@/components/review/summary/SummaryAnswerStats.vue'
import { deckEmptyMessage, deckNotEmptyMessage } from '@/core-logic/review-logic.ts'
import { computed } from 'vue'

const props = withDefaults(
  defineProps<{
    correct: number
    incorrect: number
    total: number
    elapsedTime: number
    completed?: boolean
    onBack?: () => void
    onFinish?: () => void
  }>(),
  {
    completed: false,
    onBack: undefined,
    onFinish: () => {},
  },
)

const title = computed(() => props.completed ? deckEmptyMessage() : deckNotEmptyMessage())
</script>

<style scoped>
.lightspeed-summary {
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

.lightspeed-info {
  grid-column: 1 / 3;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: clamp(8px, 1.8cqw, 12px);
}

.lightspeed-clock {
  display: flex;
  align-items: flex-start;
  justify-content: center;
  --stopwatch--bg: none;
}

.lightspeed-stats {
  flex: 1;
}

.lightspeed-chart-wrapper {
  grid-column: 3 / -1;
  width: 100%;
  height: 100%;
}

.lightspeed-chart {
  width: 100%;
  height: 100%;
  aspect-ratio: 1 / 1;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border-radius: 6px;
  padding: clamp(4px, 1.8cqw, 10px);
}

.lightspeed-left-button {
  margin-top: 4px;
  grid-column: 1 / 3;
}

.lightspeed-central-button {
  margin-top: 4px;
  grid-column: 1 / -1;
}

.lightspeed-right-button {
  margin-top: 4px;
  grid-column: 3 / -1;
}
</style>
