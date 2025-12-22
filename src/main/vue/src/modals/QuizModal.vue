<template>
  <Modal
    title="Quiz Settings"
    :visible="quizOpen"
    :on-press-exit="exit"
  >
    <div class="modal-main-area">
      <p class="quiz-description">
        Practice flashcards from selected stages.
        Missed cards repeat each round until you know them all.
        Doing this will help those tough cards stick better into your long-term memory.
      </p>
      <div v-if="latestUncompletedSessionId && showBanner" class="quiz-banner">
        <div class="quiz-banner-text">
          You have an uncompleted quiz session. Do you want to continue?
        </div>
        <div class="quiz-banner-button">
          <SmartButton
            text="Continue"
            :on-click="start"
            fill-height
            fill-width
            auto-blur
          />
        </div>
        <div class="quiz-banner-exit">
          <AwesomeButton
            icon="fa-solid fa-xmark"
            :on-click="hideBanner"
          />
        </div>
      </div>
      <div class="modal-main-area--inner">
        <div class="quiz-stage-grid">
          <div
            class="quiz-stage-grid-row"
            :class="{ 'quiz-stage-grid-row--selected': includeUnknown }"
          >
            <div class="quiz-stage-checkbox">
              <SmartCheckbox
                v-model="includeUnknown"
                :label="specialStages.UNKNOWN.displayName"
              />
            </div>
            <div class="quiz-stage-flashcard-count">
              {{ unknownCount }}
            </div>
          </div>
          <div
            class="quiz-stage-grid-row"
            :class="{ 'quiz-stage-grid-row--selected': includeAttempted }"
          >
            <div class="quiz-stage-checkbox">
              <SmartCheckbox
                v-model="includeAttempted"
                :label="specialStages.ATTEMPTED.displayName"
              />
            </div>
            <div class="quiz-stage-flashcard-count">
              {{ attemptedCount }}
            </div>
          </div>
          <div
            class="quiz-stage-grid-row"
            :class="{ 'quiz-stage-grid-row--selected': includeOuterSpace }"
          >
            <div class="quiz-stage-checkbox">
              <SmartCheckbox
                v-model="includeOuterSpace"
                :label="specialStages.OUTER_SPACE.displayName"
              />
            </div>
            <div class="quiz-stage-flashcard-count">
              {{ outerSpaceCount }}
            </div>
          </div>
          <div
            class="quiz-stage-grid-row quiz-stage-grid-row--total"
            :class="{ 'quiz-stage-grid-row--selected': reviewCount > 0 }"
          >
            <div class="quiz-stage-flashcard-total">
              Total
            </div>
            <div class="quiz-stage-flashcard-count">
              {{ reviewCount }}
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
          :on-click="startNew"
          :disabled="reviewCount <= 0"
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
import AwesomeButton from '@/components/AwesomeButton.vue'
import { useToggleStore } from '@/stores/toggle-store.ts'
import { useRouter } from 'vue-router'
import { routeNames } from '@/router'
import { specialStages } from '@/core-logic/stage-logic.ts'
import { useFlashcardStore } from '@/stores/flashcard-store.ts'
import { useChronoStore } from '@/stores/chrono-store.ts'
import { storeToRefs } from 'pinia'
import { computed, onMounted, ref, watch } from 'vue'
import { countFlashcards, ReviewSessionType } from '@/core-logic/review-logic.ts'
import { sendLatestReviewSessionGetRequest } from '@/api/api-client.ts'
import { quizSessionIdCookie } from '@/utils/cookies-ref.ts'

const router = useRouter()
const toggleStore = useToggleStore()
const flashcardStore = useFlashcardStore()
const chronoStore = useChronoStore()

const { quizOpen } = storeToRefs(toggleStore)
const { flashcardSet } = storeToRefs(flashcardStore)
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

const reviewCount = computed(() => {
  let count = 0
  if (includeUnknown.value) {
    count += unknownCount.value
  }
  if (includeAttempted.value) {
    count += attemptedCount.value
  }
  if (includeOuterSpace.value) {
    count += outerSpaceCount.value
  }
  return count
})

const latestUncompletedSessionId = ref<number>()
const showBanner = ref(true)

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

function startNew() {
  start(true)
}

