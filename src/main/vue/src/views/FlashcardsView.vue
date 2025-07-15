<template>
  <div>
    <div class="flashcard-menu">
      <FlashcardMenu/>
    </div>

    <div class="flashcard-view">

    </div>
  </div>
</template>

<script setup lang="ts">
import FlashcardMenu from '@/components/FlashcardMenu.vue'
import { useFlashcardDataStore } from "@/stores/flashcard-data.ts";
import { useFlashcardStateStore } from "@/stores/flashcard-state.ts";
import { computed } from 'vue';
import { storeToRefs } from 'pinia';

const dataStore = useFlashcardDataStore()
const sateStore = useFlashcardStateStore()

</script>

<style scoped>
.flashcard-menu {
  flex: 1;
  display: flex;
  justify-content: left;
  padding: 0;
}

.flashcard-view {
  flex: 3;
  padding: 0;
  overflow-y: auto;
}


</style>
