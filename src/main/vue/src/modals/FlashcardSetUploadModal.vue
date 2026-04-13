<template>
  <Modal
    title="Upload Flashcards"
    :visible="toggleStore.fileUploadOpen"
    :on-exit="cancel"
  >
    <div class="modal-main-area">
      <div class="spinner-container">
        <KineticRingSpinner
          class="file-upload-spinner"
          :class="{
            'file-upload-spinner--success': uploadState === 'success',
            'file-upload-spinner--error': uploadState === 'error',
          }"
          :ring-size="160"
          :beam-speed="2.4"
          :freeze="uploadState !== 'parsing'"
        />
        <font-awesome-icon
          v-if="uploadState === 'parsing'"
          icon="fa-solid fa-gears"
          class="spinner-icon spinner-icon--parsing"
        />
        <font-awesome-icon
          v-else-if="uploadState === 'success'"
          icon="fa-solid fa-check"
          class="spinner-icon spinner-icon--success"
        />
        <font-awesome-icon
          v-if="uploadState === 'error'"
          icon="fa-solid fa-xmark"
          class="spinner-icon spinner-icon--error"
        />
      </div>
      <div class="info-section">
        <div class="count-text">
          {{ parsedCount }} flashcard{{ parsedCount !== 1 ? 's' : '' }} parsed
        </div>
        <div class="duplicate-text" :style="{ visibility: duplicateCount > 0 ? 'visible' : 'hidden' }">
          {{ duplicateCount }} duplicate{{ duplicateCount !== 1 ? 's' : '' }} skipped
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
        class="calm-button counter-button"
        :on-click="upload"
        :disabled="uploadState !== 'success' || uploadCount <= 0"
        auto-blur
      >
        <span class="counter-button--layout">
          <span class="counter-button--text">
            Upload
          </span>
          <span class="counter-button--number">
            {{ uploadCount }}
          </span>
        </span>
      </SmartButton>
    </div>
  </Modal>
</template>

<script setup lang="ts">
import Modal from '@/components/Modal.vue'
import SmartButton from '@/components/SmartButton.vue'
import KineticRingSpinner from '@/components/KineticRingSpinner.vue'
import { computed, onUnmounted, ref } from 'vue'
import { useToggleStore } from '@/stores/toggle-store.ts'
import { useEventStore } from '@/stores/event-store.ts'
import { useFlashcardStore } from '@/stores/flashcard-store.ts'
import { useChronoStore } from '@/stores/chrono-store.ts'
import { useFlashcardSetStore } from '@/stores/flashcard-set-store.ts'
import { storeToRefs } from 'pinia'
import { findDuplicates, newFlashcard } from '@/core-logic/flashcard-logic.ts'
import { sendFlashcardBulkCreationRequest } from '@/api/api-client.ts'
import { useSpaceToaster } from '@/stores/toast-store.ts'
import { userApiErrors } from '@/api/user-api-error.ts'
import { parseFlashcardsFromFile } from '@/core-logic/flashcard-file-logic.ts'
import { Log, LogTag } from '@/utils/logger.ts'
import { FlashcardContent } from '@/model/flashcard.ts'
import { sleep } from '@/utils/utils.ts'

type UploadState = 'parsing' | 'success' | 'error'

const toggleStore = useToggleStore()
const eventStore = useEventStore()
const flashcardStore = useFlashcardStore()
const chronoStore = useChronoStore()
const flashcardSetStore = useFlashcardSetStore()
const toaster = useSpaceToaster()

const { flashcardSet, flashcards } = storeToRefs(flashcardStore)
const { currDay } = storeToRefs(chronoStore)

const uploadState = ref<UploadState>('parsing')
const parsedRows = ref<FlashcardContent[]>([])
const duplicateIndices = ref<Set<number>>(new Set())

const parsedCount = computed(() => parsedRows.value.length)
const duplicateCount = computed(() => duplicateIndices.value.size)
const uploadCount = computed(() =>
  parsedRows.value.filter((_, i) => !duplicateIndices.value.has(i)).length
)

async function parseFile(file: File) {
  uploadState.value = 'parsing'
  parsedRows.value = []
  duplicateIndices.value = new Set()

  try {
    await sleep(2000)
    const rows = await parseFlashcardsFromFile(file)
    parsedRows.value = rows
    duplicateIndices.value = findDuplicates(rows, flashcards.value)

    if (parsedRows.value.length === duplicateIndices.value.size) {
      uploadState.value = 'error'
      Log.error(LogTag.LOGIC, 'All flashcards are duplicates')
      toaster.bakeError(userApiErrors.FLASHCARD__ALL_DUPLICATES)
    } else {
      uploadState.value = 'success'
    }
  } catch {
    Log.error(LogTag.LOGIC, 'Failed to parse uploaded file')
    toaster.bakeError(userApiErrors.FLASHCARD__FILE_PARSE_FAILED)
    uploadState.value = 'error'
  }
}

async function upload() {
  if (!flashcardSet.value) return
  const setId = flashcardSet.value.id
  const today = currDay.value.chronodate

  const uniqueFlashcards = parsedRows.value.filter((_, i) => !duplicateIndices.value.has(i))

  if (uniqueFlashcards.length === 0) {
    toaster.bakeError(userApiErrors.FLASHCARD__ALL_DUPLICATES)
    return
  }

  const newFlashcards = uniqueFlashcards.map(r => newFlashcard(r.frontSide, r.backSide, today))

  await sendFlashcardBulkCreationRequest(setId, newFlashcards)
    .then((response) => {
      for (const flashcard of response.data) {
        flashcardStore.addNewFlashcard(flashcard)
        flashcardSetStore.incrementFlashcardsNumber(setId)
      }
      toaster.bakeSuccess('Success', `${response.data.length} flashcard${response.data.length !== 1 ? 's' : ''} uploaded`, 400)
      toggleStore.toggleFileUpload()
    })
    .catch((error) => {
      Log.error(LogTag.LOGIC, `Failed to bulk-create flashcards for FlashcardSet.id=${setId}`, error)
      toaster.bakeError(userApiErrors.FLASHCARD__BULK_CREATION_FAILED, error.response?.data)
    })
}

function cancel() {
  toggleStore.toggleFileUpload()
}

const unsubscribeFileSelected = eventStore.subscribe('flashcard-file:selected', (file) => {
  parseFile(file)
})

onUnmounted(() => {
  unsubscribeFileSelected()
})

</script>

<style scoped>
.modal-main-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding: 10px 0;
}

.spinner-container {
  position: relative;
  width: 200px;
  height: 200px;
}

.spinner-icon {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 50px;
  pointer-events: none;
}

.spinner-icon--parsing {
  color: #3498db;
}

.spinner-icon--success {
  color: #10b981;
}

.spinner-icon--error {
  color: #ef4444;
}

.file-upload-spinner {
  --kinetic-ring-spinner--beam--color-1: #3498db;
  --kinetic-ring-spinner--beam--color-2: #3487bf;
}

.file-upload-spinner--success {
  --kinetic-ring-spinner--beam--color-freeze: #10b981;
}

.file-upload-spinner--error {
  --kinetic-ring-spinner--beam--color-freeze: #ef4444;
}

.info-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.count-text {
  font-size: 1rem;
  color: #555;
}

.duplicate-text {
  font-size: 0.875rem;
  color: #f59e0b;
}

.modal-control-buttons {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  gap: 10px;
}

</style>
