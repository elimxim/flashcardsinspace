<template>
  <div class="outer-space-widget">
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
      <div class="cp-count-box">
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
import { waitUntilStoreLoaded } from '@/utils/store-loading.ts'

const router = useRouter()
const flashcardStore = useFlashcardStore()
const chronoStore = useChronoStore()

await waitUntilStoreLoaded(flashcardStore)
await waitUntilStoreLoaded(chronoStore)

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
.outer-space-widget {
  position: relative;
  display: flex;
  flex-direction: row;
  align-items: center;
  width: 100%;
  height: 100%;
  border-radius: 6px;
  padding: 10px;
  background: #242124;
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

</style>
