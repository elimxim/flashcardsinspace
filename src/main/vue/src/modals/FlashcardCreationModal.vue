<template>
  <Modal
    title="New Flashcard"
    :visible="toggleStore.flashcardCreationOpen"
    :exit-button="cancelButton"
    :enter-button="createButton"
  >
    <template #control>
      <AwesomeButton
        ref="infiniteLoopButtonRef"
        icon="fa-regular fa-circle"
        flip-icon="fa-solid fa-arrows-spin"
        tooltip="Infinite loop"
        tooltip-position="bottom-left"
        spin-when-flipped
      />
    </template>

    <FlashcardInput
      ref="flashcardSidesRef"
      v-model:front-text="frontSide"
      v-model:front-audio="frontSideAudioBlob"
      v-model:front-picture="frontSidePictureBlob"
      v-model:back-text="backSide"
      v-model:back-audio="backSideAudioBlob"
      v-model:back-picture="backSidePictureBlob"
    />

    <div class="modal-control-buttons">
      <SmartButton
        ref="cancelButton"
        class="off-button"
        text="Cancel"
        :on-click="cancel"
        auto-blur
      />
      <SmartButton
        ref="createButton"
        class="safe-button"
        text="Create"
        :on-click="create"
        :disabled="flashcardSidesRef?.invalid"
        auto-blur
      />
    </div>
  </Modal>
</template>

<script setup lang="ts">
import SmartButton from '@/components/SmartButton.vue'
import AwesomeButton from '@/components/AwesomeButton.vue'
import FlashcardInput from '@/components/FlashcardInput.vue'
import Modal from '@/components/Modal.vue'
import { ref } from 'vue'
import { useFlashcardStore } from '@/stores/flashcard-store.ts'
import { useToggleStore } from '@/stores/toggle-store.ts'
import { useChronoStore } from '@/stores/chrono-store.ts'
import { useFlashcardSetStore } from '@/stores/flashcard-set-store.ts'
import { useSpaceToaster } from '@/stores/toast-store.ts'
import { useAudioStore } from '@/stores/audio-store.ts'
import { usePictureStore } from '@/stores/picture-store.ts'
import { flashcardSides, newFlashcard } from '@/core-logic/flashcard-logic.ts'
import { uploadFlashcardAudioBlob } from '@/core-logic/flashcard-audio-logic.ts'
import { uploadFlashcardPictureBlob } from '@/core-logic/flashcard-picture-logic.ts'
import { storeToRefs } from 'pinia'
import {
  sendFlashcardAudioMetadataGetRequest,
  sendFlashcardCreationRequest,
  sendFlashcardPictureMetadataRequest,
} from '@/api/api-client.ts'
import { Flashcard } from '@/model/flashcard.ts'
import { Log, LogTag } from '@/utils/logger.ts'
import { userApiErrors } from '@/api/user-api-error.ts'

const toggleStore = useToggleStore()
const chronoStore = useChronoStore()
const flashcardSetStore = useFlashcardSetStore()
const flashcardStore = useFlashcardStore()
const audioStore = useAudioStore()
const pictureStore = usePictureStore()
const toaster = useSpaceToaster()

const { flashcardSet } = storeToRefs(flashcardStore)
const { currDay } = storeToRefs(chronoStore)

const frontSide = ref<string>()
const frontSideAudioBlob = ref<Blob | undefined>()
const frontSidePictureBlob = ref<Blob | undefined>()
const backSide = ref<string>()
const backSideAudioBlob = ref<Blob | undefined>()
const backSidePictureBlob = ref<Blob | undefined>()
const flashcardSidesRef = ref<InstanceType<typeof FlashcardInput>>()
const infiniteLoopButtonRef = ref<InstanceType<typeof AwesomeButton>>()
const cancelButton = ref<InstanceType<typeof SmartButton>>()
const createButton = ref<InstanceType<typeof SmartButton>>()
const createdFlashcard = ref<Flashcard | undefined>()

async function uploadAudioIfRelevant(): Promise<boolean> {
  const set = flashcardSet.value
  const card = createdFlashcard.value

  if (!set || !card) {
    Log.log(LogTag.LOGIC, `Cannot upload audio: both FlashcardSet.id=${set?.id} and Flashcard.id=${card?.id} must be defined`)
    return true
  }

  return await Promise.all([
    (async function () {
      if (frontSideAudioBlob.value) {
        return uploadFlashcardAudioBlob(set, card, frontSideAudioBlob.value, flashcardSides.FRONT)
      } else {
        return true
      }
    })(),
    (async function () {
      if (backSideAudioBlob.value) {
        return uploadFlashcardAudioBlob(set, card, backSideAudioBlob.value, flashcardSides.BACK)
      } else {
        return true
      }
    })(),
  ])
    .then((results) => results.every(v => v))
}

