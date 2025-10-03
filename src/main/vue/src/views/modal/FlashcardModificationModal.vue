<template>
  <Modal
    :visible="visible"
    :onPressExit="cancel"
    :onPressEnter="props.editMode ? update : create"
    :onPressDelete="remove"
    :focusOn="frontSideTextArea"
    :title="editMode ? 'Edit Flashcard' : 'New Flashcard'"
  >
    <template #control v-if="!editMode">
      <AwesomeButton
        ref="infiniteLoopButton"
        icon="fa-regular fa-circle"
        spinIcon="fa-solid fa-arrows-spin"
        spin
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
        <span class="text-error" v-if="frontSideMaxLengthInvalid">
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
        <span class="text-error" v-if="backSideMaxLengthInvalid">
          Your text has its own gravity! Maximum 512 characters.
        </span>
      </div>
    </div>
    <div class="modal-control-buttons">
      <SmartButton
        class="cancel-button"
        text="Cancel"
        :onClick="cancel"
        autoBlur
      />
      <SmartButton
        v-if="editMode"
        class="remove-button"
        text="Remove"
        :holdTime="1.2"
        :onClick="remove"
        autoBlur
      />
      <SmartButton
        v-if="editMode"
        class="update-button"
        text="Update"
        :onClick="update"
        :disabled="!stateChanged || formInvalid"
        autoBlur
      />
      <SmartButton
        v-if="!editMode"
        class="create-button"
        text="Create"
        :onClick="create"
        :disabled="formInvalid"
        autoBlur
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
import { useGlobalStore } from '@/stores/global-store.ts'
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
import { sendRemoveFlashcardRequest } from '@/api/api-client.ts'

const visible = defineModel<boolean>('visible', { default: false })
const flashcard = defineModel<Flashcard | null>('flashcard', { default: null })
const removed = defineModel<boolean>('removed', { default: false })

const props = withDefaults(defineProps<{
  editMode?: boolean,
}>(), {
  editMode: false,
})

const globalStore = useGlobalStore()
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

function create() {
  $v.value.$touch()
  if (!formInvalid.value) {
    addNewFlashcard()
    if (!infiniteLoopButton.value?.isPressed()) {
      toggleModalForm()
    }
    resetState()
  }
}

function update() {
  if (stateChanged.value) {
    $v.value.$touch()
    if (!formInvalid.value) {
      updateFlashcard()
      resetState()
      toggleModalForm()
    }
  }
}

async function removeFlashcard(): Promise<boolean> {
  const setId = flashcardSet.value?.id
  const flashcardId = flashcard.value?.id
  if (setId && flashcardId) {
    return await sendRemoveFlashcardRequest(setId, flashcardId).then(() => {
      flashcardSetStore.removeFlashcard(flashcardId)
      toaster.bakeSuccess('Success', 'Flashcard removed', 200)
      return true
    }).catch((error) => {
      toaster.bakeError('System error', error.response?.data)
      return false
    })
  } else {
    return false
  }
}

async function addNewFlashcard() {
  await flashcardSetStore.addFlashcard(
    newFlashcard(
      frontSide.value,
      backSide.value,
    )
  ).then(async () => {
    if (flashcardSet.value !== null && !isStarted.value) {
      await chronoStore.addInitialChronoday(flashcardSet.value)
        .then(async () => {
          await flashcardSetStore.syncFlashcardSet().then(async () => {
            if (flashcardSet.value != null) {
              flashcardSetsStore.overrideFlashcardSet(flashcardSet.value)
            }
          })
        })
    }
  })
}

function updateFlashcard() {
  if (flashcard.value) {
    const updatedFlashcard = updateFlashcardSides(
      flashcard.value,
      frontSide.value,
      backSide.value
    )
    flashcardSetStore.updateFlashcard(updatedFlashcard)
  } else {
    console.error("can't update an undefined flashcard")
  }
}

function toggleModalForm() {
  if (props.editMode) {
    globalStore.toggleFlashcardEditModalForm()
    reviewStore.setEditFormWasOpened(true)
  } else {
    globalStore.toggleFlashcardCreationModalForm()
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
