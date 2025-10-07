<template>
  <div class="menu-container">
    <ul class="menu-list">
      <li class="menu-item menu-select-item">
        <select
          ref="menuSelect"
          v-model="selectedFlashcardSet"
          class="menu-select"
          :disabled="isNoFlashcardSets">
          <option v-for="s in flashcardSets" :key="s.id" :value="s">
            {{ truncate(s.name, 10) }}
          </option>
        </select>
      </li>
      <li class="menu-buttons-container">
        <div
          class="menu-item menu-item-color menu-icon-button"
          :class="{ 'menu-icon-disabled-button': isNoFlashcardSets }"
          @click="onFlashcardSetSettingsClick">
          <font-awesome-icon icon="fa-solid fa-gear"/>
        </div>
        <div
          class="menu-item menu-item-color menu-icon-button"
          @click="onFlashcardSetCreationClick">
          <font-awesome-icon icon="fa-solid fa-box"/>
        </div>
        <div
          class="menu-item menu-item-color menu-icon-button"
          :class="{ 'menu-icon-disabled-button': isNoFlashcardSets }"
          @click="onFlashcardCreationClick">
          <font-awesome-icon icon="fa-solid fa-rectangle-list"/>
        </div>
      </li>
      <li
        v-if="showFlashcardMenuItem"
        class="menu-buttons-container">
        <div
          class="menu-item menu-item-color menu-icon-button"
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
      <li
        v-if="showFlashcardMenuItem"
        class="menu-item menu-item-color">
        <ul class="menu-composite-item">
          <li class="menu-item-number">{{ totalFlashcardNumber }}</li>
          <li>Total</li>
        </ul>
      </li>
      <template v-if="showFlashcardMenuItem">
        <li
          v-for="b in buckets"
          :key="b.stage.name" class="menu-item menu-item-color">
          <ul
            class="menu-composite-item"
            :class="{ 'menu-button': b.reviewable }"
            @click="b.reviewable ? startStageReview(b.stage) : null">
            <li class="menu-item-number">{{ b.flashcardNumber }}</li>
            <li>{{ b.stage.displayName }}</li>
          </ul>
        </li>
      </template>
    </ul>
  </div>

  <FlashcardSetSettingsModal/>
  <FlashcardSetCreationModal/>
  <FlashcardModificationModal/>
  <CalendarModal/>
</template>

<script setup lang="ts">
import FlashcardSetSettingsModal from '@/views/modal/FlashcardSetSettingsModal.vue'
import FlashcardSetCreationModal from '@/views/modal/FlashcardSetCreationModal.vue'
import FlashcardModificationModal from '@/views/modal/FlashcardModificationModal.vue'
import CalendarModal from '@/views/modal/CalendarModal.vue'
import { useFlashcardSetsStore } from '@/stores/flashcard-sets-store.ts'
import { useFlashcardSetStore } from '@/stores/flashcard-set-store.ts'
import { useChronoStore } from '@/stores/chrono-store.ts'
import { storeToRefs } from 'pinia'
import { computed, type ComputedRef, onMounted, ref } from 'vue'
import { useModalStore } from '@/stores/modal-store.ts'
import { truncate } from '@/utils/string.ts'
import { allStages, type Stage, specialStageSet } from '@/core-logic/stage-logic.ts'
import { countFlashcards } from '@/core-logic/review-logic.ts'
import router, { routeNames } from '@/router'
import { saveSelectedSetId } from '@/shared/cookies.ts'

const modalStore = useModalStore()
const flashcardSetsStore = useFlashcardSetsStore()
const flashcardSetStore = useFlashcardSetStore()
const chronoStore = useChronoStore()

const { flashcardSets, isEmpty: isNoFlashcardSets } = storeToRefs(flashcardSetsStore)
const { flashcardSet } = storeToRefs(flashcardSetStore)
const { currDay } = storeToRefs(chronoStore)

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
  get: () => {
    const set = flashcardSetStore.flashcardSet
    if (set !== null) {
      saveSelectedSetId(set.id)
    }
    return set
  },
  set: (set) => {
    if (set !== null) {
      saveSelectedSetId(set.id)
      flashcardSetStore.loadFlashcardsFor(set)
        .then(() => chronoStore.loadChronodays(set))
    }
  }
})

function onFlashcardSetSettingsClick() {
  if (!isNoFlashcardSets.value) {
    modalStore.toggleFlashcardSetSettings()
  }
}

function onFlashcardSetCreationClick() {
  modalStore.toggleFlashcardSetCreation()
}

function onFlashcardCreationClick() {
  if (!isNoFlashcardSets.value) {
    modalStore.toggleFlashcardCreation()
  }
}

function onCalendarClick() {
  modalStore.toggleCalendar()
}

function startStageReview(stage: Stage) {
  router.push({ name: routeNames.review, query: { stage: stage.name } })
}

onMounted(() => {
  menuSelect.value?.addEventListener('keydown', function (event) {
    event.preventDefault()
  })
})

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
