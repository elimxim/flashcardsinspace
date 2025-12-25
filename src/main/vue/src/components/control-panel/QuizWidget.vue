<template>
  <div class="quiz-widget quiz-widget--theme">
    <AwesomeButton
      icon="fa-solid fa-q"
      class="quiz-widget-button"
      :disabled="!flashcardSet"
      :on-click="toggleStore.toggleQuiz"
      fill-space
      square
    >
      <template #below>
        <div class="quiz-widget-button-text">
          Start Quiz
        </div>
      </template>
    </AwesomeButton>
  </div>
</template>

<script setup lang="ts">
import AwesomeButton from '@/components/AwesomeButton.vue'
import { useToggleStore } from '@/stores/toggle-store.ts'
import { useFlashcardStore } from '@/stores/flashcard-store.ts'
import { storeToRefs } from 'pinia'

const toggleStore = useToggleStore()
const flashcardStore = useFlashcardStore()

const { flashcardSet } = storeToRefs(flashcardStore)
</script>

<style scoped>
.quiz-widget {
  position: relative;
  height: 100%;
  width: fit-content;
}

.quiz-widget-button-text {
  margin-top: 4px;
  font-size: 0.9rem;
  font-weight: 600;
  word-spacing: 0.05rem;
  letter-spacing: 0.05rem;
  text-transform: uppercase;
  text-align: center;
}
</style>
