<template>
  <Modal
    title="Edit Flashcard"
    :visible="toggleStore.flashcardEditOpen"
    :exit-button="cancelButton"
    :enter-button="updateButton"
    :delete-button="removeButton"
  >
    <FlashcardInput
      ref="sidesRef"
      v-model:front-text="frontSide"
      v-model:front-audio="frontSideAudio"
      v-model:front-picture="frontSidePicture"
      v-model:back-text="backSide"
      v-model:back-audio="backSideAudio"
      v-model:back-picture="backSidePicture"
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
        :disabled="!stateChanged || sidesRef?.invalid"
        auto-blur
      />
    </div>
  </Modal>
  <SpaceToast/>
</template>

<script setup lang="ts">
import Modal from '@/components/Modal.vue'
import SmartButton from '@/components/SmartButton.vue'
import FlashcardInput from '@/components/FlashcardInput.vue'
import SpaceToast from '@/components/SpaceToast.vue'
import { computed, onMounted, ref, watch } from 'vue'
import { useFlashcardStore } from '@/stores/flashcard-store.ts'
import { useToggleStore } from '@/stores/toggle-store.ts'
import { useFlashcardSetStore } from '@/stores/flashcard-set-store.ts'
import { useSpaceToaster } from '@/stores/toast-store.ts'
import { useAudioStore } from '@/stores/audio-store.ts'
import { usePictureStore } from '@/stores/picture-store.ts'
import { type Flashcard } from '@/model/flashcard.ts'
import {
  flashcardSides,
  changeFlashcardSides,
  copyFlashcard,
} from '@/core-logic/flashcard-logic.ts'
import { storeToRefs } from 'pinia'
import {
  sendFlashcardRemovalRequest,
  sendFlashcardUpdateRequest,
} from '@/api/api-client.ts'
import { Log, LogTag } from '@/utils/logger.ts'
import { userApiErrors } from '@/api/user-api-error.ts'
import { useAudioCache } from '@/stores/audio-cache.ts'
import { usePictureCache } from '@/stores/picture-cache.ts'
import {
  fetchFlashcardAudioBlob,
  removeFlashcardAudioBlob,
  uploadFlashcardAudioBlob
} from '@/core-logic/flashcard-audio-logic.ts'
import {
  fetchFlashcardPictureBlob,
  removeFlashcardPictureBlob,
  uploadFlashcardPictureBlob
} from '@/core-logic/flashcard-picture-logic.ts'

const flashcard = defineModel<Flashcard | undefined>('flashcard', { default: undefined })
const removed = defineModel<boolean>('removed', { default: false })
const audioChanged = defineModel<boolean>('audioChanged', { default: false })
const pictureChanged = defineModel<boolean>('pictureChanged', { default: false })

const toggleStore = useToggleStore()
const flashcardSetStore = useFlashcardSetStore()
const flashcardStore = useFlashcardStore()
const audioStore = useAudioStore()
const audioCache = useAudioCache()
const pictureStore = usePictureStore()
const pictureCache = usePictureCache()
const toaster = useSpaceToaster()

const { flashcardSet } = storeToRefs(flashcardStore)

const frontSide = ref(flashcard.value?.frontSide ?? '')
const frontSideAudioId = ref(audioStore.getAudioId(flashcard.value?.id, flashcardSides.FRONT))
const frontSideAudio = ref<Blob | undefined>()
const frontSideAudioSize = ref<number | undefined>()
const frontSidePicture = ref<Blob | undefined>()
const frontSidePictureId = ref(pictureStore.getPictureId(flashcard.value?.id, flashcardSides.FRONT))
const frontSidePictureSize = ref<number | undefined>()
const backSide = ref(flashcard.value?.backSide ?? '')
const backSideAudioId = ref(audioStore.getAudioId(flashcard.value?.id, flashcardSides.BACK))
const backSideAudio = ref<Blob | undefined>()
const backSideAudioSize = ref<number | undefined>()
const backSidePicture = ref<Blob | undefined>()
const backSidePictureId = ref(pictureStore.getPictureId(flashcard.value?.id, flashcardSides.BACK))
const backSidePictureSize = ref<number | undefined>()
const cancelButton = ref<InstanceType<typeof SmartButton>>()
const removeButton = ref<InstanceType<typeof SmartButton>>()
const updateButton = ref<InstanceType<typeof SmartButton>>()
const sidesRef = ref<InstanceType<typeof FlashcardInput>>()

