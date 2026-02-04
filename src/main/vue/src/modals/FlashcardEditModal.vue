<template>
  <Modal
    title="Edit Flashcard"
    :visible="toggleStore.flashcardEditOpen"
    :focus-on="frontSideTextArea"
    :exit-button="cancelButton"
    :enter-button="updateButton"
    :delete-button="removeButton"
  >
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
        <ErrorText
          :when="frontSideMaxLengthInvalid"
          text="Your text has its own gravity! Maximum 512 characters."
        />
        <VoiceRecorder
          v-model="frontSideAudio"
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
        <ErrorText
          :when="backSideMaxLengthInvalid"
          text="Your text has its own gravity! Maximum 512 characters."
        />
        <VoiceRecorder
          v-model="backSideAudio"
        />
      </div>
    </div>
    <div class="modal-control-buttons">
      <SmartButton
        ref="cancelButton"
        class="off-button"
        text="Cancel"
        :on-click="cancel"
        auto-blur
      />
      <SmartButton
        ref="removeButton"
        class="dangerous-button"
        text="Remove"
        :hold-time="1.2"
        :on-click="remove"
        auto-blur
      />
      <SmartButton
        ref="updateButton"
        class="calm-button"
        text="Save"
        :on-click="update"
        :disabled="!stateChanged || formInvalid"
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
import SpaceToast from '@/components/SpaceToast.vue'
import VoiceRecorder from '@/components/VoiceRecorder.vue'
import ErrorText from '@/components/ErrorText.vue'
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { useVuelidate } from '@vuelidate/core'
import { maxLength, required } from '@vuelidate/validators'
import { useFlashcardStore } from '@/stores/flashcard-store.ts'
import { useToggleStore } from '@/stores/toggle-store.ts'
import { useFlashcardSetStore } from '@/stores/flashcard-set-store.ts'
import { useSpaceToaster } from '@/stores/toast-store.ts'
import { useAudioStore } from '@/stores/audio-store.ts'
import { type Flashcard } from '@/model/flashcard.ts'
import {
  flashcardSides,
  fetchFlashcardAudioBlob,
  removeFlashcardAudioBlob,
  changeFlashcardSides,
  uploadFlashcardAudioBlob, copyFlashcard,
} from '@/core-logic/flashcard-logic.ts'
import { storeToRefs } from 'pinia'
import {
  sendFlashcardRemovalRequest,
  sendFlashcardUpdateRequest,
} from '@/api/api-client.ts'
import { Log, LogTag } from '@/utils/logger.ts'
import { userApiErrors } from '@/api/user-api-error.ts'

const flashcard = defineModel<Flashcard | undefined>('flashcard', { default: undefined })
const removed = defineModel<boolean>('removed', { default: false })
const audioChanged = defineModel<boolean>('audioChanged', { default: false })

const toggleStore = useToggleStore()
const flashcardSetStore = useFlashcardSetStore()
const flashcardStore = useFlashcardStore()
const audioStore = useAudioStore()
const toaster = useSpaceToaster()

const { flashcardSet } = storeToRefs(flashcardStore)

const frontSide = ref(flashcard.value?.frontSide ?? '')
const frontSideTextArea = ref<HTMLElement>()
const frontSideAudioId = ref(audioStore.getAudioId(flashcard.value?.id, flashcardSides.FRONT))
const frontSideAudio = ref<Blob | undefined>()
const frontSideAudioSize = ref<number | undefined>()
const backSide = ref(flashcard.value?.backSide ?? '')
const backSideAudioId = ref(audioStore.getAudioId(flashcard.value?.id, flashcardSides.BACK))
const backSideAudio = ref<Blob | undefined>()
const backSideAudioSize = ref<number | undefined>()
const cancelButton = ref<InstanceType<typeof SmartButton>>()
const removeButton = ref<InstanceType<typeof SmartButton>>()
const updateButton = ref<InstanceType<typeof SmartButton>>()

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
    || isFrontSideAudioChanged.value || isBackSideAudioChanged.value
})

const isFrontSideAudioChanged = computed(() => {
  if (!frontSideAudioId.value) {
    return frontSideAudio.value !== undefined && frontSideAudio.value?.size > 0
  } else {
    return frontSideAudioSize.value !== frontSideAudio.value?.size
  }
})

const isBackSideAudioChanged = computed(() => {
  if (!backSideAudioId.value) {
    return backSideAudio.value !== undefined && backSideAudio.value?.size > 0
  } else {
    return backSideAudioSize.value !== backSideAudio.value?.size
  }
})

async function fetchAudio() {
  const set = flashcardSet.value
  const card = flashcard.value

  if (!set || !card) return

  await Promise.all([
    (async function () {
      if (frontSideAudioId.value) {
        return await fetchFlashcardAudioBlob(set.id, card.id, true)
          .then((blob) => {
            frontSideAudio.value = blob
            frontSideAudioSize.value = blob?.size
          })
      } else {
        frontSideAudio.value = undefined
        frontSideAudioSize.value = undefined
      }
    })(),
    (async function () {
      if (backSideAudioId.value) {
        return await fetchFlashcardAudioBlob(set.id, card.id, false)
          .then((blob) => {
            backSideAudio.value = blob
            backSideAudioSize.value = blob?.size
          })
      } else {
        backSideAudio.value = undefined
        frontSideAudioSize.value = undefined
      }
    })(),
  ])
}

