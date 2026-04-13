<template>
  <div class="import-export">
    <Transition :name="transitionName">
      <div v-if="panel === 'main'" key="main" class="buttons-panel">
        <AwesomeButton
          icon="fa-solid fa-file"
          class="ie-btn ie-btn--file ie-btn--bounce"
          :scale-factor="1.2"
          tooltip="Import / Export"
          tooltip-position="top-right"
          :on-click="goToIo"
        />
        <div class="ie-text">
          Import / Export flashcards
        </div>
      </div>
      <div v-else-if="panel === 'io'" key="io" class="buttons-panel">
        <AwesomeButton
          icon="fa-solid fa-circle-arrow-left"
          class="ie-btn ie-btn--back"
          :scale-factor="1.2"
          tooltip="Back"
          tooltip-position="top-right"
          :on-click="goBack"
        />
        <AwesomeButton
          icon="fa-solid fa-file-arrow-down"
          class="ie-btn ie-btn--download"
          :scale-factor="1.2"
          tooltip="Download"
          tooltip-position="top"
          :on-click="goToDownload"
        />
        <AwesomeButton
          icon="fa-solid fa-file-arrow-up"
          class="ie-btn ie-btn--upload"
          :scale-factor="1.2"
          tooltip="Upload"
          tooltip-position="top"
          :on-click="triggerUpload"
        />
      </div>
      <div v-else key="download" class="buttons-panel">
        <AwesomeButton
          icon="fa-solid fa-circle-arrow-left"
          class="ie-btn ie-btn--back"
          :scale-factor="1.2"
          tooltip="Back"
          tooltip-position="top-right"
          :on-click="goBack"
        />
        <AwesomeButton
          icon="fa-solid fa-file-csv"
          class="ie-btn ie-btn--csv"
          :scale-factor="1.2"
          tooltip="CSV"
          tooltip-position="top"
          :on-click="downloadCsv"
        />
        <AwesomeButton
          icon="fa-solid fa-file-excel"
          class="ie-btn ie-btn--excel"
          :scale-factor="1.2"
          tooltip="XLSX"
          tooltip-position="top"
          :on-click="downloadXlsx"
        />
      </div>
    </Transition>
    <input
      ref="fileInput"
      type="file"
      accept=".csv,.xlsx"
      style="display: none"
      @change="onFileSelected"
    />
  </div>
</template>

<script setup lang="ts">
import AwesomeButton from '@/components/AwesomeButton.vue'
import { ref } from 'vue'
import { useFlashcardStore } from '@/stores/flashcard-store.ts'
import { storeToRefs } from 'pinia'
import {
  downloadFlashcardsAsCsv,
  downloadFlashcardsAsXlsx,
} from '@/core-logic/flashcard-file-logic.ts'
import { useSpaceToaster } from '@/stores/toast-store.ts'
import { userApiErrors } from '@/api/user-api-error.ts'
import { useEventStore } from '@/stores/event-store.ts'
import { useToggleStore } from '@/stores/toggle-store.ts'
import { Log, LogTag } from '@/utils/logger.ts'

type Panel = 'main' | 'io' | 'download'

const toggleStore = useToggleStore()
const eventStore = useEventStore()
const flashcardStore = useFlashcardStore()
const toaster = useSpaceToaster()
const { flashcardSet } = storeToRefs(flashcardStore)

const panel = ref<Panel>('main')
const transitionName = ref('slide-forward')
const fileInput = ref<HTMLInputElement>()

function goToIo() {
  transitionName.value = 'slide-forward'
  panel.value = 'io'
}

function goToDownload() {
  transitionName.value = 'slide-forward'
  panel.value = 'download'
}

function goBack() {
  transitionName.value = 'slide-back'
  panel.value = panel.value === 'download' ? 'io' : 'main'
}

