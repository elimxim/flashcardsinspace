<template>
  <ModalForm
    :visible="visible"
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
  defineEmits,
  defineProps, nextTick,
  type PropType,
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

const props = defineProps({
  visible: Boolean,
  editMode: Boolean,
  removed: Boolean,
  flashcard: {
    type: Object as PropType<Flashcard | null>,
    required: false,
  },
})

const emit = defineEmits([
  'update:visible',
  'update:removed',
])

const globalStore = useGlobalStore()
const chronoStore = useChronoStore()
const flashcardSetsStore = useFlashcardSetsStore()
const flashcardSetStore = useFlashcardSetStore()
const reviewStore = useReviewStore()

const { flashcardSet, isStarted } = storeToRefs(flashcardSetStore)

// state>

const flashcardFrontSide = ref(props.flashcard?.frontSide ?? '')
const flashcardBackSide = ref(props.flashcard?.backSide ?? '')

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
  flashcardFrontSide.value = props.flashcard?.frontSide ?? ''
  flashcardBackSide.value = props.flashcard?.backSide ?? ''
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

watch(() => props.flashcard, (newValue, _) => {
  flashcardFrontSide.value = newValue?.frontSide ?? ''
  flashcardBackSide.value = newValue?.backSide ?? ''
})

const stateChanged = computed(() => {
  return props.flashcard?.frontSide !== flashcardFrontSide.value
    || props.flashcard?.backSide !== flashcardBackSide.value
})

// <state

const cancelButton = ref<HTMLButtonElement>()
const removeButton = ref<HTMLButtonElement>()
const createButton = ref<HTMLButtonElement>()
const updateButton = ref<HTMLButtonElement>()
const frontSide = ref<HTMLTextAreaElement>()

watch(() => props.visible, (newValue) => {
  if (newValue) {
    nextTick(() => {
      frontSide.value?.focus()
    })
  }
})

function cancel() {
  resetState()
  emit('update:visible', false)
  emit('update:removed', false)
  cancelButton.value?.blur()
}

function remove() {
  if (removeConfirmation.value) {
    removeFlashcard()
    resetState()
    toggleModalForm()
    emit('update:visible', false)
    emit('update:removed', true)
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
    // emit('update:visible', false)
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
      emit('update:visible', false)
    }
  }
  updateButton.value?.blur()
}

function removeFlashcard() {
  if (props.flashcard !== null && props.flashcard !== undefined) {
    flashcardSetStore.removeFlashcard(props.flashcard?.id)
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
  if (props.flashcard !== null && props.flashcard !== undefined) {
    const flashcard = updateFlashcardSides(
      props.flashcard,
      flashcardFrontSide.value,
      flashcardBackSide.value
    )
    flashcardSetStore.updateFlashcard(flashcard)
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
