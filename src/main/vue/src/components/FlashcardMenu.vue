<template>
  <div class="container">
    <ul class="menu">
      <li class="item select-item" style="display: flex;">
        <select v-model="currFlashcardSet">
          <option v-for="s in flashcardSets" :key="s.id" :value="s">
            {{ truncate(s.name, 10) }}
          </option>
        </select>
      </li>
      <li class="buttons-container">
        <div class="item item-color button"
             @click="showFlashcardSetSettingsForm = true">
          <font-awesome-icon icon="fa-solid fa-gear"/>
        </div>
        <div class="item item-color button"
             @click="showFlashcardSetCreationForm = true">
          <font-awesome-icon icon="fa-solid fa-box"/>
        </div>
        <div class="item item-color button"
             @click="showFlashcardCreationForm = true">
          <font-awesome-icon icon="fa-solid fa-rectangle-list"/>
        </div>
      </li>
      <li v-if="showFlashcardMenuItem" class="buttons-container">
        <div class="item item-color button calendar-button">
          <font-awesome-icon icon="fa-solid fa-calendar-days"/>
        </div>
        <div class="item item-color">
          <ul class="day-info">
            <li>Day</li>
            <li class="number">{{ calendar.day }}</li>
          </ul>
        </div>
      </li>
      <li v-if="showFlashcardMenuItem" v-for="info in levelInfos" :key="info.name"
          class="item item-color">
        <ul class="day-info">
          <li class="number">{{ info.count }}</li>
          <li>{{ info.name }}</li>
        </ul>
      </li>
    </ul>
  </div>

  <FlashcardSetSettingsModalForm v-model:visible="showFlashcardSetSettingsForm"/>
  <FlashcardSetCreationModalForm v-model:visible="showFlashcardSetCreationForm"/>
  <FlashcardModificationModalForm v-model:visible="showFlashcardCreationForm"
                                  title="New flashcard"/>
</template>

<script setup lang="ts">
import FlashcardSetSettingsModalForm from '@/components/FlashcardSetSettingsModalForm.vue';
import FlashcardSetCreationModalForm from '@/components/FlashcardSetCreationModalForm.vue';
import FlashcardModificationModalForm from '@/components/FlashcardModificationModalForm.vue';
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
.container {
  display: flex;
}

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

.item-color {
  background-color: #e6e6e6;
}

.item {
  padding: 10px 10px 10px 10px;
  border-radius: 4px;
  font-size: 1em;
  user-select: none;
}

.item:not(.select-item):hover {
  background-color: #d6d6d6;
}

.select-item {
  padding: 6px 6px 6px 6px;
}

.day-info {
  display: flex;
  list-style: none;
  margin: 0;
  padding: 0;
}

.day-info li {
  padding: 4px 10px 4px 10px;
}

.number {
  background-color: #ccc;
  border-radius: 4px;
}

.buttons-container {
  display: flex;
  gap: 4px;
  background: none;
}

.button {
  flex: 1;
  text-align: center;
  cursor: pointer;
  font-size: 1.2em;
}

.calendar-button {
  font-size: 1.4em;
}

select {
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
