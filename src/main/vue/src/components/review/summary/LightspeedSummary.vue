<template>
  <ReviewSummary>

  </ReviewSummary>
</template>

<script setup lang="ts">

defineProps<{
  correct: number
  incorrect: number
  elapsedTime: number
}>()

import ReviewSummary from '@/components/review/summary/ReviewSummary.vue'
</script>

<style scoped></style>
