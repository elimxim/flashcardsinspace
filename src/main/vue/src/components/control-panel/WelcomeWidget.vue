<template>
  <div class="welcome-widget">
    <div class="welcome-text">
      {{ welcomeText() }}
    </div>
    <SmartButton
      :text="buttonText()"
      class="welcome-button"
      :on-click="toggleModal"
    />
  </div>
</template>

<script setup lang="ts">
import SmartButton from '@/components/SmartButton.vue'
import { useToggleStore } from '@/stores/toggle-store.ts'
import { useFlashcardSetStore } from '@/stores/flashcard-set-store.ts'
import { storeToRefs } from 'pinia'
import { waitUntilStoreLoaded } from '@/utils/store-loading.ts'

const toggleStore = useToggleStore()
const flashcardSetStore = useFlashcardSetStore()

await waitUntilStoreLoaded(flashcardSetStore)

const { isNoFlashcardSets } = storeToRefs(flashcardSetStore)

function welcomeText(): string {
  if (isNoFlashcardSets.value) {
    return 'Welcome to Control Panel! We wish you a happy journey!'
  } else {
    return 'Welcome back! We hope you\'re having a blast!'
  }
}

function buttonText(): string {
  if (isNoFlashcardSets.value) {
    return 'Create Your First Flashcard Set'
  } else {
    return 'Create New Flashcard Set'
  }
}

function toggleModal() {
  toggleStore.toggleFlashcardSetCreation()
}
</script>

<style scoped>
.welcome-widget {
  width: 220px;
  height: fit-content;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.welcome-text {
  font-size: 1rem;
  font-weight: 500;
  color: var(--cp--text--color);
  text-align: center;
  white-space: wrap;
  word-spacing: 0.02rem;
  letter-spacing: 0.02rem;
}

.welcome-button {
  --smart-button--title--font-size: 0.9rem;
  --smart-button--title--letter-spacing: 0.1rem;
  --smart-button--border-radius: 6px;
  --smart-button--bg: #32334a;
  --smart-button--bg--hover: #494a6c;
  --smart-button--width: 200px;
  --smart-button--height: 60px;
}
</style>
