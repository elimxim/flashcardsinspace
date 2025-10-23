<template>
  <div class="flashcard-widget flashcard-widget--theme">
    <AwesomeButton
      icon="fa-solid fa-rectangle-list"
      class="flashcard-widget-button"
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
  <FlashcardModificationModal/>
</template>

<script setup lang="ts">
import FlashcardModificationModal from '@/views/modal/FlashcardModificationModal.vue'
import AwesomeButton from '@/components/AwesomeButton.vue'
import { useToggleStore } from '@/stores/toggle-store.ts'

const toggleStore = useToggleStore()

</script>

<style scoped>
.flashcard-widget {
  position: relative;
  display: flex;
  height: 100%;
}

.flashcard-widget-button-text {
  margin-top: 4px;
  font-size: 0.8rem;
  font-weight: 600;
  word-spacing: 0.05rem;
  letter-spacing: 0.05rem;
  text-transform: uppercase;
  text-align: center;
}

</style>

