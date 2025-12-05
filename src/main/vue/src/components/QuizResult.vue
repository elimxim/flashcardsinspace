<template>
  <div class="quiz-result">
    <div class="quiz-title">
      Quiz Round {{ round }} Results
    </div>
    <div class="quiz-curr-round-grid">
      <div class="quiz-curr-round-text">
        Flashcards Known
      </div>
      <div class="quiz-curr-round-number">
        {{ flashcardsTotal - flashcardsFailed }}
      </div>
      <div class="quiz-curr-round-text">
        Flashcards Unknown
      </div>
      <div class="quiz-curr-round-number">
        {{ flashcardsFailed }}
      </div>
      <div class="quiz-next-round-button">
        <SmartButton
          text="Ready for Next Round?"
          class="calm-button"
          :on-click="onNextRound"
          fill-width
          auto-blur
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import SmartButton from '@/components/SmartButton.vue'

defineProps<{
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

.quiz-curr-round-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  grid-template-rows: repeat(3, 1fr);
  grid-auto-flow: row;
  align-items: center;
  gap: 10px;
}

.quiz-curr-round-text {
  font-size: 1rem;
  color: #45454a;
  letter-spacing: 0.05rem;
  word-spacing: 0.05rem;
  text-transform: uppercase;
  text-align: center;
  white-space: nowrap;
  justify-self: start;
}

.quiz-curr-round-number {
  font-size: 0.85rem;
  font-weight: 600;
  color: orangered;
  background: gray;
  border-radius: 3px;
  padding: 2px;
  width: 50px;
  text-align: center;
  justify-self: end;
}

.quiz-next-round-button {
  grid-column: 1 / -1;
}
</style>