async function uploadPictureIfRelevant(): Promise<boolean> {
  const set = flashcardSet.value
  const card = createdFlashcard.value

  if (!set || !card) {
    Log.log(LogTag.LOGIC, `Cannot upload picture: both FlashcardSet.id=${set?.id} and Flashcard.id=${card?.id} must be defined`)
    return true
  }

  return await Promise.all([
    (async function () {
      if (frontSidePictureBlob.value) {
        return uploadFlashcardPictureBlob(set, card, frontSidePictureBlob.value, flashcardSides.FRONT)
      } else {
        return true
      }
    })(),
    (async function () {
      if (backSidePictureBlob.value) {
        return uploadFlashcardPictureBlob(set, card, backSidePictureBlob.value, flashcardSides.BACK)
      } else {
        return true
      }
    })(),
  ])
    .then((results) => results.every(v => v))
}

async function cancel() {
  await resetState()
  toggleModalForm()
}

async function create() {
  flashcardSidesRef.value?.validate()
  if (flashcardSidesRef.value?.invalid) return
  const added = await addNewFlashcard()
  if (added) {
    const uploaded = await Promise.all([uploadAudioIfRelevant(), uploadPictureIfRelevant()])
    if (uploaded.every(Boolean)) {
      if (!infiniteLoopButtonRef.value?.isPressed()) {
        toggleModalForm()
      }
      await resetState()
    }
  }
}

async function addNewFlashcard(): Promise<boolean> {
  if (!flashcardSet.value) return false
  const setId = flashcardSet.value.id
  const flashcard = newFlashcard(frontSide.value, backSide.value, currDay.value.chronodate)
  return await sendFlashcardCreationRequest(setId, flashcard)
    .then(async (response) => {
      if (response.data.initialized) {
        if (!response.data.flashcardSet
          || !response.data.flashcard
          || !response.data.chronodays
          || !response.data.currDay
        ) {
          Log.log(LogTag.LOGIC, `Response doesn't have required fields: ${JSON.stringify(response.data)}`)
          toaster.bakeError(userApiErrors.FLASHCARD__CREATION_FAILED)
          return false
        }

        createdFlashcard.value = response.data.flashcard
        flashcardSetStore.updateSet(response.data.flashcardSet)
        flashcardStore.changeSet(response.data.flashcardSet)
        flashcardStore.addNewFlashcard(response.data.flashcard)
        chronoStore.loadState(
          response.data.chronodays,
          response.data.currDay,
          {
            streak: 0,
            lastDate: currDay.value.chronodate
          }
        )
        flashcardSetStore.addExtra(setId)
        flashcardSetStore.incrementFlashcardsNumber(setId)

        return await Promise.all([
          sendFlashcardAudioMetadataGetRequest(setId),
          sendFlashcardPictureMetadataRequest(setId),
        ])
          .then(([audioResponse, pictureResponse]) => {
            audioStore.loadState(audioResponse.data)
            pictureStore.loadState(pictureResponse.data)
            return true
          })
          .catch((error) => {
            Log.error(LogTag.LOGIC, `Failed to init FlashcardSet.id=${setId}`, error.response?.data)
            toaster.bakeError(userApiErrors.FLASHCARD__CREATION_FAILED, error.response?.data)
            return false
          })
      } else {
        if (!response.data.flashcard) {
          Log.log(LogTag.LOGIC, `Response doesn't have flashcard: ${JSON.stringify(response.data)}`)
          toaster.bakeError(userApiErrors.FLASHCARD__CREATION_FAILED)
          return false
        }

        createdFlashcard.value = response.data.flashcard
        flashcardStore.addNewFlashcard(response.data.flashcard)
        flashcardSetStore.incrementFlashcardsNumber(setId)
        return true
      }
    })
    .catch((error) => {
      Log.error(LogTag.LOGIC, `Failed to add a flashcard to FlashcardSet.id=${setId}`, error.response?.data)
      toaster.bakeError(userApiErrors.FLASHCARD__CREATION_FAILED, error.response?.data)
      return false
    })
}

function toggleModalForm() {
  toggleStore.toggleFlashcardCreation()
}

async function resetState() {
  createdFlashcard.value = undefined
  frontSideAudioBlob.value = undefined
  frontSidePictureBlob.value = undefined
  backSideAudioBlob.value = undefined
  backSidePictureBlob.value = undefined
  flashcardSidesRef.value?.resetState()
}

</script>

<style scoped>
.modal-control-buttons {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  gap: 10px;
}
</style>
