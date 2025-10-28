<template>
  <Modal
    :visible="editMode ? toggleStore.flashcardEditOpen : toggleStore.flashcardCreationOpen"
    :on-press-exit="cancel"
    :on-press-enter="props.editMode ? update : create"
    :on-press-delete="remove"
    :focus-on="frontSideTextArea"
    :title="editMode ? 'Edit Flashcard' : 'New Flashcard'"
  >
    <template v-if="!editMode" #control>
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
        v-if="editMode"
        class="remove-button"
        text="Remove"
        :hold-time="1.2"
        :on-click="remove"
        auto-blur
      />
      <SmartButton
        v-if="editMode"
        class="update-button"
        text="Update"
        :on-click="update"
        :disabled="!stateChanged || formInvalid"
        auto-blur
      />
      <SmartButton
        v-if="!editMode"
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
import {
  computed,
  defineProps,
  nextTick, onMounted,
  ref,
  watch
} from 'vue'
import { useVuelidate } from '@vuelidate/core'
import { maxLength, required } from '@vuelidate/validators'
import { useFlashcardStore } from '@/stores/flashcard-store.ts'
import { useToggleStore } from '@/stores/toggle-store.ts'
import { useChronoStore } from '@/stores/chrono-store.ts'
import { useFlashcardSetStore } from '@/stores/flashcard-set-store.ts'
import { useSpaceToaster } from '@/stores/toast-store.ts'
import { type Flashcard } from '@/model/flashcard.ts'
import {
  fetchFlashcardAudioBlob,
  newFlashcard,
  updateFlashcardSides,
} from '@/core-logic/flashcard-logic.ts'
import { storeToRefs } from 'pinia'
import {
  sendFlashcardRemovalRequest,
  sendFlashcardCreationRequest,
  sendFlashcardSetInitRequest,
  sendFlashcardUpdateRequest,
  sendFlashcardAudioUploadRequest,
} from '@/api/api-client.ts'

const flashcard = defineModel<Flashcard | undefined>('flashcard', { default: undefined })
const removed = defineModel<boolean>('removed', { default: false })

const props = withDefaults(defineProps<{
  editMode?: boolean,
}>(), {
  editMode: false,
})

const toggleStore = useToggleStore()
const chronoStore = useChronoStore()
const flashcardSetStore = useFlashcardSetStore()
const flashcardStore = useFlashcardStore()
const toaster = useSpaceToaster()

const { flashcardSet, isStarted } = storeToRefs(flashcardStore)

const frontSide = ref(flashcard.value?.frontSide ?? '')
const frontSideTextArea = ref<HTMLElement>()
const frontSideAudioId = ref(flashcard.value?.frontSideAudioId)
const flashcardFrontSideAudioBlob = ref<Blob | undefined>()
const frontSideAudioBlob = ref<Blob | undefined>()
const backSide = ref(flashcard.value?.backSide ?? '')
const infiniteLoopButton = ref<InstanceType<typeof AwesomeButton>>()
const backSideAudioId = ref(flashcard.value?.backSideAudioId)
const flashcardBackSideAudioBlob = ref<Blob | undefined>()
const backSideAudioBlob = ref<Blob | undefined>()

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

const stateChanged = computed(() => {
  return flashcard.value?.frontSide !== frontSide.value
    || flashcard.value?.backSide !== backSide.value
    || isFrontSideAudioChanged.value
    || isBackSideAudioChanged.value
})

const isFrontSideAudioChanged = computed(() => {
  if (!frontSideAudioId.value) {
    return frontSideAudioBlob.value !== undefined && frontSideAudioBlob.value?.size > 0
  } else {
    return flashcardFrontSideAudioBlob.value?.size !== frontSideAudioBlob.value?.size
  }
})

const isBackSideAudioChanged = computed(() => {
  if (!backSideAudioId.value) {
    return backSideAudioBlob.value !== undefined && backSideAudioBlob.value?.size > 0
  } else {
    return flashcardBackSideAudioBlob.value?.size !== backSideAudioBlob.value?.size
  }
})

async function fetchFlashcardAudio() {
  return Promise.all([
    fetchAudioBlob(true)
      .then((blob) => {
        flashcardFrontSideAudioBlob.value = blob
        frontSideAudioBlob.value = blob
      }),
    fetchAudioBlob(false)
      .then((blob) => {
        flashcardBackSideAudioBlob.value = blob
        backSideAudioBlob.value = blob
      }),
  ])
}

async function fetchAudioBlob(isFrontSide: boolean): Promise<Blob | undefined> {
  if (isFrontSide && frontSideAudioId.value) {
    return await fetchFlashcardAudioBlob(flashcardSet.value, flashcard.value, isFrontSide)
  } else if (!isFrontSide && backSideAudioId.value) {
    return await fetchFlashcardAudioBlob(flashcardSet.value, flashcard.value, isFrontSide)
  }
}

async function uploadFlashcardAudio(): Promise<boolean> {
  return Promise.all([
    uploadAudioBlob(frontSideAudioBlob.value, true),
    uploadAudioBlob(backSideAudioBlob.value, false),
  ])
    .then((results) => {
      return results.every(v => v)
    })
}

