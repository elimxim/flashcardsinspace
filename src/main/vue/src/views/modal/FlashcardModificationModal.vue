<template>
  <Modal
    :visible="editMode ? modalStore.flashcardEditOpen : modalStore.flashcardCreationOpen"
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
          :placeholder="frontSideNotSet ? 'Front side cannot be empty' : 'Front side'"
          area
        />
        <span v-if="frontSideMaxLengthInvalid" class="text-error">
          Your text has its own gravity! Maximum 512 characters.
        </span>
      </div>
      <div class="modal-main-area--inner">
        <SmartInput
          v-model="backSide"
          type="text"
          :invalid="backSideInvalid"
          :placeholder="backSideNotSet ? 'Back side cannot be empty' : 'Back side'"
          area
        />
        <span v-if="backSideMaxLengthInvalid" class="text-error">
          Your text has its own gravity! Maximum 512 characters.
        </span>
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
import {
  computed,
  defineProps,
  nextTick,
  ref,
  watch
} from 'vue'
import { useVuelidate } from '@vuelidate/core'
import { maxLength, required } from '@vuelidate/validators'
import { useFlashcardSetStore } from '@/stores/flashcard-set-store.ts'
import { useModalStore } from '@/stores/modal-store.ts'
import { useChronoStore } from '@/stores/chrono-store.ts'
import { useFlashcardSetsStore } from '@/stores/flashcard-sets-store.ts'
import { useReviewStore } from '@/stores/review-store.ts'
import { useSpaceToaster } from '@/stores/toast-store.ts'
import { type Flashcard } from '@/model/flashcard.ts'
import {
  newFlashcard,
  updateFlashcardSides
} from '@/core-logic/flashcard-logic.ts'
import { storeToRefs } from 'pinia'
import {
  sendFlashcardRemovalRequest,
  sendFlashcardCreationRequest,
  sendFlashcardSetInitRequest, sendFlashcardUpdateRequest,

} from '@/api/api-client.ts'

const flashcard = defineModel<Flashcard | null>('flashcard', { default: null })
const removed = defineModel<boolean>('removed', { default: false })

const props = withDefaults(defineProps<{
  editMode?: boolean,
}>(), {
  editMode: false,
})

const modalStore = useModalStore()
const chronoStore = useChronoStore()
const flashcardSetsStore = useFlashcardSetsStore()
const flashcardSetStore = useFlashcardSetStore()
const reviewStore = useReviewStore()
const toaster = useSpaceToaster()

const { flashcardSet, isStarted } = storeToRefs(flashcardSetStore)

const frontSide = ref(flashcard.value?.frontSide ?? '')
const frontSideTextArea = ref<HTMLElement>()
const backSide = ref(flashcard.value?.backSide ?? '')
const infiniteLoopButton = ref<InstanceType<typeof AwesomeButton>>()

const stateChanged = computed(() => {
  return flashcard.value?.frontSide !== frontSide.value
    || flashcard.value?.backSide !== backSide.value
})

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
const frontSideNotSet = computed(() =>
  $v.value.frontSide.$errors.find(v => v.$validator === 'required') !== undefined
)
const frontSideMaxLengthInvalid = computed(() =>
  $v.value.frontSide.$errors.find(v => v.$validator === 'maxLength') !== undefined
)
const backSideInvalid = computed(() => $v.value.backSide.$errors.length > 0)
const backSideNotSet = computed(() =>
  $v.value.backSide.$errors.find(v => v.$validator === 'required') !== undefined
)
const backSideMaxLengthInvalid = computed(() =>
  $v.value.backSide.$errors.find(v => v.$validator === 'maxLength') !== undefined
)

function cancel() {
  resetState()
  toggleModalForm()
  removed.value = false
}

async function remove() {
  const done = await removeFlashcard()
  if (done) {
    resetState()
    toggleModalForm()
    removed.value = true
  }
}

async function create() {
  $v.value.$touch()
  if (!formInvalid.value) {
    const added = await addNewFlashcard()
    if (added) {
      if (!infiniteLoopButton.value?.isPressed()) {
        toggleModalForm()
      }
      resetState()
    }
  }
}

async function update() {
  if (stateChanged.value) {
    $v.value.$touch()
    if (!formInvalid.value) {
      const updated = await updateFlashcard()
      if (updated) {
        toggleModalForm()
        resetState()
      }
    }
  }
}

async function removeFlashcard(): Promise<boolean> {
  if (!flashcardSet.value || !flashcard.value) return false
  const flashcardId = flashcard.value.id
  return await sendFlashcardRemovalRequest(flashcardSet.value.id, flashcardId)
    .then(() => {
      flashcardSetStore.removeFlashcard(flashcardId)
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
        flashcardSetStore.addNewFlashcard(response.data)
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
        flashcardSetsStore.updateSet(response.data.flashcardSet)
        flashcardSetStore.changeFlashcardSet(response.data.flashcardSet)
        flashcardSetStore.addNewFlashcard(flashcard)
        chronoStore.addChronodays(
          response.data.chronodays,
          response.data.currDay
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
      flashcardSetStore.changeFlashcard(response.data)
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
    modalStore.toggleFlashcardEdit()
    reviewStore.setEditFormWasOpened(true)
  } else {
    modalStore.toggleFlashcardCreation()
  }
}

function resetState() {
  frontSide.value = flashcard.value?.frontSide ?? ''
  backSide.value = flashcard.value?.backSide ?? ''
  $v.value.$reset()
  nextTick().then(() => {
    frontSideTextArea.value?.focus()
  })
}

watch(flashcard, (newVal) => {
  frontSide.value = newVal?.frontSide ?? ''
  backSide.value = newVal?.backSide ?? ''
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
