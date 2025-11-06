<template>
  <div class="launch-widget">
    <LaunchButton
      :on-click="startReview"
      :disabled="isDisabled"
    />
  </div>
</template>

<script setup lang="ts">
import LaunchButton from '@/components/control-panel/LaunchButton.vue'
import { useRouter } from 'vue-router'
import { routeNames } from '@/router'
import { useFlashcardStore } from '@/stores/flashcard-store.ts'
import { computed } from 'vue'
import { storeToRefs } from 'pinia'
import { createReviewQueue } from '@/core-logic/review-logic.ts'
import { useChronoStore } from '@/stores/chrono-store.ts'

const router = useRouter()
const flashcardStore = useFlashcardStore()
const chronoStore = useChronoStore()

const { flashcards, isEmpty, isSuspended } = storeToRefs(flashcardStore)
const { currDay, chronodays } = storeToRefs(chronoStore)

const noFlashcardsForReview = computed<boolean>(() => {
  const queue = createReviewQueue(flashcards.value, currDay.value, chronodays.value)
  return queue.remaining() === 0
})

const isDisabled = computed(() =>
  isEmpty.value || isSuspended.value || noFlashcardsForReview.value
)

function startReview() {
  router.push({ name: routeNames.review })
}
</script>

<style scoped>
.launch-widget {
  position: relative;
  display: flex;
  align-items: center;
  height: 100%;
  min-width: 106px;
}
</style>
