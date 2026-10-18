<template>
  <div class="summary-answer-stats">
    <div v-if="title" class="answer-stats-title">{{ title }}</div>
    <div class="summary-answer-stats-rows">
      <div class="summary-answer-stats-row">
        <div class="summary-answer-stats-text">Correct</div>
        <div class="summary-answer-stats-number">
          {{ correct }}
        </div>
      </div>
      <div class="summary-answer-stats-row">
        <div class="summary-answer-stats-text">Incorrect</div>
        <div class="summary-answer-stats-number">
          {{ incorrect }}
        </div>
      </div>
      <div v-if="total !== undefined" class="summary-answer-stats-row">
        <div class="summary-answer-stats-text">Total</div>
        <div class="summary-answer-stats-number">
          {{ total }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    title?: string
    correct: number
    incorrect: number
    total?: number
  }>(),
  {
    title: undefined,
    total: undefined,
  },
)
</script>

<style scoped>
.summary-answer-stats {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: clamp(2px, 1cqw, 4px);
  padding: 10px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border-radius: 6px;
}

.answer-stats-title {
  font-size: clamp(0.7rem, 2.5cqw, 0.85rem);
  font-weight: 400;
  letter-spacing: 0.05rem;
  word-spacing: 0.05rem;
  text-transform: uppercase;
  text-align: center;
  white-space: nowrap;
  color: #6a6a6a;
}

.summary-answer-stats-rows {
  flex: 1;
  width: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: clamp(2px, 1cqw, 4px);
}

.summary-answer-stats-row {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  gap: clamp(4px, 1cqw, 10px);
  width: 100%;
}

.summary-answer-stats-text {
  font-size: clamp(0.65rem, 2.5cqw, 0.85rem);
  font-weight: 600;
  color: #353535;
  letter-spacing: 0.05rem;
  word-spacing: 0.05rem;
  text-transform: uppercase;
  text-align: start;
  white-space: nowrap;
}

.summary-answer-stats-number {
  font-size: clamp(0.7rem, 2.5cqw, 0.85rem);
  font-weight: 600;
  color: rgba(17, 33, 85, 0.92);
  background: rgb(225, 228, 240);
  padding: clamp(2px, 1cqw, 3px);
  margin: clamp(1px, 0.5cqw, 2px);
  border-radius: 6px;
  width: 40px;
  text-align: center;
}
</style>
