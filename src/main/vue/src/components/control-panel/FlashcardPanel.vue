<template>
  <div class="flashcard-panel flashcard-panel--theme">
    <AwesomeButton
      icon="fa-solid fa-rectangle-list"
      class="flashcard-panel-button"
      :on-click="modalStore.toggleFlashcardCreation"
      fill-space
      square
    />
    <div class="flashcard-panel-text">
      Add flashcard
    </div>
  </div>
  <FlashcardModificationModal/>
</template>

<script setup lang="ts">
import FlashcardModificationModal from '@/views/modal/FlashcardModificationModal.vue'
import AwesomeButton from '@/components/AwesomeButton.vue'
import { useModalStore } from '@/stores/modal-store.ts'

const modalStore = useModalStore()

</script>

<style scoped>
.flashcard-panel--theme {
  --f-panel--text-color: var(--flashcard-panel--text-color, rgba(43, 69, 142, 0.88));
}

.flashcard-panel {
  position: relative;
  display: grid;
  grid-template-rows: minmax(0, 1fr) auto;
  height: 100%;
  width: auto;
  gap: 1px;
  padding: 1px;
}

.flashcard-panel-text {
  font-size: 0.6rem;
  color: var(--f-panel--text-color);
  word-spacing: 0.05rem;
  letter-spacing: 0.05rem;
  text-transform: uppercase;
  white-space: nowrap;
  text-align: center;
}

</style>

