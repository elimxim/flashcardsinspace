<template>
  <ReviewPage
    v-if="reviewMode.isLightspeed()"
    :session-type="reviewMode.sessionType"
    :session-id="sessionId"
    :stages="stages"
  />
  <ReviewPage
    v-else-if="reviewMode.isSpecial()"
    :session-type="reviewMode.sessionType"
    :session-id="sessionId"
    :stages="stages"
  />
  <QuizReviewPage
    v-else-if="reviewMode.isQuiz()"
    :session-id="sessionId"
    :stages="stages"
  />
</template>

<script setup lang="ts">
import ReviewPage from '@/pages/ReviewPage.vue'
import QuizReviewPage from '@/pages/QuizReviewPage.vue'
import { Stage } from '@/core-logic/stage-logic.ts'
import { computed } from 'vue'
import { determineReviewMode } from '@/core-logic/review-logic.ts'

const props = defineProps<{
  sessionType?: string,
  sessionId?: number,
  stages: Stage[],
}>()

const reviewMode = computed(() => determineReviewMode(props.sessionType, props.stages))

</script>

<style scoped>
</style>
