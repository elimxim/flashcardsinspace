<template>
  <LightspeedReviewPage
    v-if="reviewMode.isLightspeed()"
    :session-id="sessionId"
    :stages="stages"
  />
  <SpecialReviewPage
    v-else-if="reviewMode.isSpecial()"
    :session-id="sessionId"
    :review-mode="reviewMode"
  />
  <QuizReviewPage
    v-else-if="reviewMode.isQuiz()"
    :session-id="sessionId"
    :stages="stages"
  />
</template>

<script setup lang="ts">
import LightspeedReviewPage from '@/pages/LightspeedReviewPage.vue'
import SpecialReviewPage from '@/pages/SpecialReviewPage.vue'
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
