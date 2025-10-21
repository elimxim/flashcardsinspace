<template>
  <div
    class="special-stage-widget special-stage-widget--theme"
  >
    <div class="special-stage-top">
      <div class="special-stage-icon">
        <font-awesome-icon :icon="icon"/>
      </div>
      <div class="special-stage-title">
        {{ stage.displayName }}<br>Flashcards
      </div>
    </div>
    <div class="special-stage-bottom">
      <div class="special-stage-count">
        {{ flashcardsCount }}
      </div>
      <SmartButton
        text="VIEW"
        class="special-stage-button"
        :on-click="startReview"
        :title-scale="1.1"
        fill-width
        fill-height
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import SmartButton from '@/components/SmartButton.vue'
import { Stage } from '@/core-logic/stage-logic.ts'
import { computed } from 'vue'
import { useFlashcardStore } from '@/stores/flashcard-store.ts'
import { countFlashcards } from '@/core-logic/review-logic.ts'
import { storeToRefs } from 'pinia'
import { routeNames } from '@/router'
import { useRouter } from 'vue-router'

const props = defineProps<{
  stage: Stage
  icon: string
}>()

const router = useRouter()
const flashcardStore = useFlashcardStore()

const { flashcards } = storeToRefs(flashcardStore)

const flashcardsCount = computed(() => countFlashcards(flashcards.value, props.stage))

function startReview() {
  router.push({ name: routeNames.review, query: { stage: props.stage.name } })
}

</script>

<style scoped>
.special-stage-widget--theme {
  --s-widget--bg: var(--special-stage-widget--bg, linear-gradient(135deg, rgba(102, 126, 234, 0.66) 0%, rgba(118, 75, 162, 0.68) 100%));
  --s-widget--title--color: var(--special-stage-widget--title--color, rgba(13, 18, 74, 0.6));
  --s-widget--icon--color: var(--special-stage-widget--icon--color, rgba(255, 255, 255, 0.6));
  --s-widget--number--color: var(--special-stage-widget--number--color, rgba(20, 27, 106, 0.82));
  --s-widget--number--bg: var(--special-stage-widget--number--bg, rgba(255, 255, 255, 0.6));
  --s-widget--button--bg: var(--special-stage-widget--button--bg, rgba(0, 0, 0, 0.22));
  --s-widget--button--bg--hover: var(--special-stage-widget--button--bg--hover, rgba(0, 0, 0, 0.35));
}

.special-stage-widget {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 4px;
  width: fit-content;
  height: 100%;
  background: var(--s-widget--bg);
  border-radius: 6px;
}

.special-stage-top {
  display: flex;
  flex-direction: row;
  justify-content: center;
  align-items: center;
  gap: 2px;
}

.special-stage-bottom {
  flex: 1;
  height: 100%;
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: flex-end;
  gap: 8px;
  padding: 6px;
}

.special-stage-title {
  flex: 1;
  padding: 4px;
  font-size: 0.9rem;
  font-weight: 600;
  word-wrap: break-word;
  word-spacing: 0.05rem;
  letter-spacing: 0.05rem;
  text-transform: uppercase;
  text-align: left;
  color: var(--s-widget--title--color);
}

.special-stage-icon {
  font-size: 1.6rem;
  color: var(--s-widget--icon--color);
  padding: 4px;
  transition: transform 0.2s ease-in-out;
}

.special-stage-count {
  font-size: 1rem;
  font-weight: 600;
  color: var(--s-widget--number--color);
  background: var(--s-widget--number--bg);
  border-radius: 4px;
  padding: 2px;
  width: 40px;
  text-align: center;
}

.special-stage-button {
  flex: 1;
  height: 100%;
  width: 100%;
  --smart-button--title--font-size: 0.9rem;
  --smart-button--title--letter-spacing: 0.1rem;
  --smart-button--border-radius: 6px;
  --smart-button--bg: var(--s-widget--button--bg);
  --smart-button--bg--hover: var(--s-widget--button--bg--hover);
}

.special-stage-widget:hover .special-stage-icon {
  transform: scale(1.2);
}

</style>
