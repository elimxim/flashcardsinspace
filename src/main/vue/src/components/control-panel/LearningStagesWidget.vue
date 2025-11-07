<template>
  <div class="stages-widget stages-widget--theme">
    <div class="stages-title">
      Learning Stages
    </div>
    <div class="stage-grid">
      <div
        v-for="stage in mainStageArray"
        :key="stage.name"
        class="stage-wrapper"
      >
        <div class="stage">
          <div class="stage-name">
            {{ stage.displayName }}
          </div>
          <div class="stage-count">
            {{ createFlashcardCountComputed(stage) }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { mainStageArray, Stage } from '@/core-logic/stage-logic.ts'
import { countFlashcards } from '@/core-logic/review-logic.ts'
import { useFlashcardStore } from '@/stores/flashcard-store.ts'
import { useChronoStore } from '@/stores/chrono-store.ts'
import { storeToRefs } from 'pinia'
import { computed } from 'vue'

const flashcardStore = useFlashcardStore()
const chronoStore = useChronoStore()

const { flashcards } = storeToRefs(flashcardStore)
const { currDay } = storeToRefs(chronoStore)

const createFlashcardCountComputed = (stage: Stage) => {
  return computed(() => {
    return countFlashcards(flashcards.value, stage, currDay.value)
  })
}

</script>

<style scoped>
.stages-widget--theme {

}

.stages-widget {
  position: relative;
  padding: 1px 4px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 4px;
  border: 1px solid black;
  border-radius: 6px;
  width: 100%;
  min-width: 360px;
  height: 100%;
}

.stages-title {
  font-size: 0.9rem;
  font-weight: 700;
  color: black;
  letter-spacing: 0.05rem;
  word-spacing: 0.05rem;
  text-transform: uppercase;
  align-self: start;
  text-align: left;
  white-space: nowrap;
}

.stage-grid {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  align-content: center;
  gap: 4px;
  width: 100%;
}

.stage-wrapper {
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}

.stage {
  display: flex;
  flex-direction: column;
  border: 1px solid black;
  border-radius: 6px;
  justify-content: center;
  align-items: center;
  width: 70%;
  min-width: 40px;
  max-width: 106px;
  height: auto;
  min-height: 40px;
  max-height: 106px;
  aspect-ratio: 1 / 1;
  padding: 4px;
  gap: 4px;
  container-type: size;
}

.stage-name {
  font-size: clamp(0.4rem, 24cqw, 1rem);
  font-weight: 500;
  color: black;
  letter-spacing: 0.05rem;
  word-spacing: 0.05rem;
  text-transform: uppercase;
  text-align: center;
  white-space: nowrap;
}

.stage-count {
  font-size: clamp(0.3rem, 24cqw, 0.8rem);
  font-weight: 600;
  color: black;
  border: 1px solid black;
  border-radius: 4px;
  background: none;
  padding: 2px 4px;
  width: clamp(10px, 50cqw, 40px);
  text-align: center;
}
</style>
