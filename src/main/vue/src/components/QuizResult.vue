<template>
  <div class="quiz-result">
    <div class="quiz-title">
      Quiz Round {{ round }}
    </div>
    <DoughnutChart
      :total="flashcardsTotal"
      :left="flashcardsFailed"
      left-placeholder="Incorrect"
      right-placeholder="Correct"
    />
    <div class="quiz-next-round-button">
      <SmartButton
        text="Ready for the Next Round?"
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

const props = defineProps<{
  round: number
  flashcardsTotal: number
  flashcardsFailed: number
  onNextRound: () => void
}>()

</script>

<style scoped>
.quiz-result {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 10px;
  border-radius: 6px;
  border: 1px solid rgb(126, 128, 135);
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
}

.quiz-title {
  letter-spacing: 0.05rem;
  word-spacing: 0.05rem;
  text-transform: uppercase;
  text-align: center;
  white-space: nowrap;
  font-size: 0.9rem;
  font-weight: 600;
  color: black;
  padding: 4px;
}

.quiz-next-round-button {
  margin-top: auto;
}
</style>
