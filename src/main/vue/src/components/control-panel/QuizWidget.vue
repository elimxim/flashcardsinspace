<template>
  <div class="quiz-widget">
    <AwesomeButton
      icon="fa-solid fa-q"
      class="cp-widget"
      :disabled="!flashcardSet"
      :on-click="toggleStore.toggleQuiz"
      fill-space
      square
    >
      <template #below>
        <div class="cp-text quiz-text">
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

.quiz-text {
  margin-top: 8px;
}
</style>
