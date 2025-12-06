<template>
  <div class="quiz-result">
    <div class="quiz-title">
      Quiz Round {{ round }}
    </div>
    <div class="quiz-info">
      <div class="quiz-stats">
        <div class="quiz-stats-title">
          Overall Stats
        </div>
        <div class="quiz-stats-row">
          <div class="quiz-stats-text">
            Correct
          </div>
          <div class="quiz-stats-number">
            {{ overallCorrect }}
          </div>
        </div>
        <div class="quiz-stats-row">
          <div class="quiz-stats-text">
            Incorrect
          </div>
          <div class="quiz-stats-number">
            {{ roundFailed }}
          </div>
        </div>
        <div class="quiz-stats-row">
          <div class="quiz-stats-text">
            Total
          </div>
          <div class="quiz-stats-number">
            {{ overallTotal }}
          </div>
        </div>
      </div>
    </div>
    <div class="quiz-chart">
      <DoughnutChart
        :total="roundTotal"
        :left="roundFailed"
        legend-left="Incorrect"
        legend-right="Correct"
      />
    </div>
    <div
      :class="{
        'quiz-central-button': roundFailed === 0,
        'quiz-left-button': roundFailed > 0
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
</template>

<script setup lang="ts">
import DoughnutChart from '@/components/DoughnutChart.vue'
import SmartButton from '@/components/SmartButton.vue'

withDefaults(defineProps<{
  round: number
  overallTotal: number
  overallCorrect: number
  roundTotal: number
  roundFailed: number
  onNextRound?: () => void
  onFinish?: () => void
}>(), {
  onNextRound: () => {
  },
  onFinish: () => {
  },
})

</script>

<style scoped>
.quiz-result {
  width: 100%;
  height: 100%;
  display: grid;
  grid-template-rows: auto 1fr auto;
  grid-template-columns: repeat(5, 1fr);
  gap: 10px;
  padding: 10px;
  border-radius: 6px;
  border: 1px solid rgb(225, 228, 240);
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
  overflow: hidden;
}

.quiz-title {
  grid-column: 1 / -1;
  font-size: 1.2rem;
  font-weight: 600;
  letter-spacing: 0.05rem;
  word-spacing: 0.05rem;
  text-transform: uppercase;
  text-align: center;
  white-space: nowrap;
  color: #45454a;
  padding: 4px;
}

.quiz-info {
  grid-column: 1 / 3;
  flex: 0 0 38%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 10px;
  padding: 10px;
}

.quiz-chart {
  grid-column: 3 / -1;
  flex: 0 0 58%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.quiz-stats {
  display: flex;
  flex-direction: column;
  align-items: center;
  height: fit-content;
  gap: 10px;
  padding: 10px;
  border: 2px solid rgb(225, 228, 240);
  border-radius: 6px;
}

.quiz-stats-title {
  font-size: 1rem;
  font-weight: 600;
  letter-spacing: 0.05rem;
  word-spacing: 0.05rem;
  text-transform: uppercase;
  text-align: center;
  white-space: nowrap;
  color: rgb(159, 164, 182);
}

.quiz-stats-row {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  width: 100%;
}

.quiz-stats-text {
  font-size: 0.85rem;
  font-weight: 600;
  color: #45454a;
  letter-spacing: 0.05rem;
  word-spacing: 0.05rem;
  text-transform: uppercase;
  text-align: start;
  white-space: nowrap;
}

.quiz-stats-number {
  font-size: 0.85rem;
  font-weight: 600;
  color: rgba(17, 33, 85, 0.92);
  background: rgb(225, 228, 240);
  padding: 2px;
  margin: 2px;
  border-radius: 6px;
  width: 40px;
  text-align: center;
}

.quiz-left-button {
  grid-column: 1 / 3;
}

.quiz-central-button {
  grid-column: 1 / -1;
}

.quiz-right-button {
  grid-column: 3 / -1;
}
</style>
