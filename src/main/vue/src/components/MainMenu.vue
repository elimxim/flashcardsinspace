<template>
  <div class="menu-area">
    <ul class="menu-list">
      <li class="menu-item menu-select-item">
        <select class="menu-select"
                ref="menuSelect"
                v-model="flashcardSet"
                @change="handleSelectChange">
          <option v-for="s in flashcardSets" :key="s.id" :value="s">
            {{ truncate(s.name, 10) }}
          </option>
        </select>
      </li>
      <li class="menu-buttons-container">
        <div class="menu-item menu-item-color menu-icon-button"
             @click="globalStateStore.toggleFlashcardSetSettingsModalForm()">
          <font-awesome-icon icon="fa-solid fa-gear"/>
        </div>
        <div class="menu-item menu-item-color menu-icon-button"
             @click="globalStateStore.toggleFlashcardSetCreationModalForm()">
          <font-awesome-icon icon="fa-solid fa-box"/>
        </div>
        <div class="menu-item menu-item-color menu-icon-button"
             @click="globalStateStore.toggleFlashcardCreationModalForm()">
          <font-awesome-icon icon="fa-solid fa-rectangle-list"/>
        </div>
      </li>
      <li class="menu-buttons-container"
          v-if="showFlashcardMenuItem">
        <div class="menu-item menu-item-color menu-icon-button">
          <font-awesome-icon icon="fa-solid fa-calendar-days"/>
        </div>
        <div class="menu-item menu-item-color">
          <ul class="menu-composite-item">
            <li>Day</li>
            <li class="menu-item-number">{{ 40 }}</li>
          </ul>
        </div>
      </li>
      <li class="menu-item menu-item-color">
        <ul class="menu-composite-item">
          <li class="menu-item-number">{{ totalFlashcardNumber }}</li>
          <li>Total</li>
        </ul>
      </li>
      <li class="menu-item menu-item-color"
          v-if="showFlashcardMenuItem"
          v-for="b in buckets" :key="b.stage.name">
        <ul class="menu-composite-item"
            :class="{ 'menu-button': b.reviewable }"
            @click="b.reviewable ? startReview(b.stage) : null">
          <li class="menu-item-number">{{ b.flashcardNumber }}</li>
          <li>{{ b.stage.name }}</li>
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
import { computed, type ComputedRef, onMounted, ref } from 'vue'
import { useReviewStateStore } from '@/stores/review-state.ts'
import { useGlobalStateStore } from '@/stores/global-state.ts'
import { truncate } from '@/utils/string.ts'
import { allStages, type Stage, specialStages } from '@/core-logic/stage-logic.ts'

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

const menuSelect = ref<HTMLSelectElement>()

// buckets>

interface Bucket {
  stage: string
  flashcardNumber: ComputedRef<number>
  reviewable: boolean
}

const totalFlashcardNumber = computed(() => flashcardStateStore.flashcards.length)

const buckets = allStages.map(stage => {
  return {
    stage: stage,
    flashcardNumber: computed(() => flashcardNumberByStage(stage)),
    reviewable: specialStages.has(stage),
  }
})

function flashcardNumberByStage(stage: Stage): number {
  return flashcardStateStore.flashcards.filter(f => f.stage === stage.name).length
}

// <buckets

function handleSelectChange() {
  reviewStateStore.finishReview()
}

function startReview(stage: Stage) {
  reviewStateStore.startSpecialReview(stage)
}

onMounted(() => {
  menuSelect.value?.addEventListener('keydown', function (event) {
    event.preventDefault()
  })
});

</script>

<style scoped>
.menu-area {
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
  cursor: pointer;
}

.menu-icon-button {
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