function start(fresh: boolean = false) {
  router.push({
    name: routeNames.review,
    query: {
      sessionType: ReviewSessionType.QUIZ,
      sessionId: fresh ? undefined : latestUncompletedSessionId.value,
      stages: reviewStages()
    }
  })
  exit()
}

function hideBanner() {
  showBanner.value = false
}

async function getLatestUncompletedQuizSessionId(): Promise<number | undefined> {
  if (!flashcardSet.value) return undefined
  return await sendLatestReviewSessionGetRequest(flashcardSet.value.id, ReviewSessionType.QUIZ)
    .then((response) => {
      if (response.status === 204) return undefined
      const overallTotal = response.data.metadata?.overallTotalCount ?? 0
      const overallCorrect = response.data.metadata?.overallCorrectCount ?? 0
      if (overallTotal != overallCorrect) {
        return response.data.id
      }
    })
    .catch((error) => {
      console.error(`Failed to fetch latest quiz session`, error.response?.data)
      return undefined
    })
}

watch(flashcardSet, async (newVal) => {
  if (newVal) {
    latestUncompletedSessionId.value = await getLatestUncompletedQuizSessionId()
  }
})

watch(quizSessionIdCookie, async (newVal) => {
  if (newVal) {
    latestUncompletedSessionId.value = await getLatestUncompletedQuizSessionId()
  }
})

onMounted(async () => {
  latestUncompletedSessionId.value = await getLatestUncompletedQuizSessionId()
})

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

.quiz-description {
  margin: 0;
  padding: 6px;
  font-size: 0.9rem;
  line-height: 1.4;
  color: #4a5568;
  background: #f0f4f8;
  border-radius: 6px;
  border-left: 3px solid #007bff;
  border-right: 3px solid #007bff;
}

.quiz-stage-grid {
  display: grid;
  grid-template-rows: repeat(4, 1fr);
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}

.quiz-stage-grid-row {
  grid-column: span 2;
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  padding: 4px;
  border: 1px solid #b8c4d6;
  border-radius: 6px;
  transition: border-color 0.2s ease-in-out;
}

.quiz-stage-grid-row--total {
  grid-column: 2;
  margin-top: 4px;
}

@media (hover: hover) {
  .quiz-stage-grid-row:not(.quiz-stage-grid-row--selected):not(.quiz-stage-grid-row--total):hover {
    border-color: #007BFFFF;
  }

  .quiz-stage-grid-row:has(.quiz-stage-grid-row--selected):hover {
    border-color: #0056B3FF;
  }
}

.quiz-stage-grid-row--selected {
  border-color: #007BFFFF;
}

.quiz-stage-checkbox {
  flex: 1;
  font-size: 1rem;
  --smart-checkbox--color-unchecked: #007BFFFF;
  --smart-checkbox--color-unchecked--hover: #0056B3FF;
  --smart-checkbox--color-checked: #0056B3FF;
  --smart-checkbox--color-checked--hover: #007BFFFF;
}

.quiz-stage-flashcard-count {
  font-size: 0.85rem;
  font-weight: 600;
  color: rgba(13, 18, 74, 0.6);
  background: rgb(225, 228, 240);
  border-radius: 3px;
  padding: 2px;
  width: 40px;
  text-align: center;
}

.quiz-stage-flashcard-total {
  color: #45454a;
  font-size: 1rem;
  letter-spacing: 0.05rem;
  word-spacing: 0.05rem;
  text-transform: uppercase;
}

.quiz-banner {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr auto;
  grid-template-rows: auto;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  background: #4098f6;
  border-radius: 6px;
  padding: 4px;
}

.quiz-banner-text {
  grid-column: 1 / 3;
  padding: 6px;
  font-size: 0.9rem;
  line-height: 1.4;
  color: #373737;
}

.quiz-banner-button {
  width: 100%;
  height: 100%;
  --smart-button--bg: transparent;
  --smart-button--border-color: #236bbc;
  --smart-button--border-width: 2px;
  --smart-button--border-radius: 6px;
  --smart-button--bg--hover: #236bbc;
  padding: 4px;
}

.quiz-banner-exit {
  align-self: flex-start;
  --awesome-button--icon--size: 1rem;
  --awesome-button--icon--color: #9bcdff;
  --awesome-button--icon--color--hover: #cfe9fb;
}
</style>
