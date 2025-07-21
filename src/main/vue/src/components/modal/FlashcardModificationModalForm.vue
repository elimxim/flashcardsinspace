<template>
  <div class="modal-overlay" role="dialog" tabindex="-1" v-if="visible">
    <div class="modal-window">
      <h2 class="modal-title">
        {{ editMode ? 'Edit flashcard' : 'New flashcard' }}
      </h2>
      <div class="modal-body">
        <div class="modal-form-group-line">
          <textarea id="frontSide"
                    class="modal-input"
                    rows="3"
                    placeholder="Front side"
                    v-model="flashcardFrontSide"/>
          <span class="modal-error-message" v-if="$v.frontSide.$errors.length">
            Please don't forget to fill this out
          </span>
        </div>
        <div class="modal-form-group-line">
          <textarea id="backSide"
                    class="modal-input"
                    rows="3"
                    placeholder="Back side"
                    v-model="flashcardBackSide"/>
          <span class="modal-error-message" v-if="$v.backSide.$errors.length">
            Please don't forget to fill this out
          </span>
        </div>
        <div class="modal-message-container modal-warning-message-container"
             v-if="removeConfirmation">
          <span class="modal-icon">
            <font-awesome-icon icon="fa-solid fa-triangle-exclamation"/></span>
          <span class="modal-message-text">
            Are you sure you want to remove this flashcard?
          </span>
        </div>
      </div>
      <div class="modal-buttons">
        <button class="modal-button modal-cancel-button"
                @click="cancel">
          Cancel
        </button>
        <button class="modal-button modal-remove-button"
                v-if="editMode"
                @click="remove">
          Remove
        </button>
        <button class="modal-button modal-create-button"
                :class="{ 'modal-button-disabled': !stateChanged }"
                v-if="editMode"
                :disabled="!stateChanged"
                @click="update">
          Update
        </button>
        <button class="modal-button modal-create-button"
                v-if="!editMode"
                @click="create">
          Create
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import '@/assets/modal.css'
import {
  computed,
  defineEmits,
  defineProps,
  onMounted,
  onUnmounted,
  type PropType,
  ref,
  watch
} from 'vue'
import { useVuelidate } from '@vuelidate/core'
import { required } from '@vuelidate/validators'
import { useFlashcardStateStore } from '@/stores/flashcard-state.ts'
import { type Flashcard } from '@/models/flashcard.ts'
import { useGlobalStateStore } from '@/stores/global-state.ts'
import { newFlashcard, updateFlashcardSides } from '@/core-logic/flashcard-logic.ts'
import { levels } from '@/core-logic/level-logic.ts';

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

const globalStateStore = useGlobalStateStore()
const flashcardStateStore = useFlashcardStateStore()

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

function cancel() {
  resetState()

  emit('update:visible', false)
  emit('update:removed', false)
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
}

function create() {
  $v.value.$touch()
  if (!$v.value.$invalid) {
    addNewFlashcard()
    resetState()
    toggleModalForm()
    emit('update:visible', false)
  }
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
}

function removeFlashcard() {
  if (props.flashcard !== null && props.flashcard !== undefined) {
    flashcardStateStore.removeFlashcard(props.flashcard?.id)
  } else {
    console.error("can't remove an undefined flashcard")
  }
}

function addNewFlashcard() {
  flashcardStateStore.addFlashcard(
    newFlashcard(
      flashcardFrontSide.value,
      flashcardBackSide.value,
    )
  )
}

function updateFlashcard() {
  if (props.flashcard !== null && props.flashcard !== undefined) {
    const flashcard = updateFlashcardSides(
      props.flashcard,
      flashcardFrontSide.value,
      flashcardBackSide.value
    )
    flashcardStateStore.updateFlashcard(flashcard)
  } else {
    console.error("can't update an undefined flashcard")
  }
}

function toggleModalForm() {
  if (props.editMode) {
    globalStateStore.toggleFlashcardEditModalForm()
  } else {
    globalStateStore.toggleFlashcardCreationModalForm()
  }
}

onMounted(() => {
  document.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown)
})

function handleKeydown(event: KeyboardEvent) {
  if (event.key === 'Escape') {
    cancel()
  } else if (event.key === 'Enter' && event.ctrlKey) {
    create()
  }
}

</script>

<style scoped>
</style>
