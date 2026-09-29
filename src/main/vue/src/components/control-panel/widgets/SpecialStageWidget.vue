<template>
  <div class="special-stage-widget">
    <AwesomeButton
      class="cp-widget cp-widget--special-stage"
      :icon="icon"
      :on-click="startReview"
      :disabled="flashcardsCount === 0"
      :hold-time="REVERSE_HOLD_TIME"
      :held="reversed"
      :on-hold="toggleReversed"
      fill-space
      square
    >
      <template #below>
        <div class="special-stage-title">
          <div class="cp-text">
            {{ props.stage.displayName }}
          </div>
          <div class="cp-count-box">
            {{ flashcardsCount }}
          </div>
        </div>
        <font-awesome-icon
          icon="fa-solid fa-play"
          class="cp-review-badge"
          :class="{ 'cp-review-badge--reversed': reversed }"
        />
      </template>
    </AwesomeButton>
  </div>
</template>

<script setup lang="ts">
import AwesomeButton from '@/components/common/AwesomeButton.vue'
import { Stage } from '@/core-logic/stage-logic.ts'
import { computed } from 'vue'
import { useFlashcardStore } from '@/stores/flashcard-store.ts'
import { countFlashcards, specialStageToReviewSessionType } from '@/core-logic/review-logic.ts'
import { storeToRefs } from 'pinia'
import { routeNames } from '@/router'
import { useRouter } from 'vue-router'
import { useChronoStore } from '@/stores/chrono-store.ts'
import { REVERSE_HOLD_TIME, useReverseMode } from '@/utils/reverse-mode.ts'

const props = defineProps<{
  stage: Stage
  icon: string
}>()

const router = useRouter()
const flashcardStore = useFlashcardStore()
const chronoStore = useChronoStore()

const sessionType = computed(() => specialStageToReviewSessionType(props.stage))
const { reversed, toggle: toggleReversed } = useReverseMode(sessionType)

const { flashcards } = storeToRefs(flashcardStore)
const { currDay } = storeToRefs(chronoStore)

const flashcardsCount = computed(() =>
  countFlashcards(flashcards.value, props.stage, currDay.value),
)

function startReview() {
  if (flashcardsCount.value === 0) return
  router.push({
    name: routeNames.review,
    query: { sessionType: sessionType.value, reversed: reversed.value ? 'true' : undefined },
  })
}
</script>

<style scoped>
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
</style>
