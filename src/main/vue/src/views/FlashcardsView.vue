<template>
  <div class="container">
    <div class="menu">
      <FlashcardSetMenu />
    </div>
  </div>

  <div class="levels">
    <ul class="levels-list">
      <li v-for="level in levels" :key="level" @click="selectLevel(level)">
        Level {{ level }}
      </li>
    </ul>
  </div>

  <div class="flashcards">

  </div>
</template>

<script setup>
import { ref } from 'vue'
import FlashcardSetMenu from '@/components/FlashcardSetMenu.vue'

import { Level } from '@/stores/model.ts'
// const levels = Object.values(Level).filter(v => typeof v === 'number') as number[]

// const selectLevel = (level: number) => {
//   console.log('Selected level:', level)
// }

</script>

<style scoped>
.container {
  display: flex;
  height: 100vh;
}

.menu {
  flex: 1; /* Small width for menu */
  border-right: 1px solid #ccc;
  overflow-y: auto;
}

.levels {
  flex: 0 0 150px; /* Fixed width or adjust as needed */
  display: flex;
  align-items: center;
  justify-content: center;
  border-right: 1px solid #ccc;
  padding: 10px;
}

.levels-list {
  display: flex;
  gap: 10px;
  list-style: none;
  margin: 0;
  padding: 0;
}

.levels-list li {
  padding: 8px 12px;
  background-color: #f0f0f0;
  cursor: pointer;
  border-radius: 4px;
  user-select: none;
}

.levels-list li:hover {
  background-color: #ddd;
}

.flashcards {
  flex: 3; /* Largest area */
  padding: 20px;
  overflow-y: auto;
}


</style>
