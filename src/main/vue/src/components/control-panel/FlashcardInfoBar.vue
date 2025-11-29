<template>
  <transition name="fade-slide">
    <div v-if="!hidden" class="flashcard-info-bar flashcard-info-bar--theme">
      <div class="language-info">
        <AwesomeContainer icon="fa-solid fa-globe">
          <div class="language-info-text">
            {{ language?.name }}
          </div>
        </AwesomeContainer>
      </div>
      <div class="flashcards-info">
        <div class="flashcards-info-number">
          {{ flashcardCount }}
        </div>
        <div class="flashcards-info-text">
          Flashcards
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup lang="ts">
import AwesomeContainer from '@/components/AwesomeContainer.vue'
import { useFlashcardStore } from '@/stores/flashcard-store.ts'
import { storeToRefs } from 'pinia'
import { computed } from 'vue'

withDefaults(defineProps<{
  hidden?: boolean
}>(), {
  hidden: false,
})

const flashcardStore = useFlashcardStore()
const { flashcards, language } = storeToRefs(flashcardStore)

const flashcardCount = computed(() => flashcards.value.length)
</script>

<style scoped>
.flashcard-info-bar--theme {
  --f-bar--bg-color: var(--flashcard-info-bar--bg-color, transparent);
  --f-bar--language--text-color: var(--flashcard-info-bar--text-color, #333333);
  --f-bar--language--icon-color: var(--flashcard-info-bar--language--icon-color, #007bff);
  --f-bar--flashcards--text-color: var(--flashcard-info-bar--flashcards--text-color, rgba(43, 69, 142, 0.88));
  --f-bar--flashcards--number--color: var(--flashcard-info-bar--flashcards--number--color, rgba(17, 33, 85, 0.92));
  --f-bar--flashcards--number--bg-color: var(--flashcard-info-bar--flashcards--number--bg-color, rgb(225, 228, 240));
}

.flashcard-info-bar {
  display: flex;
  flex-direction: row;
  justify-content: flex-start;
  align-items: center;
  background: var(--f-bar--bg-color);
  gap: 40px;
  height: fit-content;
  max-height: max-content;
}

.language-info {
  display: flex;
  --awesome-container--icon--size: 1.2rem;
  --awesome-container--icon--color: var(--f-bar--language--icon-color);
  --awesome-container--gap: 8px;
}

.language-info-text {
  font-size: clamp(0.75rem, 1.5vw, 0.9rem);
  color: var(--f-bar--language--text-color);
  white-space: nowrap;
  word-spacing: 0.05rem;
  letter-spacing: 0.05rem;
}

.flashcards-info {
  display: flex;
  flex-direction: row;
  justify-content: flex-start;
  align-items: center;
  gap: 8px;
}

.flashcards-info-text {
  font-size: 0.7rem;
  color: var(--f-bar--flashcards--text-color);
  word-spacing: 0.05rem;
  letter-spacing: 0.05rem;
  text-transform: uppercase;
  text-align: center;
  white-space: nowrap;
}

.flashcards-info-number {
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--f-bar--flashcards--number--color);
  background-color: var(--f-bar--flashcards--number--bg-color);
  border-radius: 3px;
  padding: 2px;
  width: 50px;
  height: fit-content;
  text-align: center;
}

.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: max-height 0.3s ease-out, opacity 0.3s ease-out;
}

.fade-slide-enter-from {
  max-height: 0;
  opacity: 0;
}

.fade-slide-enter-to {
  max-height: 100px;
  opacity: 0;
}

.fade-slide-leave-from {
  max-height: 100px;
  opacity: 1;
}

.fade-slide-leave-to {
  max-height: 0;
  opacity: 0;
}
</style>
