<template>
  <ModalForm
    :visible="flashcardEditModalFormOpen"
    :focusable="false"
    :onPressExit="cancel"
    :onPressDelete="remove"
    :onPressEnter="props.editMode ? update : create"
    :title="editMode ? 'Edit flashcard' : 'New flashcard'"
  >
    <div class="form-body">
      <div class="modal-vertical-group">
          <textarea ref="frontSide"
                    class="modal-input"
                    rows="6"
                    placeholder="Front side"
                    v-model="flashcardFrontSide"/>
        <span class="modal-error-text" v-if="$v.frontSide.$errors.length">
            Please don't forget to fill this out
          </span>
      </div>
      <div class="modal-vertical-group">
          <textarea id="backSide"
                    class="modal-input"
                    rows="6"
                    placeholder="Back side"
                    v-model="flashcardBackSide"/>
        <span class="modal-error-text" v-if="$v.backSide.$errors.length">
            Please don't forget to fill this out
          </span>
      </div>
      <div class="modal-message-group modal-warning"
           v-if="removeConfirmation">
          <span class="modal-icon">
            <font-awesome-icon icon="fa-solid fa-triangle-exclamation"/></span>
        <span class="modal-message-text">
            Are you sure you want to remove this flashcard?
          </span>
      </div>
    </div>
    <div class="modal-bottom">
      <button class="modal-button modal-cancel-button"
              ref="cancelButton"
              @click="cancel">
        Cancel
      </button>
      <button class="modal-button modal-remove-button"
              ref="removeButton"
              v-if="editMode"
              @click="remove">
        Remove
      </button>
      <button class="modal-button modal-update-button"
              :class="{ 'modal-button-disabled': !stateChanged }"
              ref="updateButton"
              v-if="editMode"
              :disabled="!stateChanged"
              @click="update">
        Update
      </button>
      <button class="modal-button modal-create-button"
              ref="createButton"
              v-if="!editMode"
              @click="create">
        Create
      </button>
    </div>
  </ModalForm>
</template>

<script setup lang="ts">
import ModalForm from '@/components/modal/ModalForm.vue'
import {
  computed,
  defineProps,
  nextTick,
  ref,
  watch
} from 'vue'
import { useVuelidate } from '@vuelidate/core'
import { required } from '@vuelidate/validators'
import { useFlashcardSetStore } from '@/stores/flashcard-set-store.ts'
import { type Flashcard } from '@/model/flashcard.ts'
import { useGlobalStore } from '@/stores/global-store.ts'
import {
  newFlashcard,
  updateFlashcardSides
} from '@/core-logic/flashcard-logic.ts'
import { useChronoStore } from '@/stores/chrono-store.ts'
import { useFlashcardSetsStore } from '@/stores/flashcard-data-store.ts'
import { useReviewStore } from '@/stores/review-store.ts'
import { storeToRefs } from 'pinia'

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

const { flashcardSet, isStarted } = storeToRefs(flashcardSetStore)
const {
  flashcardEditModalFormOpen,
  flashcardCreationModalFormOpen,
} = storeToRefs(globalStore)

// state>

const flashcardFrontSide = ref(flashcard.value?.frontSide ?? '')
const flashcardBackSide = ref(flashcard.value?.backSide ?? '')

const validationRules = {
  frontSide: { required },
  backSide: { required },
}

const $v = useVuelidate(validationRules, {
  frontSide: flashcardFrontSide,
  backSide: flashcardBackSide,
})

const removeConfirmation = ref(false)

function resetState() {
  flashcardFrontSide.value = flashcard.value?.frontSide ?? ''
  flashcardBackSide.value = flashcard.value?.backSide ?? ''
  removeConfirmation.value = false
  $v.value.$reset()
  nextTick(() => {
    frontSide.value?.focus()
  })
}

watch(flashcardFrontSide, (_) => {
  removeConfirmation.value = false
})

watch(flashcardBackSide, (_) => {
  removeConfirmation.value = false
})

watch(flashcard, (newVal) => {
  flashcardFrontSide.value = newVal?.frontSide ?? ''
  flashcardBackSide.value = newVal?.backSide ?? ''
})

const stateChanged = computed(() => {
  return flashcard.value?.frontSide !== flashcardFrontSide.value
    || flashcard.value?.backSide !== flashcardBackSide.value
})

// <state

const cancelButton = ref<HTMLButtonElement>()
const removeButton = ref<HTMLButtonElement>()
const createButton = ref<HTMLButtonElement>()
const updateButton = ref<HTMLButtonElement>()
const frontSide = ref<HTMLTextAreaElement>()

watch(flashcardEditModalFormOpen, (newValue) => {
  if (newValue) {
    nextTick(() => {
      frontSide.value?.focus()
    })
  }
})

watch(flashcardCreationModalFormOpen, (newValue) => {
  if (newValue) {
    nextTick(() => {
      frontSide.value?.focus()
    })
  }
})

function cancel() {
  resetState()
  toggleModalForm()
  removed.value = false
  cancelButton.value?.blur()
}

function remove() {
  if (removeConfirmation.value) {
    removeFlashcard()
    resetState()
    toggleModalForm()
    removed.value = false
  } else {
    removeConfirmation.value = true
  }
  removeButton.value?.blur()
}

function create() {
  $v.value.$touch()
  if (!$v.value.$invalid) {
    addNewFlashcard()
    resetState()
    // fixme infinite loop
    // toggleModalForm()
  }
  createButton.value?.blur()
}

function update() {
  if (stateChanged.value) {
    $v.value.$touch()
    if (!$v.value.$invalid) {
      updateFlashcard()
      resetState()
      toggleModalForm()
    }
  }
  updateButton.value?.blur()
}

function removeFlashcard() {
  if (flashcard.value) {
    flashcardSetStore.removeFlashcard(flashcard.value.id)
  } else {
    console.error("can't remove an undefined flashcard")
  }
}

async function addNewFlashcard() {
  await flashcardSetStore.addFlashcard(
    newFlashcard(
      flashcardFrontSide.value,
      flashcardBackSide.value,
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
      flashcardFrontSide.value,
      flashcardBackSide.value
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

</script>

<style scoped>
.form-body {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 20px;
  min-width: 30vw;
  width: 30vw;
}
</style>