async function uploadAudioIfRelevant(): Promise<boolean> {
  const set = flashcardSet.value
  const card = flashcard.value

  if (!set || !card) {
    Log.error(LogTag.LOGIC, `Cannot upload audio: both FlashcardSet.id=${set?.id} and Flashcard.id=${card?.id} must be defined`)
    return true
  }

  return await Promise.all([
    (async function () {
      if (isFrontSideAudioChanged.value && frontSideAudio.value) {
        return uploadFlashcardAudioBlob(set, card, frontSideAudio.value, true)
          .then((success) => {
            if (success) {
              audioChanged.value = true
            }
            return success
          })
      } else {
        return true
      }
    })(),
    (async function () {
      if (isBackSideAudioChanged.value && backSideAudio.value) {
        return uploadFlashcardAudioBlob(set, card, backSideAudio.value, false)
          .then((success) => {
            if (success) {
              audioChanged.value = true
            }
            return success
          })
      } else {
        return true
      }
    })(),
  ])
    .then((results) => results.every(v => v))
}

async function removeAudioIfRelevant(): Promise<boolean> {
  const set = flashcardSet.value
  const card = flashcard.value

  if (!set || !card) {
    Log.error(LogTag.LOGIC, `Cannot remove audio blobs: both FlashcardSet.id=${set?.id} and Flashcard.id=${card?.id} must be defined`)
    return true
  }

  return await Promise.all([
    (async function () {
      if (frontSideAudioId.value && !frontSideAudio.value && frontSideAudioSize.value) {
        return removeFlashcardAudioBlob(set, card, frontSideAudioId.value, true)
          .then((result) => {
            if (result) {
              frontSideAudioId.value = undefined
              frontSideAudioSize.value = undefined
              audioChanged.value = true
            }
            return result
          })
      } else {
        return true
      }
    })(),
    (async function () {
      if (backSideAudioId.value && !backSideAudio.value && backSideAudioSize.value) {
        return removeFlashcardAudioBlob(set, card, backSideAudioId.value, false)
          .then((result) => {
            if (result) {
              backSideAudioId.value = undefined
              backSideAudioSize.value = undefined
              audioChanged.value = true
            }
            return result
          })
      } else {
        return true
      }
    })(),
  ])
    .then((result) => result.every(v => v))
}

async function cancel() {
  await resetState()
  toggleModalForm()
  removed.value = false
  audioChanged.value = false
}

async function remove() {
  const done = await removeFlashcard()
  if (done) {
    await resetState()
    toggleModalForm()
    removed.value = true
    audioChanged.value = false
  }
}

async function update() {
  if (stateChanged.value) {
    $v.value.$touch()
    if (!formInvalid.value) {
      const updated = await updateFlashcard()
      if (updated) {
        const uploaded = await uploadAudioIfRelevant()
        const removed = await removeAudioIfRelevant()
        if (uploaded && removed) {
          toggleModalForm()
          await resetState()
        }
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
      toaster.bakeError(userApiErrors.FLASHCARD__REMOVING_FAILED, error.response?.data)
      return false
    })
}

async function updateFlashcard(): Promise<boolean> {
  if (!flashcardSet.value || !flashcard.value) return false

  const flashcardCopy = copyFlashcard(flashcard.value)
  const changed = changeFlashcardSides(flashcardCopy, frontSide.value, backSide.value)
  if (!changed) return true // consider it is updated

  return await sendFlashcardUpdateRequest(flashcardSet.value.id, flashcardCopy)
    .then((response) => {
      flashcardStore.changeFlashcard(response.data)
      if (flashcard.value) {
        flashcard.value.frontSide = response.data.frontSide
        flashcard.value.backSide = response.data.backSide
      }
      return true
    })
    .catch((error) => {
      Log.error(LogTag.LOGIC, `Failed to update Flashcard.id=${flashcardCopy.id}`, error.response?.data)
      toaster.bakeError(userApiErrors.FLASHCARD__UPDATING_FAILED, error.response?.data)
      return false
    })
}

function toggleModalForm() {
  toggleStore.toggleFlashcardEdit()
}

async function resetState() {
  Log.log(LogTag.LOGIC, 'Resetting state...')
  frontSide.value = flashcard.value?.frontSide ?? ''
  frontSideAudioId.value = audioStore.getAudioId(flashcard.value?.id, flashcardSides.FRONT)
  backSide.value = flashcard.value?.backSide ?? ''
  backSideAudioId.value = audioStore.getAudioId(flashcard.value?.id, flashcardSides.BACK)
  await fetchAudio()
  $v.value.$reset()
  nextTick().then(() => {
    frontSideTextArea.value?.focus()
  })
}

watch(flashcard, async (newVal) => {
  Log.log(LogTag.DEBUG, 'watch.flashcard', newVal)
  frontSide.value = newVal?.frontSide ?? ''
  frontSideAudioId.value = audioStore.getAudioId(newVal?.id, flashcardSides.FRONT)
  backSide.value = newVal?.backSide ?? ''
  backSideAudioId.value = audioStore.getAudioId(newVal?.id, flashcardSides.BACK)
  await fetchAudio()
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
  await fetchAudio()
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