function downloadCsv() {
  try {
    downloadFlashcardsAsCsv(flashcardStore.flashcards, flashcardSet.value?.name ?? 'flashcards')
  } catch (error) {
    Log.error(LogTag.LOGIC, 'Failed to download flashcards as CSV', error)
    toaster.bakeError(userApiErrors.FLASHCARD__DOWNLOAD_FAILED)
  }
}

function downloadXlsx() {
  try {
    downloadFlashcardsAsXlsx(flashcardStore.flashcards, flashcardSet.value?.name ?? 'flashcards')
  } catch (error) {
    Log.error(LogTag.LOGIC, 'Failed to download flashcards as XLSX', error)
    toaster.bakeError(userApiErrors.FLASHCARD__DOWNLOAD_FAILED)
  }
}

function triggerUpload() {
  fileInput.value?.click()
}

function onFileSelected(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]

  try {
    if (!file) return

    if (!file.name.endsWith('.xlsx') && !file.name.endsWith('csv')) {
      Log.error(LogTag.LOGIC, `File extension is not supported ${file.name}`)
      toaster.bakeError(userApiErrors.FLASHCARD__FILE_PARSE_FAILED)
      return
    }

    toggleStore.toggleFlashcardSetSettings()
    toggleStore.toggleFileUpload()
    eventStore.emit('flashcard-file:selected', file)
  } finally {
    input.value = ''
  }
}

</script>

<style scoped>
.import-export {
  position: relative;
  width: fit-content;
  height: 52px;
}

.buttons-panel {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 4px;
}

.ie-text {
  font-size: clamp(0.9rem, 2vh, 1rem);
  color: #45454a;
  line-height: 1.5;
  display: inline-block;
  vertical-align: middle;
  text-wrap: nowrap;
}

.ie-btn {
  --awesome-button--icon--size: 36px;
  --awesome-button--bg--hover: #eaeaea;
  --awesome-button--border-radius: 10px;
  --awesome-button--padding: 8px;
}

.ie-btn--download {
  --awesome-button--icon--color: #069494;
  --awesome-button--icon--color--hover: #069494;
}

.ie-btn--upload {
  --awesome-button--icon--color: #f08000;
  --awesome-button--icon--color--hover: #f08000;
}

.ie-btn--file {
  --awesome-button--icon--color: #007bff;
  --awesome-button--icon--color--hover: #007bff;
}

.ie-btn--csv {
  --awesome-button--icon--color: #5a5a5a;
  --awesome-button--icon--color--hover: #5a5a5a;
}

.ie-btn--excel {
  --awesome-button--icon--color: #1D6F42;
  --awesome-button--icon--color--hover: #1D6F42;

}

.ie-btn--back {
  --awesome-button--icon--color: #a3a3a3;
  --awesome-button--icon--color--hover: #a3a3a3;
  --awesome-button--icon--size: 24px;
  --awesome-button--border-radius: 50%;
}

.ie-btn--bounce {
  animation: bounce-hint 3s ease-in-out infinite;
}

@keyframes bounce-hint {
  0%, 100% {
    transform: translateY(0);
  }
  6% {
    transform: translateY(-7px);
  }
  12% {
    transform: translateY(0);
  }
  18% {
    transform: translateY(-4px);
  }
  24% {
    transform: translateY(0);
  }
}

.slide-forward-enter-active {
  transition: transform 0.26s ease, opacity 0.26s ease;
  z-index: 1;
}

.slide-forward-leave-active {
  transition: transform 0.26s ease, opacity 0.26s ease;
}

.slide-forward-enter-from {
  transform: translateX(30px);
  opacity: 0;
}

.slide-forward-leave-to {
  transform: translateX(-30px);
  opacity: 0;
}

.slide-back-enter-active {
  transition: transform 0.26s ease, opacity 0.26s ease;
  z-index: 1;
}

.slide-back-leave-active {
  transition: transform 0.26s ease, opacity 0.26s ease;
}

.slide-back-enter-from {
  transform: translateX(-30px);
  opacity: 0;
}

.slide-back-leave-to {
  transform: translateX(30px);
  opacity: 0;
}
</style>
