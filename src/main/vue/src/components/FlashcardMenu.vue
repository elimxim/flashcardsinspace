<template>
  <div class="menu-container">
    <ul class="menu">
      <li class="menu-item menu-select-item" style="display: flex;">
        <select id="flashcard-set-select" v-model="currFlashcardSet" class="menu-select" @change="handleSelectChange">
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
        <div class="menu-item menu-item-color menu-button">
          <font-awesome-icon icon="fa-solid fa-calendar-days"/>
        </div>
        <div class="menu-item menu-item-color">
          <ul class="menu-composite-item">
            <li>Day</li>
            <li class="menu-item-number">{{ calendar.day }}</li>
          </ul>
        </div>
      </li>
      <li v-if="showFlashcardMenuItem" v-for="info in levelInfos" :key="info.name"
          class="menu-item menu-item-color">
        <ul class="menu-composite-item">
          <li class="menu-item-number">{{ info.count }}</li>
          <li>{{ info.name }}</li>
        </ul>
      </li>
    </ul>
  </div>

  <FlashcardSetSettingsModalForm id="flashcard-set-settings"
                                 v-model:visible="showFlashcardSetSettingsForm"/>
  <FlashcardSetCreationModalForm id="new-flashcard-set"
                                 v-model:visible="showFlashcardSetCreationForm"/>
  <FlashcardModificationModalForm id="new-flashcard"
                                  v-model:visible="showFlashcardCreationForm"
                                  title="New flashcard"/>
</template>

<script setup lang="ts">
import FlashcardSetSettingsModalForm from '@/components/FlashcardSetSettingsModalForm.vue';
import FlashcardSetCreationModalForm from '@/components/FlashcardSetCreationModalForm.vue';
import FlashcardModificationModalForm from '@/components/FlashcardModificationModalForm.vue';
import { useFlashcardDataStore } from '@/stores/flashcard-data.ts'
import { useFlashcardStateStore } from '@/stores/flashcard-state.ts';
import { type FlashcardSet, Level } from '@/models/flashcard.ts';
import { storeToRefs } from 'pinia';
import { computed, onMounted, ref } from 'vue';
import { useReviewStateStore } from '@/stores/review-state.ts';

const dataStore = useFlashcardDataStore()
const { flashcardSets } = storeToRefs(dataStore)

const stateStore = useFlashcardStateStore()
stateStore.chooseCurr(flashcardSets)
const { currFlashcardSet } = storeToRefs(stateStore)

const showFlashcardMenuItem = computed(() => currFlashcardSet.value !== undefined)
const showFlashcardSetSettingsForm = ref(false)
const showFlashcardSetCreationForm = ref(false)
const showFlashcardCreationForm = ref(false)

const flashcardNumberByLevel = (level: number) => {
  return stateStore.flashcards.filter(f => f.level.valueOf() === level).length
}

const totalFlashcardNumber = () => {
  return stateStore.flashcards.length
}

const total = {
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

const reviewStateStore = useReviewStateStore()

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
  padding: 6px;
}

.menu-composite-item {
  display: flex;
  gap: 4px;
  list-style: none;
  margin: 0;
  padding: 0;
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
