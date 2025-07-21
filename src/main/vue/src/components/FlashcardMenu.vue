<template>
  <div class="menu-container">
    <ul class="menu-list">
      <li class="menu-item menu-select-item">
        <select id="flashcard-set-select" class="menu-select"
                v-model="flashcardSet"
                @change="handleSelectChange">
          <option v-for="s in flashcardSets" :key="s.id" :value="s">
            {{ truncate(s.name, 10) }}
          </option>
        </select>
      </li>
      <li class="menu-buttons-container">
        <div class="menu-item menu-item-color menu-button"
             @click="globalStateStore.toggleFlashcardSetSettingsModalForm()">
          <font-awesome-icon icon="fa-solid fa-gear"/>
        </div>
        <div class="menu-item menu-item-color menu-button"
             @click="globalStateStore.toggleFlashcardSetCreationModalForm()">
          <font-awesome-icon icon="fa-solid fa-box"/>
        </div>
        <div class="menu-item menu-item-color menu-button"
             @click="globalStateStore.toggleFlashcardCreationModalForm()">
          <font-awesome-icon icon="fa-solid fa-rectangle-list"/>
        </div>
      </li>
      <li class="menu-buttons-container"
          v-if="showFlashcardMenuItem">
        <div class="menu-item menu-item-color menu-button">
          <font-awesome-icon icon="fa-solid fa-calendar-days"/>
        </div>
        <div class="menu-item menu-item-color">
          <ul class="menu-composite-item">
            <li>Day</li>
            <li class="menu-item-number">{{ 40 }}</li>
          </ul>
        </div>
      </li>
      <li class="menu-item menu-item-color"
          v-if="showFlashcardMenuItem"
          v-for="b in buckets" :key="b.name">
        <ul class="menu-composite-item">
          <li class="menu-item-number">{{ b.count }}</li>
          <li>{{ b.name }}</li>
        </ul>
      </li>
    </ul>
  </div>

  <FlashcardSetSettingsModalForm
    v-model:visible="flashcardSetSettingsModalFormOpen"/>
  <FlashcardSetCreationModalForm
    v-model:visible="flashcardSetCreationModalFormOpen"/>
  <FlashcardModificationModalForm
    v-model:visible="flashcardCreationModalFormOpen"/>
</template>

<script setup lang="ts">
import FlashcardSetSettingsModalForm from '@/components/modal/FlashcardSetSettingsModalForm.vue'
import FlashcardSetCreationModalForm from '@/components/modal/FlashcardSetCreationModalForm.vue'
import FlashcardModificationModalForm from '@/components/modal/FlashcardModificationModalForm.vue'
import { useFlashcardDataStore } from '@/stores/flashcard-data.ts'
import { useFlashcardStateStore } from '@/stores/flashcard-state.ts'
import { storeToRefs } from 'pinia'
import { computed, type ComputedRef, onMounted } from 'vue'
import { useReviewStateStore } from '@/stores/review-state.ts'
import { useGlobalStateStore } from '@/stores/global-state.ts'
import { truncate } from '@/utils/string.ts'

import { levelNames } from '@/core-logic/level-logic.ts';

const flashcardDataStore = useFlashcardDataStore()
const flashcardStateStore = useFlashcardStateStore()
const globalStateStore = useGlobalStateStore()
const reviewStateStore = useReviewStateStore()

const { flashcardSets } = storeToRefs(flashcardDataStore)
const { flashcardSet } = storeToRefs(flashcardStateStore)
const {
  flashcardSetSettingsModalFormOpen,
  flashcardSetCreationModalFormOpen,
  flashcardCreationModalFormOpen,
} = storeToRefs(globalStateStore)

flashcardStateStore.initFromList(flashcardSets.value)

const showFlashcardMenuItem = computed(() => flashcardSet.value !== null)

// buckets>

interface Bucket {
  name: string
  count: ComputedRef<number>
}

const total: Bucket = {
  name: 'Total',
  count: computed(() => flashcardStateStore.flashcards.length),
}

const buckets = [total].concat(
  levelNames.map(name => {
    return {
      name: name,
      count: computed(() => flashcardNumberByLevel(name)),
    }
  })
)

function flashcardNumberByLevel(levelName: string): number {
  return flashcardStateStore.flashcards.filter(f => f.level.name === levelName).length
}

// <buckets

function handleSelectChange() {
  reviewStateStore.finishReview()
}

onMounted(() => {
  const flashcardSetSelect = document.getElementById('flashcard-set-select') as HTMLSelectElement
  flashcardSetSelect.addEventListener('keydown', function (event) {
    event.preventDefault()
  })
});

</script>

<style scoped>
.menu-container {
  flex: 1;
  display: flex;
  padding: 4px;
  background-color: #f0f0f0;
}

.menu-list {
  display: grid;
  list-style: none;
  gap: 4px;
  padding: 0;
  margin: 0;
}

.menu-list li {
  max-width: 200px;
}

.menu-item-color {
  background-color: #e6e6e6;
}

.menu-item {
  padding: 8px;
  margin: 0;
  border-radius: 4px;
  font-size: 1em;
  user-select: none;
}

.menu-item:not(.menu-select-item):hover {
  background-color: #d6d6d6;
}

.menu-select-item {
  display: flex;
  padding: 6px;
}

.menu-composite-item {
  display: flex;
  gap: 4px;
  list-style: none;
  margin: 0;
  padding: 0;
  text-wrap: nowrap;
}

.menu-composite-item li {
  padding: 2px 10px 2px 10px;
}

.menu-item-number {
  background-color: #ccc;
  border-radius: 4px;
}

.menu-buttons-container {
  display: flex;
  gap: 4px;
  background: none;
}

.menu-button {
  flex: 1;
  text-align: center;
  padding: 10px;
  cursor: pointer;
  font-size: 1.2em;
}

.menu-select {
  flex: 1;
  appearance: none;
  border: 2px solid #ddd;
  border-radius: 4px;
  padding: 6px 10px 6px 10px;
  font-size: 1em;
  outline: none;
  cursor: pointer;
}

</style>
