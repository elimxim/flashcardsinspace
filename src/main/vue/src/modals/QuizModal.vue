<template>
  <Modal
    title="Quiz Settings"
    :visible="toggleStore.quizOpen"
    :on-press-exit="exit"
  >
    <div class="modal-main-area">
      <div class="modal-main-area--inner">
        <div class="stage-checklist">
          <div
            class="stage-checkbox-wrapper"
            :class="{ 'stage-checkbox-wrapper--selected': includeUnknown }"
          >
            <div class="stage-checkbox">
              <SmartCheckbox
                v-model="includeUnknown"
                :label="specialStages.UNKNOWN.displayName"
              />
            </div>
            <div class="stage-flashcard-count">
              {{ unknownCount }}
            </div>
          </div>
          <div
            class="stage-checkbox-wrapper"
            :class="{ 'stage-checkbox-wrapper--selected': includeAttempted }"
          >
            <div class="stage-checkbox">
              <SmartCheckbox
                v-model="includeAttempted"
                :label="specialStages.ATTEMPTED.displayName"
              />
            </div>
            <div class="stage-flashcard-count">
              {{ attemptedCount }}
            </div>
          </div>
          <div
            class="stage-checkbox-wrapper"
            :class="{ 'stage-checkbox-wrapper--selected': includeOuterSpace }"
          >
            <div class="stage-checkbox">
              <SmartCheckbox
                v-model="includeOuterSpace"
                :label="specialStages.OUTER_SPACE.displayName"
              />
            </div>
            <div class="stage-flashcard-count">
              {{ outerSpaceCount }}
            </div>
          </div>
        </div>
      </div>
      <div class="modal-control-buttons">
        <SmartButton
          class="off-button"
          text="Cancel"
          :on-click="cancel"
          auto-blur
        />
        <SmartButton
          class="calm-button"
          text="Start"
          :on-click="start"
          :disabled="totalCount <= 0"
          auto-blur
        />
      </div>
    </div>
  </Modal>
</template>

<script setup lang="ts">
import Modal from '@/components/Modal.vue'
import SmartButton from '@/components/SmartButton.vue'
import SmartCheckbox from '@/components/SmartCheckbox.vue'
import { useToggleStore } from '@/stores/toggle-store.ts'
import { useRouter } from 'vue-router'
import { routeNames } from '@/router'
import { specialStages } from '@/core-logic/stage-logic.ts'
import { useFlashcardStore } from '@/stores/flashcard-store.ts'
import { useChronoStore } from '@/stores/chrono-store.ts'
import { storeToRefs } from 'pinia'
import { computed, ref } from 'vue'
import { countFlashcards, ReviewMethod } from '@/core-logic/review-logic.ts'

const router = useRouter()
const toggleStore = useToggleStore()
const flashcardStore = useFlashcardStore()
const chronoStore = useChronoStore()

const { flashcards } = storeToRefs(flashcardStore)
const { currDay } = storeToRefs(chronoStore)

const includeUnknown = ref(false)
const includeAttempted = ref(false)
const includeOuterSpace = ref(false)

const reviewStages = (): string[] => {
  const stages = []
  if (includeUnknown.value) {
    stages.push(specialStages.UNKNOWN.name)
  }
  if (includeAttempted.value) {
    stages.push(specialStages.ATTEMPTED.name)
  }
  if (includeOuterSpace.value) {
    stages.push(specialStages.OUTER_SPACE.name)
  }
  return stages
}

const unknownCount = computed(() =>
  countFlashcards(flashcards.value, specialStages.UNKNOWN, currDay.value)
)
const attemptedCount = computed(() =>
  countFlashcards(flashcards.value, specialStages.ATTEMPTED, currDay.value)
)
const outerSpaceCount = computed(() =>
  countFlashcards(flashcards.value, specialStages.OUTER_SPACE, currDay.value)
)
const totalCount = computed(() =>
  unknownCount.value + attemptedCount.value + outerSpaceCount.value
)

function toggleModalForm() {
  toggleStore.toggleQuiz()
}

function resetState() {
  includeUnknown.value = false
  includeAttempted.value = false
  includeOuterSpace.value = false
}

function exit() {
  toggleModalForm()
  resetState()
}

async function cancel() {
  toggleModalForm()
}

function start() {
  router.push({
    name: routeNames.review,
    query: { mode: ReviewMethod.QUIZ, stages: reviewStages() }
  })
  exit()
}
</script>

<style scoped>
.modal-main-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 10px;
}

.modal-main-area--inner {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.modal-control-buttons {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  gap: 10px;
}

.stage-checklist {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.stage-checkbox-wrapper {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  padding: 4px;
  border: 1px solid rgb(225, 228, 240);
  border-radius: 6px;
  transition: background 0.2s ease-in-out;
}

.stage-checkbox-wrapper:not(.stage-checkbox-wrapper--selected):hover {
  background: rgb(225, 228, 240);
}

.stage-checkbox-wrapper:not(.stage-checkbox-wrapper--selected):hover .stage-flashcard-count {
  background: #fff;
}

.stage-checkbox-wrapper--selected {
  background: rgb(225, 228, 240);
}

.stage-checkbox-wrapper--selected .stage-flashcard-count {
  background: #fff;
}

.stage-checkbox {
  flex: 1;
  font-size: 1rem;
}

.stage-flashcard-count {
  font-size: 0.85rem;
  font-weight: 600;
  color: rgba(13, 18, 74, 0.6);
  background: rgb(225, 228, 240);
  border-radius: 3px;
  padding: 2px;
  width: 40px;
  text-align: center;
  transition: background 0.2s ease-in-out;
}
</style>
