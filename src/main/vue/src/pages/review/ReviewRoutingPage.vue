<template>
  <LightspeedReviewPage
    v-if="reviewMode.isLightspeed()"
    :session-id="sessionId"
    :stages="stages"
    :reversed="reversed"
  />
  <SpecialReviewPage
    v-else-if="reviewMode.isSpecial()"
    :session-id="sessionId"
    :review-mode="reviewMode"
    :reversed="reversed"
  />
  <QuizReviewPage
    v-else-if="reviewMode.isQuiz()"
    :session-id="sessionId"
    :stages="stages"
    :reversed="reversed"
  />
</template>

<script setup lang="ts">
import LightspeedReviewPage from '@/pages/review/LightspeedReviewPage.vue'
import SpecialReviewPage from '@/pages/review/SpecialReviewPage.vue'
import QuizReviewPage from '@/pages/review/QuizReviewPage.vue'
import { Stage } from '@/core-logic/stage-logic.ts'
import { computed } from 'vue'
import { determineReviewMode } from '@/core-logic/review-logic.ts'

const props = withDefaults(
  defineProps<{
    sessionType?: string
    sessionId?: number
    stages: Stage[]
    reversed?: boolean
  }>(),
  {
    sessionType: undefined,
    sessionId: undefined,
    reversed: false,
  },
)

const reviewMode = computed(() => determineReviewMode(props.sessionType, props.stages))
</script>

<style scoped></style>