async function uploadAudioBlob(audioBlob: Blob | undefined, isFrontSide: boolean): Promise<boolean> {
  const flashcardSetId = flashcardSet.value?.id
  const flashcardId = flashcard.value?.id
  const side = isFrontSide ? 'FRONT' : 'BACK'

  if (!audioBlob || !flashcardSetId || !flashcardId) return true

  return await sendFlashcardAudioUploadRequest(flashcardSetId, flashcardId, side, audioBlob)
    .then((response) => {
      console.log(`Audio uploaded ${response.data.id}, size: ${response.data.audioSize}, mime: ${response.data.mimeType}`)
      flashcardStore.setFlashcardAudioId(flashcardId, response.data.id, isFrontSide)
      return true
    })
    .catch((error) => {
      console.error(`Failed to upload audio for flashcard ${flashcardId}`, error)
      toaster.bakeError(`Couldn't upload audio`, error.response?.data)
      return false
    })
}

async function cancel() {
  await resetState()
  toggleModalForm()
  removed.value = false
}

async function remove() {
  const done = await removeFlashcard()
  if (done) {
    await resetState()
    toggleModalForm()
    removed.value = true
  }
}

async function create() {
  $v.value.$touch()
  if (!formInvalid.value) {
    const added = await addNewFlashcard()
    if (added) {
      await uploadFlashcardAudio()
      if (!infiniteLoopButton.value?.isPressed()) {
        toggleModalForm()
      }
      await resetState()
    }
  }
}

async function update() {
  if (stateChanged.value) {
    $v.value.$touch()
    if (!formInvalid.value) {
      const updated = await updateFlashcard()
      if (updated) {
        await uploadFlashcardAudio()
        toggleModalForm()
        await resetState()
      }
    }
  }
}

async function removeFlashcard(): Promise<boolean> {
  if (!flashcardSet.value || !flashcard.value) return false
  const setId = flashcardSet.value.id
  const flashcardId = flashcard.value.id
  return await sendFlashcardRemovalRequest(flashcardSet.value.id, flashcardId)
    .then(() => {
      flashcardStore.removeFlashcard(flashcardId)
      flashcardSetStore.decrementFlashcardsNumber(setId)
      toaster.bakeSuccess('Success', 'Flashcard removed', 200)
      return true
    }).catch((error) => {
      toaster.bakeError('System error', error.response?.data)
      return false
    })
}

async function addNewFlashcard(): Promise<boolean> {
  if (!flashcardSet.value) return false
  const setId = flashcardSet.value.id
  const flashcard = newFlashcard(frontSide.value, backSide.value)
  if (isStarted.value) {
    return await sendFlashcardCreationRequest(setId, flashcard)
      .then((response) => {
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
        flashcardSetStore.updateSet(response.data.flashcardSet)
        flashcardStore.changeSet(response.data.flashcardSet)
        flashcardStore.addNewFlashcard(response.data.flashcard)
        chronoStore.loadState(
          response.data.chronodays,
          response.data.currDay,
          0,
        )
        return true
      })
      .catch((error) => {
        console.error(`Failed to init flashcard set ${setId}`, error.response?.data)
        toaster.bakeError(`Couldn't add a flashcard`, error.response?.data)
        return false
      })
  }
}

async function updateFlashcard(): Promise<boolean> {
  if (!flashcardSet.value || !flashcard.value) return false
  const updatedFlashcard = updateFlashcardSides(
    flashcard.value,
    frontSide.value,
    backSide.value
  )
  return await sendFlashcardUpdateRequest(flashcardSet.value.id, updatedFlashcard)
    .then((response) => {
      flashcardStore.changeFlashcard(response.data)
      return true
    })
    .catch((error) => {
      console.error(`Failed to update flashcard ${updatedFlashcard.id}`, error.response?.data)
      toaster.bakeError(`Couldn't change a flashcard`, error.response?.data)
      return false
    })
}

function toggleModalForm() {
  if (props.editMode) {
    toggleStore.toggleFlashcardEdit()
  } else {
    toggleStore.toggleFlashcardCreation()
  }
}

async function resetState() {
  frontSide.value = flashcard.value?.frontSide ?? ''
  frontSideAudioId.value = flashcard.value?.frontSideAudioId
  backSide.value = flashcard.value?.backSide ?? ''
  backSideAudioId.value = flashcard.value?.backSideAudioId
  await fetchFlashcardAudio()
  $v.value.$reset()
  nextTick().then(() => {
    frontSideTextArea.value?.focus()
  })
}

watch(flashcard, async (newVal) => {
  frontSide.value = newVal?.frontSide ?? ''
  frontSideAudioId.value = newVal?.frontSideAudioId
  backSide.value = newVal?.backSide ?? ''
  backSideAudioId.value = newVal?.backSideAudioId
  await fetchFlashcardAudio()
})

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

onMounted(async () => {
  await fetchFlashcardAudio()
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
