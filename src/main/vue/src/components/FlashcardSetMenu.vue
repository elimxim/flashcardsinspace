<template>
  <ul>
    <li v-for="s in flashcardSets" :key="s.id">
      <FlashcardSetMenuItem :flashcardSet="s" />
    </li>
  </ul>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useFlashcardStore } from '@/stores/flashcards.ts'
import FlashcardSetMenuItem from '@/components/FlashcardSetMenuItem.vue'

const flashcardStore = useFlashcardStore()
const flashcardSets = ref([])

onMounted(() => {
  flashcardSets.value = flashcardStore.loadFlashcardSets()
})
</script>
