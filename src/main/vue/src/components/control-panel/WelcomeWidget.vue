<template>
  <div class="welcome-widget welcome-widget--theme">
    <div class="welcome-text">
      {{ welcomeText() }}
    </div>
    <SmartButton
      :text="buttonText()"
      class="welcome-button"
      :on-click="toggleModal"
      fill-width
      fill-height
    />
  </div>
</template>

<script setup lang="ts">
import SmartButton from '@/components/SmartButton.vue'
import { useToggleStore } from '@/stores/toggle-store.ts'
import { useFlashcardSetStore } from '@/stores/flashcard-set-store.ts'
import { storeToRefs } from 'pinia'

const toggleStore = useToggleStore()
const flashcardSetStore = useFlashcardSetStore()

const { isNoFlashcardSets } = storeToRefs(flashcardSetStore)

function welcomeText(): String {
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
.welcome-widget--theme {
  --w-widget--text--color: var(--welcome-widget--text--color, #32334a);
  --w-widget--button--bg: var(--welcome-widget--button--bg, #32334a);
  --w-widget--button--bg--hover: var(--welcome-widget--button--bg--hover, #494a6c);
}

.welcome-widget {
  width: 200px;
  height: 100px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.welcome-text {
  font-size: 0.9rem;
  font-weight: 500;
  color: var(--w-widget--text--color);
  text-align: center;
  white-space: wrap;
  word-spacing: 0.02rem;
  letter-spacing: 0.02rem;
}

.welcome-button {
  flex: 1;
  --smart-button--title--font-size: 0.9rem;
  --smart-button--title--letter-spacing: 0.1rem;
  --smart-button--border-radius: 6px;
  --smart-button--bg: var(--w-widget--button--bg);
  --smart-button--bg--hover: var(--w-widget--button--bg--hover);
}
</style>
