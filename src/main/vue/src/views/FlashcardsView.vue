<template>
  <div class="flashcards-container">
    <div class="menu-area">
      <MainMenu/>
    </div>

    <div class="main-area">
      <ReviewFormStarter/>
    </div>
  </div>
  <SpaceToast/>
</template>

<script setup lang="ts">
import MainMenu from '@/components/MainMenu.vue'
import ReviewFormStarter from '@/components/ReviewFormStarter.vue'
import SpaceToast from '@/components/SpaceToast.vue'
import {
  determineCurrFlashcardSet,
  loadFlashcardAndChronoStores,
  loadFlashcardSetsStore,
} from '@/shared/stores.ts'


loadFlashcardSetsStore()
  .then(async (loaded) => {
    if (loaded) {
      const currFlashcardSet = determineCurrFlashcardSet()
      if (currFlashcardSet) {
        await loadFlashcardAndChronoStores(currFlashcardSet)
      }
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
