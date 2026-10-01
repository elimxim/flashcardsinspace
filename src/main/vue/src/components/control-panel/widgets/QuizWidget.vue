<template>
  <div class="quiz-widget">
    <AwesomeButton
      :icon="reviewIcons.get(ReviewSessionType.QUIZ)!!"
      class="cp-widget"
      :disabled="!flashcardSet"
      :on-click="toggleStore.toggleQuiz"
      :hold-time="REVERSE_HOLD_TIME"
      :held="reversed"
      :on-hold="toggleReversed"
      fill-space
      square
    >
      <template #below>
        <div class="cp-text quiz-text">Start Quiz</div>
        <WidgetReviewBadge :reversed="reversed" />
      </template>
    </AwesomeButton>
  </div>
</template>

<script setup lang="ts">
import AwesomeButton from '@/components/common/AwesomeButton.vue'
import WidgetReviewBadge from '@/components/WidgetReviewBadge.vue'
import { useToggleStore } from '@/stores/toggle-store.ts'
import { useFlashcardStore } from '@/stores/flashcard-store.ts'
import { storeToRefs } from 'pinia'
import { reviewIcons, ReviewSessionType } from '@/core-logic/review-logic.ts'
import { REVERSE_HOLD_TIME, useReverseMode } from '@/utils/reverse-mode.ts'

const toggleStore = useToggleStore()
const flashcardStore = useFlashcardStore()

const { flashcardSet } = storeToRefs(flashcardStore)
const { reversed, toggle: toggleReversed } = useReverseMode(ReviewSessionType.QUIZ)
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
