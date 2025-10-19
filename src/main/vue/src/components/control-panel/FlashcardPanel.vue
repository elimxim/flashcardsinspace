<template>
  <div class="flashcard-panel flashcard-panel--theme">
    <div class="flashcard-panel-button-wrapper">
      <AwesomeButton
        icon="fa-solid fa-rectangle-list"
        class="flashcard-panel-button"
        :on-click="modalStore.toggleFlashcardCreation"
        fill-space
        square
      />
    </div>
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
  display: flex;
  flex-direction: column;
  width: fit-content;
  height: 100%;
  gap: 1px;
}

.flashcard-panel-text {
  font-size: clamp(0.6rem, 2vw, 0.7rem);
  color: var(--f-panel--text-color);
  word-spacing: 0.05rem;
  letter-spacing: 0.05rem;
  text-transform: uppercase;
  white-space: nowrap;
  text-align: center;
}

.flashcard-panel-button-wrapper {
  flex: 1;
  width: 100%;
  height: 100%;
}
</style>

