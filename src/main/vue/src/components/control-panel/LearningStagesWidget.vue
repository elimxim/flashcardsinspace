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
  padding: 4px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 4px;
  border: 1px solid black;
  border-radius: 6px;
  width: 100%;
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
  padding: 2px;
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
  width: fit-content;
  padding: 4px;
  gap: 4px;
}

.stage-name {
  font-size: 0.8rem;
  font-weight: 500;
  color: black;
  letter-spacing: 0.05rem;
  word-spacing: 0.05rem;
  text-transform: uppercase;
  text-align: center;
  white-space: nowrap;
}

.stage-count {
  font-size: 0.7rem;
  font-weight: 600;
  color: black;
  border: 1px solid black;
  border-radius: 4px;
  background: none;
  padding: 2px 4px;
  width: 30px;
  text-align: center;
}
</style>
