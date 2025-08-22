<template>
  <div class="menu-container">
    <ul class="menu-list">
      <li class="menu-item menu-select-item">
        <select class="menu-select"
                ref="menuSelect"
                v-model="selectedFlashcardSet"
                :disabled="isNoFlashcardSets">
          <option v-for="s in flashcardSets" :key="s.id" :value="s">
            {{ truncate(s.name, 10) }}
          </option>
        </select>
      </li>
      <li class="menu-buttons-container">
        <div class="menu-item menu-item-color menu-icon-button"
             :class="{ 'menu-icon-disabled-button': isNoFlashcardSets || reviewStarted }"
             @click="onFlashcardSetSettingsClick">
          <font-awesome-icon icon="fa-solid fa-gear"/>
        </div>
        <div class="menu-item menu-item-color menu-icon-button"
             :class="{ 'menu-icon-disabled-button': reviewStarted }"
             @click="onFlashcardSetCreationClick">
          <font-awesome-icon icon="fa-solid fa-box"/>
        </div>
        <div class="menu-item menu-item-color menu-icon-button"
             :class="{ 'menu-icon-disabled-button': isNoFlashcardSets || reviewStarted }"
             @click="onFlashcardCreationClick">
          <font-awesome-icon icon="fa-solid fa-rectangle-list"/>
        </div>
      </li>
      <li class="menu-buttons-container"
          v-if="showFlashcardMenuItem">
        <div class="menu-item menu-item-color menu-icon-button"
             :class="{ 'menu-icon-disabled-button': reviewStarted }"
             @click="onCalendarClick">
          <font-awesome-icon icon="fa-solid fa-calendar-days"/>
        </div>
        <div class="menu-item menu-item-color">
          <ul class="menu-composite-item">
            <li>Day</li>
            <li class="menu-item-number">{{ currDay.seqNumber }}</li>
          </ul>
        </div>
      </li>
      <li class="menu-item menu-item-color"
          v-if="showFlashcardMenuItem">
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
            @click="b.reviewable ? startStageReview(b.stage) : null">
          <li class="menu-item-number">{{ b.flashcardNumber }}</li>
          <li>{{ b.stage.displayName }}</li>
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
  <CalendarModalForm
    v-model:visible="calendarModalFormOpen"/>
</template>

<script setup lang="ts">
import FlashcardSetSettingsModalForm from '@/components/modal/FlashcardSetSettingsModalForm.vue'
import FlashcardSetCreationModalForm from '@/components/modal/FlashcardSetCreationModalForm.vue'
import FlashcardModificationModalForm from '@/components/modal/FlashcardModificationModalForm.vue'
import CalendarModalForm from '@/components/modal/CalendarModalForm.vue'
import { useFlashcardDataStore } from '@/stores/flashcard-data-store.ts'
import { useFlashcardSetStore } from '@/stores/flashcard-set-store.ts'
import { useChronoStore } from '@/stores/chrono-store.ts'
import { storeToRefs } from 'pinia'
import { computed, type ComputedRef, onMounted, ref } from 'vue'
import { useReviewStore } from '@/stores/review-store.ts'
import { useGlobalStore } from '@/stores/global-store.ts'
import { truncate } from '@/utils/string.ts'
import { allStages, type Stage, specialStageSet } from '@/core-logic/stage-logic.ts'
import { countFlashcards } from '@/core-logic/review-logic.ts'

const globalStore = useGlobalStore()
const flashcardDataStore = useFlashcardDataStore()
const flashcardSetStore = useFlashcardSetStore()
const reviewStore = useReviewStore()
const chronoStore = useChronoStore()

const { flashcardSets, isEmpty: isNoFlashcardSets } = storeToRefs(flashcardDataStore)
const { flashcardSet } = storeToRefs(flashcardSetStore)
const { currDay } = storeToRefs(chronoStore)
const { started: reviewStarted } = storeToRefs(reviewStore)
const {
  flashcardSetSettingsModalFormOpen,
  flashcardSetCreationModalFormOpen,
  flashcardCreationModalFormOpen,
  calendarModalFormOpen
} = storeToRefs(globalStore)

const showFlashcardMenuItem = computed(() => flashcardSet.value !== null)
const menuSelect = ref<HTMLSelectElement>()

// buckets>

interface Bucket {
  stage: Stage
  flashcardNumber: ComputedRef<number>
  reviewable: boolean
}

const totalFlashcardNumber = computed(() => flashcardSetStore.flashcards.length)

const buckets: Bucket[] = allStages.map(stage => {
  return {
    stage: stage,
    flashcardNumber: computed(() => flashcardNumberByStage(stage)),
    reviewable: specialStageSet.has(stage),
  }
})

function flashcardNumberByStage(stage: Stage): number {
  return countFlashcards(flashcardSetStore.flashcards, stage)
}

// <buckets

const selectedFlashcardSet = computed({
  get: () => flashcardSetStore.flashcardSet,
  set: (newValue) => {
    if (newValue !== null) {
      flashcardSetStore.loadData(newValue)
        .then(() => chronoStore.loadData(newValue))
        .then(() => reviewStore.finishReview())
    }
  }
})

function onFlashcardSetSettingsClick() {
  if (!isNoFlashcardSets.value && !reviewStarted.value) {
    globalStore.toggleFlashcardSetSettingsModalForm()
  }
}

function onFlashcardSetCreationClick() {
  if (!reviewStarted.value) {
    globalStore.toggleFlashcardSetCreationModalForm()
  }
}

function onFlashcardCreationClick() {
  if (!isNoFlashcardSets.value && !reviewStarted.value) {
    globalStore.toggleFlashcardCreationModalForm()
  }
}

function onCalendarClick() {
  if (!reviewStarted.value) {
    globalStore.toggleCalendarModalForm()
  }
}

function startStageReview(stage: Stage) {
  reviewStore.startSpecialReview(stage)
}

onMounted(() => {
  menuSelect.value?.addEventListener('keydown', function (event) {
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

.menu-item:not(.menu-select-item, .menu-icon-disabled-button):hover {
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

.menu-icon-disabled-button {
  color: #ccc;
  cursor: default;
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
