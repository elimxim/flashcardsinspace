<template>
  <div class="flashcard-widget flashcard-widget--theme">
    <AwesomeButton
      icon="fa-solid fa-rectangle-list"
      class="flashcard-widget-button"
      :disabled="!flashcardSet"
      :on-click="toggleStore.toggleFlashcardCreation"
      fill-space
      square
    >
      <template #below>
        <div class="flashcard-widget-button-text">
          Add flashcard
        </div>
      </template>
    </AwesomeButton>
  </div>
</template>

<script setup lang="ts">
import AwesomeButton from '@/components/AwesomeButton.vue'
import { useToggleStore } from '@/stores/toggle-store.ts'
import { useFlashcardStore } from '@/stores/flashcard-store.ts'
import { storeToRefs } from 'pinia'

const toggleStore = useToggleStore()

const flashcardStore = useFlashcardStore()
const { flashcardSet } = storeToRefs(flashcardStore)

</script>

<style scoped>
.flashcard-widget {
  position: relative;
  height: 100%;
  width: fit-content;
}

.flashcard-widget-button-text {
  margin-top: 4px;
  font-size: 0.9rem;
  font-weight: 600;
  word-spacing: 0.05rem;
  letter-spacing: 0.05rem;
  text-transform: uppercase;
  text-align: center;
}

</style>

