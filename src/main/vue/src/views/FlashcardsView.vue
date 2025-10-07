<template>
  <div class="flashcards-container">
    <div class="menu-area">
      <MainMenu/>
    </div>

    <div class="main-area">
      <ReviewFormStarter/>
    </div>
  </div>
</template>

<script setup lang="ts">
import MainMenu from '@/components/MainMenu.vue'
import ReviewFormStarter from '@/components/ReviewFormStarter.vue'
import { storeToRefs } from 'pinia'
import { useFlashcardSetsStore } from '@/stores/flashcard-sets-store.ts'
import { useChronoStore } from '@/stores/chrono-store.ts'
import { useFlashcardSetStore } from '@/stores/flashcard-set-store.ts'

const flashcardSetsStore = useFlashcardSetsStore()
const flashcardSetStore = useFlashcardSetStore()
const chronoStore = useChronoStore()

const { firstFlashcardSet } = storeToRefs(flashcardSetsStore)
const { flashcardSet } = storeToRefs(flashcardSetStore)

flashcardSetsStore.loadFlashcardSets().then(async () => {
  console.log('flashcard data loaded')
  if (firstFlashcardSet.value !== null) {
    await flashcardSetStore.loadFlashcardsFor(firstFlashcardSet.value)
  } else {
    flashcardSetStore.resetState()
  }
}).then(async () => {
  console.log('flashcard set initialized')
  if (flashcardSet.value !== null) {
    await chronoStore.loadChronodays(flashcardSet.value)
    console.log('chrono data loaded')
  } else {
    console.log('flashcard set hasn\'t been initialized')
  }
})

</script>

<style scoped>
.flashcards-container {
  display: flex;
  flex-direction: row;
  min-height: 92vh;
}

.menu-area {
  flex: 1;
  background-color: #f0f0f0;
}

.main-area {
  flex: 10;
  background: none;
}

</style>