const stateChanged = computed(() => {
  return flashcard.value?.frontSide !== frontSide.value
    || flashcard.value?.backSide !== backSide.value
    || isFrontSideAudioChanged.value || isBackSideAudioChanged.value
    || isFrontSidePictureChanged.value || isBackSidePictureChanged.value
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

const isFrontSidePictureChanged = computed(() => {
  if (!frontSidePictureId.value) {
    return frontSidePicture.value !== undefined && frontSidePicture.value?.size > 0
  } else {
    return frontSidePictureSize.value !== frontSidePicture.value?.size
  }
})

const isBackSidePictureChanged = computed(() => {
  if (!backSidePictureId.value) {
    return backSidePicture.value !== undefined && backSidePicture.value?.size > 0
  } else {
    return backSidePictureSize.value !== backSidePicture.value?.size
  }
})

async function fetchAudio() {
  const set = flashcardSet.value
  const card = flashcard.value

  if (!set || !card) return

  await Promise.all([
    (async function () {
      if (frontSideAudioId.value) {
        return await fetchFlashcardAudioBlob(set.id, card.id, flashcardSides.FRONT)
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
        return await fetchFlashcardAudioBlob(set.id, card.id, flashcardSides.BACK)
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
        return uploadFlashcardAudioBlob(set, card, frontSideAudio.value, flashcardSides.FRONT)
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
        return uploadFlashcardAudioBlob(set, card, backSideAudio.value, flashcardSides.BACK)
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
        return removeFlashcardAudioBlob(set, card, frontSideAudioId.value, flashcardSides.FRONT)
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
        return removeFlashcardAudioBlob(set, card, backSideAudioId.value, flashcardSides.BACK)
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

async function fetchPicture() {
  const set = flashcardSet.value
  const card = flashcard.value

  if (!set || !card) return

  await Promise.all([
    (async function () {
      if (frontSidePictureId.value) {
        return await fetchFlashcardPictureBlob(set.id, card.id, flashcardSides.FRONT)
          .then((blob) => {
            frontSidePicture.value = blob
            frontSidePictureSize.value = blob?.size
          })
      } else {
        frontSidePicture.value = undefined
        frontSidePictureSize.value = undefined
      }
    })(),
    (async function () {
      if (backSidePictureId.value) {
        return await fetchFlashcardPictureBlob(set.id, card.id, flashcardSides.BACK)
          .then((blob) => {
            backSidePicture.value = blob
            backSidePictureSize.value = blob?.size
          })
      } else {
        backSidePicture.value = undefined
        backSidePictureSize.value = undefined
      }
    })(),
  ])
}

async function uploadPictureIfRelevant(): Promise<boolean> {
  const set = flashcardSet.value
  const card = flashcard.value

  if (!set || !card) {
    Log.error(LogTag.LOGIC, `Cannot upload picture: both FlashcardSet.id=${set?.id} and Flashcard.id=${card?.id} must be defined`)
    return true
  }

  return await Promise.all([
    (async function () {
      if (isFrontSidePictureChanged.value && frontSidePicture.value) {
        return uploadFlashcardPictureBlob(set, card, frontSidePicture.value, flashcardSides.FRONT)
          .then((success) => {
            if (success) {
              pictureChanged.value = true
            }
            return success
          })
      } else {
        return true
      }
    })(),
    (async function () {
      if (isBackSidePictureChanged.value && backSidePicture.value) {
        return uploadFlashcardPictureBlob(set, card, backSidePicture.value, flashcardSides.BACK)
          .then((success) => {
            if (success) {
              pictureChanged.value = true
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

async function removePictureIfRelevant(): Promise<boolean> {
  const set = flashcardSet.value
  const card = flashcard.value

  if (!set || !card) {
    Log.error(LogTag.LOGIC, `Cannot remove picture blobs: both FlashcardSet.id=${set?.id} and Flashcard.id=${card?.id} must be defined`)
    return true
  }

  return await Promise.all([
    (async function () {
      if (frontSidePictureId.value && !frontSidePicture.value && frontSidePictureSize.value) {
        return removeFlashcardPictureBlob(set, card, frontSidePictureId.value, flashcardSides.FRONT)
          .then((result) => {
            if (result) {
              frontSidePictureId.value = undefined
              frontSidePictureSize.value = undefined
              pictureChanged.value = true
            }
            return result
          })
      } else {
        return true
      }
    })(),
    (async function () {
      if (backSidePictureId.value && !backSidePicture.value && backSidePictureSize.value) {
        return removeFlashcardPictureBlob(set, card, backSidePictureId.value, flashcardSides.BACK)
          .then((result) => {
            if (result) {
              backSidePictureId.value = undefined
              backSidePictureSize.value = undefined
              pictureChanged.value = true
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
  pictureChanged.value = false
}

async function remove() {
  const done = await removeFlashcard()
  if (done) {
    await resetState()
    toggleModalForm()
    removed.value = true
    audioChanged.value = false
    pictureChanged.value = false
  }
}

async function update() {
  if (stateChanged.value) {
    sidesRef.value?.validate()
    if (!sidesRef.value?.invalid) {
      const updated = await updateFlashcard()
      if (updated) {
        const uploaded = await Promise.all([uploadAudioIfRelevant(), uploadPictureIfRelevant()])
        const removed = await Promise.all([removeAudioIfRelevant(), removePictureIfRelevant()])
        if (uploaded.every(Boolean) && removed.every(Boolean)) {
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
      audioStore.removeAudioId(flashcardId, flashcardSides.FRONT)
      audioStore.removeAudioId(flashcardId, flashcardSides.BACK)
      audioCache.deleteAudio(flashcardId, flashcardSides.FRONT)
      audioCache.deleteAudio(flashcardId, flashcardSides.BACK)
      pictureStore.removePictureId(flashcardId, flashcardSides.FRONT)
      pictureStore.removePictureId(flashcardId, flashcardSides.BACK)
      pictureCache.deletePicture(flashcardId, flashcardSides.FRONT)
      pictureCache.deletePicture(flashcardId, flashcardSides.BACK)
      frontSideAudio.value = undefined
      backSideAudio.value = undefined
      frontSideAudioId.value = undefined
      backSideAudioId.value = undefined
      frontSideAudioSize.value = undefined
      backSideAudioSize.value = undefined
      frontSidePicture.value = undefined
      backSidePicture.value = undefined
      frontSidePictureId.value = undefined
      backSidePictureId.value = undefined
      frontSidePictureSize.value = undefined
      backSidePictureSize.value = undefined
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
  sidesRef.value?.resetState()
  frontSide.value = flashcard.value?.frontSide ?? ''
  frontSideAudioId.value = audioStore.getAudioId(flashcard.value?.id, flashcardSides.FRONT)
  frontSidePictureId.value = pictureStore.getPictureId(flashcard.value?.id, flashcardSides.FRONT)
  backSide.value = flashcard.value?.backSide ?? ''
  backSideAudioId.value = audioStore.getAudioId(flashcard.value?.id, flashcardSides.BACK)
  backSidePictureId.value = pictureStore.getPictureId(flashcard.value?.id, flashcardSides.BACK)
  await Promise.all([fetchAudio(), fetchPicture()])
}

watch(flashcard, async (newVal) => {
  Log.log(LogTag.DEBUG, 'watch.flashcard', newVal)
  frontSide.value = newVal?.frontSide ?? ''
  frontSideAudioId.value = audioStore.getAudioId(newVal?.id, flashcardSides.FRONT)
  frontSidePictureId.value = pictureStore.getPictureId(newVal?.id, flashcardSides.FRONT)
  backSide.value = newVal?.backSide ?? ''
  backSideAudioId.value = audioStore.getAudioId(newVal?.id, flashcardSides.BACK)
  backSidePictureId.value = pictureStore.getPictureId(newVal?.id, flashcardSides.BACK)
  await Promise.all([fetchAudio(), fetchPicture()])
})

onMounted(async () => {
  await Promise.all([fetchAudio(), fetchPicture()])
})

</script>

<style scoped>
.modal-control-buttons {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  gap: 10px;
}
</style>
