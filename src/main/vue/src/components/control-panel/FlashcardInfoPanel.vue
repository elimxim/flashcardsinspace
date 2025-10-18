<template>
  <div class="flashcard-panel flashcard-panel--theme">
    <div class="flashcard-panel-content">
      <div class="flashcard-panel-label">
        Info
      </div>
      <div class="flashcard-panel-layout">
        <div class="flashcard-panel-row flashcard-panel-row--language">
          <AwesomeContainer
            icon="fa-solid fa-globe"
            class="awesome-globe flashcard-panel-awesome-language"
          >
            <div class="flashcard-panel-text">
              {{ language?.name }}
            </div>
          </AwesomeContainer>
        </div>
        <div class="flashcard-panel-row">
          <div class="flashcard-panel-text--info flashcard-panel-text--info--large">
            Flashcards Total
          </div>
          <div class="flashcard-panel-number">
            {{ flashcardCount }}
          </div>
        </div>
        <div class="flashcard-panel-button-wrapper">
          <AwesomeButton
            icon="fa-solid fa-rectangle-list"
            class="flashcard-panel-button"
            :on-click="modalStore.toggleFlashcardCreation"
            fill-space
          />
        </div>
        <div class="flashcard-panel-text--info">
          Add new flashcard
        </div>
      </div>
    </div>
  </div>
  <FlashcardModificationModal/>
</template>

<script setup lang="ts">
import FlashcardModificationModal from '@/views/modal/FlashcardModificationModal.vue'
import AwesomeButton from '@/components/AwesomeButton.vue'
import AwesomeContainer from '@/components/AwesomeContainer.vue'
import { useFlashcardStore } from '@/stores/flashcard-store.ts'
import { useModalStore } from '@/stores/modal-store.ts'
import { computed } from 'vue'
import { storeToRefs } from 'pinia'

const flashcardStore = useFlashcardStore()
const modalStore = useModalStore()

const { flashcards, language } = storeToRefs(flashcardStore)

const flashcardCount = computed(() => flashcards.value.length)

</script>

<style scoped>
.flashcard-panel--theme {
  font-family: var(--main-font-family);
  --f-panel--label-color: var(--flashcard-panel--label-color, #555555);
  --f-panel--text-color: var(--flashcard-panel--text-color, rgba(57, 57, 57, 0.92));
  --f-panel--text-color--info: var(--flashcard-panel--text-color--info, rgba(43, 69, 142, 0.88));
  --f-panel--border-color: var(--flashcard-panel--border-color, rgba(128, 128, 128, 0.62));
  --f-panel--number-color: var(--flashcard-panel--number-color, rgba(17, 33, 85, 0.92));
  --f-panel--language--bg: var(--flashcard-panel--language--bg, rgba(88, 114, 209, 0.13));
}

.flashcard-panel {
  position: relative;
  display: flex;
  flex-direction: row;
}

.flashcard-panel-label {
  font-size: clamp(0.5rem, 1.8vw, 0.6rem);
  color: var(--f-panel--label-color);
  letter-spacing: 0.1rem;
  word-spacing: 0.1rem;
  text-transform: uppercase;
  padding: 0 0 0 6px;
}

.flashcard-panel-content {
  position: relative;
  display: flex;
  flex-direction: column;
  padding: 1px;
}

.flashcard-panel-layout {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 4px;
  gap: 1px;
  border: 1px solid var(--f-panel--border-color);
  border-radius: 6px;
  width: clamp(180px, 20vw, 200px);
  height: clamp(110px, 20vw, 120px);
}

.flashcard-panel-row {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  padding-bottom: clamp(2px, 0.5vw, 4px);
}

.flashcard-panel-text {
  font-size: clamp(0.75rem, 1.5vw, 0.9rem);
  color: var(--f-panel--text-color);
  white-space: nowrap;
  word-spacing: 0.05rem;
  letter-spacing: 0.05rem;
}

.flashcard-panel-text--info {
  font-size: clamp(0.5rem, 1vw, 0.6rem);
  color: var(--f-panel--text-color--info);
  word-spacing: 0.05rem;
  letter-spacing: 0.05rem;
  text-transform: uppercase;
  text-align: center;
}

.flashcard-panel-text--info--large {
  font-size: clamp(0.6rem, 1vw, 0.7rem);
}

.flashcard-panel-number {
  font-size: clamp(0.75rem, 2vw, 0.9rem);
  font-weight: 500;
  border: 1px solid var(--f-panel--border-color);
  color: var(--f-panel--number-color);
  border-radius: 3px;
  padding: 2px;
  width: clamp(40px, 4vw, 50px);
  height: fit-content;
  text-align: center;
}

.flashcard-panel-row--language {
  justify-content: left;
  padding-bottom: 2px;
}

.flashcard-panel-awesome-language {
  --awesome-container--icon--font-size: clamp(12px, 1.5vw, 18px);
  --awesome-container--gap: 4px;
}

.flashcard-panel-button-wrapper {
  flex: 1;
  width: 100%;
  height: 100%;
}
</style>

