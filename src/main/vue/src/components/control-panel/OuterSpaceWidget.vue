<template>
  <div class="outer-space-widget outer-space-widget--theme">
    <Starfield
      twinkle
      :star-size="2"
      :density="100"
      vertical-drift="2px"
    />
    <div class="left-spacer"></div>
    <SmartButton
      class="outer-space-button"
      :text="isDisabled ? 'VOID' : 'ENTER OUTER SPACE'"
      :disabled="isDisabled"
      :on-click="startReview"
      fit-content
      auto-blur
    />
    <div class="right-spacer">
      <div class="outer-space-count">
        {{ flashcardCount }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import Starfield from '@/components/Starfield.vue'
import SmartButton from '@/components/SmartButton.vue'
import { computed } from 'vue'
import { countFlashcards, ReviewSessionType } from '@/core-logic/review-logic.ts'
import { useFlashcardStore } from '@/stores/flashcard-store.ts'
import { useChronoStore } from '@/stores/chrono-store.ts'
import { storeToRefs } from 'pinia'
import { specialStages } from '@/core-logic/stage-logic.ts'
import { routeNames } from '@/router'
import { useRouter } from 'vue-router'

const router = useRouter()
const flashcardStore = useFlashcardStore()
const chronoStore = useChronoStore()

const { flashcards } = storeToRefs(flashcardStore)
const { currDay } = storeToRefs(chronoStore)

const flashcardCount = computed(() => {
  return countFlashcards(flashcards.value, specialStages.OUTER_SPACE, currDay.value)
})
const isDisabled = computed(() => flashcardCount.value === 0)


function startReview() {
  router.push({
    name: routeNames.review,
    query: { sessionType: ReviewSessionType.OUTER_SPACE }
  })
}

</script>

<style scoped>
.outer-space-widget--theme {
  --o-widget--bg: var(--outer-space-widget--bg, #242124);
  --o-widget--stage--count--color: var(--stages-stage-count--color, rgba(13, 18, 74, 0.6));
  --o-widget--stage--count--bg: var(--stages-stage-count--bg, rgba(255, 255, 255, 0.6));
}

.outer-space-widget {
  position: relative;
  display: flex;
  flex-direction: row;
  align-items: center;
  width: 100%;
  height: 100%;
  border-radius: 6px;
  padding: 10px;
  background: var(--o-widget--bg);
}

.left-spacer {
  flex: 1;
}

.right-spacer {
  flex: 1;
  display: flex;
  justify-content: flex-start;
  padding-left: 10px;
}

.outer-space-count {
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--o-widget--stage--count--color);
  background: var(--o-widget--stage--count--bg);
  border-radius: 3px;
  padding: 2px;
  width: 40px;
  text-align: center;
}

</style>
