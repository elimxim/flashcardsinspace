<template>
  <Modal
    title="New Flashcard"
    :visible="toggleStore.flashcardCreationOpen"
    :on-press-exit="cancel"
    :on-press-enter="create"
    :focus-on="frontSideTextArea"
  >
    <template #control>
      <AwesomeButton
        ref="infiniteLoopButton"
        icon="fa-regular fa-circle"
        spin-icon="fa-solid fa-arrows-spin"
        spinnable
      />
    </template>

    <div class="modal-main-area">
      <div class="modal-main-area--inner">
        <SmartInput
          ref="frontSideTextArea"
          v-model="frontSide"
          type="text"
          :invalid="frontSideInvalid"
          placeholder="Front side"
          area
        />
        <span v-if="frontSideMaxLengthInvalid" class="text-error">
          Your text has its own gravity! Maximum 512 characters.
        </span>
        <VoiceRecorder
          v-model="frontSideAudioBlob"
        />
      </div>
      <div class="modal-main-area--inner">
        <SmartInput
          v-model="backSide"
          type="text"
          :invalid="backSideInvalid"
          placeholder="Back side"
          area
        />
        <span v-if="backSideMaxLengthInvalid" class="text-error">
          Your text has its own gravity! Maximum 512 characters.
        </span>
        <VoiceRecorder
          v-model="backSideAudioBlob"
        />
      </div>
    </div>
    <div class="modal-control-buttons">
      <SmartButton
        class="cancel-button"
        text="Cancel"
        :on-click="cancel"
        auto-blur
      />
      <SmartButton
        class="create-button"
        text="Create"
        :on-click="create"
        :disabled="formInvalid"
        auto-blur
      />
    </div>
  </Modal>
  <SpaceToast/>
</template>

<script setup lang="ts">
import Modal from '@/components/Modal.vue'
import SmartButton from '@/components/SmartButton.vue'
import SmartInput from '@/components/SmartInput.vue'
import AwesomeButton from '@/components/AwesomeButton.vue'
import SpaceToast from '@/components/SpaceToast.vue'
import VoiceRecorder from '@/components/VoiceRecorder.vue'
import { computed, nextTick, ref, watch } from 'vue'
import { useVuelidate } from '@vuelidate/core'
import { maxLength, required } from '@vuelidate/validators'
import { useFlashcardStore } from '@/stores/flashcard-store.ts'
import { useToggleStore } from '@/stores/toggle-store.ts'
import { useChronoStore } from '@/stores/chrono-store.ts'
import { useFlashcardSetStore } from '@/stores/flashcard-set-store.ts'
import { useSpaceToaster } from '@/stores/toast-store.ts'
import { useAudioStore } from '@/stores/audio-store.ts'
import {
  newFlashcard,
  uploadFlashcardAudioBlob,
} from '@/core-logic/flashcard-logic.ts'
import { storeToRefs } from 'pinia'
import {
  sendFlashcardAudioMetadataGetRequest,
  sendFlashcardCreationRequest,
  sendFlashcardSetInitRequest,
} from '@/api/api-client.ts'
import { Flashcard } from '@/model/flashcard.ts'

const toggleStore = useToggleStore()
const chronoStore = useChronoStore()
const flashcardSetStore = useFlashcardSetStore()
const flashcardStore = useFlashcardStore()
const audioStore = useAudioStore()
const toaster = useSpaceToaster()

const { flashcardSet, isStarted } = storeToRefs(flashcardStore)

const frontSide = ref<string>('')
const frontSideTextArea = ref<HTMLElement>()
const frontSideAudioBlob = ref<Blob | undefined>()
const backSide = ref<string>('')
const backSideAudioBlob = ref<Blob | undefined>()
const infiniteLoopButton = ref<InstanceType<typeof AwesomeButton>>()

const createdFlashcard = ref<Flashcard | undefined>()

