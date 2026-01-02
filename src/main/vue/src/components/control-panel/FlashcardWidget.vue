<template>
  <div class="flashcard-widget">
    <AwesomeButton
      icon="fa-solid fa-rectangle-list"
      class="cp--widget"
      :disabled="!flashcardSet"
      :on-click="toggleStore.toggleFlashcardCreation"
      fill-space
      square
    >
      <template #below>
        <div class="cp--text">
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

</style>

