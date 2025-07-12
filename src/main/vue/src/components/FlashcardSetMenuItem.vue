<template>
  <div class="flashcard-set-item">
    {{ flashcardSet.targetLanguage.name }}
  </div>
</template>

<script setup lang="ts">
import { defineProps } from 'vue'
import type { FlashcardSet } from '@/stores/model.ts'

defineProps<{
  flashcardSet: FlashcardSet
}>()
</script>

<style scoped>
.flashcard-set-item {
  font-size: 1.2em; /* Adjust size as needed */
  padding: 8px;
  background-color: #f0f0f0; /* Optional styling */
  border-radius: 4px;
}
</style>