const validationRules = {
  frontSide: { required, maxLength: maxLength(512) },
  backSide: { required, maxLength: maxLength(512) },
}

const $v = useVuelidate(validationRules, {
  frontSide: frontSide,
  backSide: backSide,
})

const formInvalid = computed(() => $v.value.$errors.length > 0)
const frontSideInvalid = computed(() => $v.value.frontSide.$errors.length > 0)
const frontSideMaxLengthInvalid = computed(() =>
  $v.value.frontSide.$errors.find(v => v.$validator === 'maxLength') !== undefined
)
const backSideInvalid = computed(() => $v.value.backSide.$errors.length > 0)
const backSideMaxLengthInvalid = computed(() =>
  $v.value.backSide.$errors.find(v => v.$validator === 'maxLength') !== undefined
)

async function uploadAudioIfRelevant(): Promise<boolean> {
  const set = flashcardSet.value
  const card = createdFlashcard.value

  if (!set || !card) {
    console.error('Cannot upload audio: flashcard set or flashcard is undefined')
    return true
  }

  return await Promise.all([
    (async function () {
      if (frontSideAudioBlob.value) {
        return uploadFlashcardAudioBlob(set, card, frontSideAudioBlob.value, true)
      } else {
        return true
      }
    })(),
    (async function () {
      if (backSideAudioBlob.value) {
        return uploadFlashcardAudioBlob(set, card, backSideAudioBlob.value, false)
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
  $v.value.$touch()
  if (!formInvalid.value) {
    const added = await addNewFlashcard()
    if (added) {
      const uploaded = await uploadAudioIfRelevant()
      if (uploaded) {
        if (!infiniteLoopButton.value?.isPressed()) {
          toggleModalForm()
        }
        await resetState()
      }
    }
  }
}

async function addNewFlashcard(): Promise<boolean> {
  if (!flashcardSet.value) return false
  const setId = flashcardSet.value.id
  const flashcard = newFlashcard(frontSide.value, backSide.value)
  if (isStarted.value) {
    return await sendFlashcardCreationRequest(setId, flashcard)
      .then((response) => {
        createdFlashcard.value = response.data
        flashcardStore.addNewFlashcard(response.data)
        flashcardSetStore.incrementFlashcardsNumber(setId)
        return true
      })
      .catch((error) => {
        console.error(`Failed to add a flashcard to set ${setId}`, error.response?.data)
        toaster.bakeError(`Couldn't add a flashcard`, error.response?.data)
        return false
      })
  } else {
    return await sendFlashcardSetInitRequest(setId, flashcard)
      .then((response) => {
        createdFlashcard.value = response.data.flashcard
        flashcardSetStore.updateSet(response.data.flashcardSet)
        flashcardStore.changeSet(response.data.flashcardSet)
        flashcardStore.addNewFlashcard(response.data.flashcard)
        chronoStore.loadState(
          response.data.chronodays,
          response.data.currDay,
          0,
        )
        return sendFlashcardAudioMetadataGetRequest(setId)
      })
      .then((response) => {
        audioStore.loadState(response.data)
        return true
      })
      .catch((error) => {
        console.error(`Failed to init flashcard set ${setId}`, error.response?.data)
        toaster.bakeError(`Couldn't add a flashcard`, error.response?.data)
        return false
      })
  }
}

function toggleModalForm() {
  toggleStore.toggleFlashcardCreation()
}

async function resetState() {
  createdFlashcard.value = undefined
  frontSide.value = ''
  frontSideAudioBlob.value = undefined
  backSide.value = ''
  backSideAudioBlob.value = undefined
  $v.value.$reset()
  nextTick().then(() => {
    frontSideTextArea.value?.focus()
  })
}

watch(frontSide, () => {
  if (frontSideInvalid.value) {
    $v.value.frontSide.$reset()
  }
})

watch(backSide, () => {
  if (backSideInvalid.value) {
    $v.value.backSide.$reset()
  }
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
</style>
