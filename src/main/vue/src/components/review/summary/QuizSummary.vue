<template>
  <ReviewSummary :title="`Quiz Round ${round}`">
    <div class="quiz-summary">
      <div class="quiz-info">
        <div class="quiz-clock">
          <Clock :time="elapsedTime" top-label="Round Time" />
        </div>
        <div v-if="elapsedTotalTime" class="quiz-clock">
          <Clock :time="elapsedTotalTime" top-label="Total Time" />
        </div>
        <SummaryAnswerStats
          class="quiz-stats"
          title="Overall"
          :correct="overallCorrect"
          :incorrect="overallIncorrect"
        />
      </div>
      <div class="quiz-chart-wrapper">
        <div class="quiz-chart">
          <DoughnutChart
            :total="roundTotal"
            :left="roundFailed"
            legend-left="Incorrect"
            legend-right="Correct"
          />
        </div>
      </div>
      <div
        :class="{
          'quiz-central-button': roundFailed === 0,
          'quiz-left-button': roundFailed > 0,
        }"
      >
        <SmartButton
          text="Finish Quiz"
          class="off-button"
          :on-click="onFinish"
          fill-width
          auto-blur
        />
      </div>
      <div v-if="roundFailed > 0" class="quiz-right-button">
        <SmartButton
          text="Next Round"
          class="calm-button"
          :on-click="onNextRound"
          fill-width
          auto-blur
        />
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

withDefaults(
  defineProps<{
    elapsedTime: number
    elapsedTotalTime: number
    round: number
    overallCorrect: number
    overallIncorrect: number
    roundTotal: number
    roundFailed: number
    onNextRound?: () => void
    onFinish?: () => void
  }>(),
  {
    onNextRound: () => {},
    onFinish: () => {},
  },
)
</script>

<style scoped>
.quiz-summary {
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

.quiz-info {
  grid-column: 1 / 3;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: clamp(8px, 1.8cqw, 12px);
}

.quiz-chart-wrapper {
  grid-column: 3 / -1;
  width: 100%;
  height: 100%;
}

.quiz-chart {
  width: 100%;
  height: 100%;
  aspect-ratio: 1 / 1;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border-radius: 6px;
  padding: clamp(4px, 1.8cqw, 10px);
}

.quiz-stats {
  flex: 1;
}

.quiz-clock {
  display: flex;
  align-items: flex-start;
  justify-content: center;
  --stopwatch--bg: none;
}

.quiz-left-button {
  margin-top: 4px;
  grid-column: 1 / 3;
}

.quiz-central-button {
  margin-top: 4px;
  grid-column: 1 / -1;
}

.quiz-right-button {
  margin-top: 4px;
  grid-column: 3 / -1;
}
</style>
