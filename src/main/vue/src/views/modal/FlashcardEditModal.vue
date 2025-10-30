<template>
  <Modal
    title="Edit Flashcard"
    :visible="toggleStore.flashcardEditOpen"
    :on-press-exit="cancel"
    :on-press-enter="update"
    :on-press-delete="remove"
    :focus-on="frontSideTextArea"
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
        class="remove-button"
        text="Remove"
        :hold-time="1.2"
        :on-click="remove"
        auto-blur
      />
      <SmartButton
        class="update-button"
        text="Update"
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
  updateFlashcardSides,
  uploadFlashcardAudioBlob,
} from '@/core-logic/flashcard-logic.ts'
import { storeToRefs } from 'pinia'
import {
  sendFlashcardRemovalRequest,
  sendFlashcardUpdateRequest,
} from '@/api/api-client.ts'

const flashcard = defineModel<Flashcard | undefined>('flashcard', { default: undefined })
const removed = defineModel<boolean>('removed', { default: false })

const toggleStore = useToggleStore()
const flashcardSetStore = useFlashcardSetStore()
const flashcardStore = useFlashcardStore()
const audioStore = useAudioStore()
const toaster = useSpaceToaster()

const { flashcardSet } = storeToRefs(flashcardStore)

const frontSide = ref(flashcard.value?.frontSide ?? '')
const frontSideTextArea = ref<HTMLElement>()
const frontSideAudioId = ref(audioStore.getAudioId(flashcard.value?.id, flashcardSides.FRONT))
const flashcardFrontSideAudioBlob = ref<Blob | undefined>()
const frontSideAudioBlob = ref<Blob | undefined>()
const backSide = ref(flashcard.value?.backSide ?? '')
const backSideAudioId = ref(audioStore.getAudioId(flashcard.value?.id, flashcardSides.BACK))
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

async function fetchAudio() {
  const set = flashcardSet.value
  const card = flashcard.value

  if (!set || !card) return

  await Promise.all([
    (async function () {
      if (frontSideAudioId.value) {
        return await fetchFlashcardAudioBlob(set, card, true)
          .then((blob) => {
            flashcardFrontSideAudioBlob.value = blob
            frontSideAudioBlob.value = blob
          })
      }
    })(),
    (async function () {
      if (backSideAudioId.value) {
        return await fetchFlashcardAudioBlob(set, card, false)
          .then((blob) => {
            flashcardBackSideAudioBlob.value = blob
            backSideAudioBlob.value = blob
          })
      }
    })(),
  ])
}

async function uploadAudioIfRelevant(): Promise<boolean> {
  const set = flashcardSet.value
  const card = flashcard.value

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

async function removeAudioIfRelevant(): Promise<boolean> {
  const set = flashcardSet.value
  const card = flashcard.value

  if (!set || !card) {
    console.error('Cannot remove audio blobs: flashcard set or flashcard is undefined')
    return true
  }

  return await Promise.all([
    (async function () {
      if (frontSideAudioId.value && !frontSideAudioBlob.value && flashcardFrontSideAudioBlob.value) {
        return removeFlashcardAudioBlob(set, card, frontSideAudioId.value, true)
          .then((result) => {
            if (result) {
              frontSideAudioId.value = undefined
              flashcardFrontSideAudioBlob.value = undefined
            }
            return result
          })
      } else {
        return true
      }
    })(),
    (async function () {
      if (backSideAudioId.value && !backSideAudioBlob.value && flashcardBackSideAudioBlob.value) {
        return removeFlashcardAudioBlob(set, card, backSideAudioId.value, false)
          .then((result) => {
            if (result) {
              backSideAudioId.value = undefined
              flashcardBackSideAudioBlob.value = undefined
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
}

async function remove() {
  const done = await removeFlashcard()
  if (done) {
    await resetState()
    toggleModalForm()
    removed.value = true
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
      toaster.bakeError('System error', error.response?.data)
      return false
    })
}

async function updateFlashcard(): Promise<boolean> {
  if (!flashcardSet.value || !flashcard.value) return false
  const updatedFlashcard = updateFlashcardSides(flashcard.value, frontSide.value, backSide.value)
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
  toggleStore.toggleFlashcardEdit()
}

async function resetState() {
  console.log('Resetting state')
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
  console.log('Watch.flashcard', newVal)
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
