<template>
  <ReviewSummary :title="deckEmptyMessage()">

  </ReviewSummary>
</template>

<script setup lang="ts">
import { deckEmptyMessage } from '@/core-logic/review-logic.ts'

defineProps<{
  correct: number
  incorrect: number
  elapsedTime: number
}>()

import ReviewSummary from '@/components/review/summary/ReviewSummary.vue'
</script>

<style scoped></style>
