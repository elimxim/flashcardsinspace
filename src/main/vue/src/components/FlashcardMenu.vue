<template>
  <ul class="menu">
    <li class="menu-item menu-item-color" style="display: flex;">
      <select v-model="currFlashcardSet" class="menu-select">
        <option v-for="s in flashcardSets" :key="s.id" :value="s">
          {{ truncate(s.name, 10) }}
        </option>
      </select>
    </li>
    <li class="menu-buttons-container">
      <div class="menu-item menu-item-color menu-button"
           @click="showFlashcardSetSettingsForm = true">
        <font-awesome-icon icon="fa-solid fa-gear"/>
      </div>
      <div class="menu-item menu-item-color menu-button"
           @click="showFlashcardSetCreationForm = true">
        <font-awesome-icon icon="fa-solid fa-box"/>
      </div>
      <div class="menu-item menu-item-color menu-button"
           @click="showFlashcardCreationForm = true">
        <font-awesome-icon icon="fa-solid fa-rectangle-list"/>
      </div>
    </li>
    <li v-if="showFlashcardMenuItem" class="menu-buttons-container">
      <div class="menu-item menu-item-color menu-calendar-button">
        <font-awesome-icon icon="fa-solid fa-calendar-days"/>
      </div>
      <div class="menu-item menu-item-color">
        <ul class="menu-info">
          <li>Day</li>
          <li class="menu-info-number">{{ calendar.day }}</li>
        </ul>
      </div>
    </li>
    <li v-if="showFlashcardMenuItem" v-for="info in levelInfos" :key="info.name"
        class="menu-item menu-item-color">
      <ul class="menu-info">
        <li class="menu-info-number">{{ info.count }}</li>
        <li>{{ info.name }}</li>
      </ul>
    </li>
  </ul>

  <FlashcardSetSettingsForm v-model:visible="showFlashcardSetSettingsForm"/>
  <FlashcardSetCreationForm v-model:visible="showFlashcardSetCreationForm"/>
  <FlashcardModificationForm v-model:visible="showFlashcardCreationForm" title="New flashcard"/>
</template>

<script setup lang="ts">
import FlashcardSetSettingsForm from '@/components/FlashcardSetSettingsForm.vue';
import FlashcardSetCreationForm from '@/components/FlashcardSetCreationForm.vue';
import FlashcardModificationForm from '@/components/FlashcardModificationForm.vue';
import { useFlashcardDataStore } from '@/stores/flashcard-data.ts'
import { useFlashcardStateStore } from '@/stores/flashcard-state.ts';
import { type LevelInfo } from '@/models/level-info.ts';
import { type FlashcardSet, Level } from '@/models/flashcard.ts';
import { storeToRefs } from 'pinia';
import { computed, ref } from 'vue';

const dataStore = useFlashcardDataStore()
const stateStore = useFlashcardStateStore()
const { flashcardSets } = storeToRefs(dataStore)

stateStore.chooseCurrFlashcardSet(flashcardSets)
const { currFlashcardSet } = storeToRefs(stateStore)

const showFlashcardMenuItem = computed(() => currFlashcardSet.value !== undefined)
const showFlashcardSetSettingsForm = ref(false)
const showFlashcardSetCreationForm = ref(false)
const showFlashcardCreationForm = ref(false)

const flashcardNumberByLevel = (level: number) => {
  const set = currFlashcardSet.value
  if (set === undefined) return 0
  return set.flashcards.filter(f => f.level.valueOf() === level).length
}

const totalFlashcardNumber = () => {
  const set = currFlashcardSet.value
  if (set === undefined) return 0
  return set.flashcards.length
}

const total: LevelInfo = {
  name: 'Total',
  count: computed(() => totalFlashcardNumber()),
}

const levelNumbers = Object.values(Level).filter(v => typeof v === 'number') as number[]
const levelInfos = [total].concat(
  levelNumbers.map(level => {
    return {
      name: `Level ${level}`,
      count: computed(() => flashcardNumberByLevel(level)),
    }
  })
)

function truncate(str: string, length: number) {
  return str.length > length ? `${str.slice(0, length)}...` : str;
}

// todo
const calendar = {
  day: 4 as number
}

</script>

<style scoped>
.menu {
  display: grid;
  list-style: none;
  gap: 4px;
  padding: 0;
  margin: 0;
}

.menu li {
  max-width: 200px;
}

.menu-item-color {
  background-color: #f0f0f0;
}

.menu-item {
  padding: 10px 10px 10px 10px;
  border-radius: 4px;
  font-size: 1em;
  user-select: none;
}

.menu-item:hover {
  background-color: #ddd;
}

.menu-info {
  display: flex;
  list-style: none;
  margin: 0;
  padding: 0;
}

.menu-info li {
  padding: 4px 10px 4px 10px;
}

.menu-info-number {
  background-color: #ddd;
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
  cursor: pointer;
  font-size: 1.2em;
}

.menu-calendar-button {
  flex: 1;
  text-align: center;
  cursor: pointer;
  font-size: 1.4em;
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
