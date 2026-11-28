<template>
  <div :class="['page', 'page--bg--light', 'flex-row']">
    <FlashcardsGallery />
  </div>
  <SpaceToast />
</template>

<script setup lang="ts">
import FlashcardsGallery from '@/components/gallery/FlashcardsGallery.vue'
import SpaceToast from '@/components/common/SpaceToast.vue'
import { onMounted } from 'vue'
import { useFlashcardSetLoading } from '@/stores/flashcard-set-loading.ts'

const flashcardSetLoading = useFlashcardSetLoading()

onMounted(async () => {
  await flashcardSetLoading.loadCurrFlashcardSet()
})
</script>
