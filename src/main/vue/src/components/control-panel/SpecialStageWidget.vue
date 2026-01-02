<template>
  <div class="special-stage-widget special-stage-widget--theme">
    <AwesomeButton
      class="cp-widget"
      :icon="icon"
      :on-click="startReview"
      :disabled="flashcardsCount === 0"
      fill-space
      square
    >
      <template #below>
        <div class="special-stage-title">
          <div class="special-stage-text">
            {{ props.stage.displayName }}
          </div>
          <div class="special-stage-count">
            {{ flashcardsCount }}
          </div>
        </div>
      </template>
    </AwesomeButton>
  </div>
</template>

<script setup lang="ts">
import { Stage } from '@/core-logic/stage-logic.ts'
import { computed } from 'vue'
import { useFlashcardStore } from '@/stores/flashcard-store.ts'
import {
  countFlashcards,
  specialStageToReviewSessionType
} from '@/core-logic/review-logic.ts'
import { storeToRefs } from 'pinia'
import { routeNames } from '@/router'
import { useRouter } from 'vue-router'
import { useChronoStore } from '@/stores/chrono-store.ts'
import AwesomeButton from '@/components/AwesomeButton.vue'

const props = defineProps<{
  stage: Stage
  icon: string
}>()

const router = useRouter()
const flashcardStore = useFlashcardStore()
const chronoStore = useChronoStore()

const { flashcards } = storeToRefs(flashcardStore)
const { currDay } = storeToRefs(chronoStore)

const flashcardsCount = computed(() =>
  countFlashcards(flashcards.value, props.stage, currDay.value)
)

function startReview() {
  if (flashcardsCount.value === 0) return
  const sessionType = specialStageToReviewSessionType(props.stage)
  router.push({
    name: routeNames.review,
    query: { sessionType: sessionType }
  })
}

</script>

<style scoped>
.special-stage-widget--theme {
  --s-widget--title--color: var(--special-stage-widget--title--color, rgba(13, 18, 74, 0.6));
  --s-widget--number--color: var(--special-stage-widget--number--color, rgba(20, 27, 106, 0.82));
  --s-widget--number--bg: var(--special-stage-widget--number--bg, rgba(255, 255, 255, 0.6));
}

.special-stage-widget {
  position: relative;
  height: 100%;
  width: fit-content;
}

.special-stage-title {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 4px;
}

.special-stage-text {
  flex: 1;
  font-size: 0.9rem;
  font-weight: 600;
  word-wrap: break-word;
  word-spacing: 0.05rem;
  letter-spacing: 0.05rem;
  text-transform: uppercase;
  text-align: center;
  color: var(--s-widget--title--color);
}

.special-stage-count {
  font-size: 0.85rem;
  font-weight: 600;
  text-align: center;
  color: var(--s-widget--number--color);
  background: var(--s-widget--number--bg);
  border-radius: 4px;
  padding: 2px;
  width: 40px;
}

</style>
