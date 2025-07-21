<template>
  <div v-if="visible" class="modal-overlay" role="dialog" tabindex="-1">
    <div class="modal-window">
      <h2 class="modal-title">Settings</h2>
      <div class="modal-body">
        <div class="modal-form-group-line">
          <input class="modal-input"
                 placeholder="Flashcard set name"
                 v-model="flashcardSetName"/>
          <span class="modal-error-message" v-if="$v.flashcardSetName.$errors.length">
            Please don't forget to fill this out
          </span>
        </div>
        <div class="modal-form-group-line">
          <div class="modal-form-group-row">
            <span class="modal-icon modal-globe-icon">
              <font-awesome-icon icon="fa-solid fa-globe"/>
            </span>
            <input class="modal-input" v-model="flashcardSetLanguage" disabled/>
          </div>
        </div>
        <div class="modal-form-group-line">
          <label>
            <input type="checkbox" v-model="flashcardSetDefault"/>
            {{
              flashcardSetDefault
                ? "This flashcard set is set as the default"
                : "Set this flashcard set as the default (it will appear first if you have more than one)"
            }}
          </label>
        </div>
        <div class="modal-message-container modal-warning-message-container"
             v-if="removeConfirmation">
          <span class="modal-icon">
            <font-awesome-icon icon="fa-solid fa-triangle-exclamation"/></span>
          <span class="modal-message-text">
            Are you sure you want to remove '{{ flashcardSet?.name }}'? All progress and flash cards will disappear
          </span>
        </div>
      </div>
      <div class="modal-buttons">
        <button class="modal-button modal-cancel-button"
                ref="cancelButton"
                @click="cancel">
          Cancel
        </button>
        <button class="modal-button modal-remove-button"
                ref="removeButton"
                @click="remove">
          Remove
        </button>
        <button class="modal-button modal-update-button"
                :class="{ 'modal-button-disabled': !stateChanged }"
                ref="updateButton"
                :disabled="!stateChanged"
                @click="update">
          Update
        </button>
      </div>
    </div>
  </div>

</template>

<script setup lang="ts">
import {
  computed,
  defineEmits,
  defineProps,
  onMounted,
  onUnmounted,
  ref,
  watch
} from 'vue'
import { required } from '@vuelidate/validators'
import { useVuelidate } from '@vuelidate/core'
import { useFlashcardDataStore } from '@/stores/flashcard-data.ts'
import { useFlashcardStateStore } from '@/stores/flashcard-state.ts'
import { storeToRefs } from 'pinia'
import { useReviewStateStore } from '@/stores/review-state.ts'
import { useGlobalStateStore } from '@/stores/global-state.ts'

const emit = defineEmits(['update:visible'])

defineProps({
  visible: Boolean,
})

const globalStateStore = useGlobalStateStore()
const reviewStateStore = useReviewStateStore()
const flashcardDataStore = useFlashcardDataStore()
const flashcardStateStore = useFlashcardStateStore()

const { flashcardSets } = storeToRefs(flashcardDataStore)
const { flashcardSet } = storeToRefs(flashcardStateStore)

// state>

const flashcardSetName = ref(flashcardSet.value?.name ?? null)
const flashcardSetLanguage = ref(flashcardSet.value?.language.name ?? null)
const flashcardSetDefault = ref(flashcardSet.value?.default ?? null)

const stateChanged = computed(() => {
  return flashcardSet.value?.name !== flashcardSetName.value
    || flashcardSet.value?.default !== flashcardSetDefault.value
})

const removeConfirmation = ref(false)

watch(flashcardSetName, (_) => {
  removeConfirmation.value = false
})

watch(flashcardSetDefault, (_) => {
  removeConfirmation.value = false
})

flashcardStateStore.$subscribe((mutation, newState) => {
  flashcardSetName.value = newState.flashcardSet?.name ?? null
  flashcardSetLanguage.value = newState.flashcardSet?.language.name ?? null
  flashcardSetDefault.value = newState.flashcardSet?.default ?? null
})

const validationRules = {
  flashcardSetName: { required },
}

const $v = useVuelidate(validationRules, { flashcardSetName: flashcardSetName })

function resetState() {
  $v.value.$reset()
  flashcardSetName.value = flashcardSet.value?.name ?? null
  flashcardSetLanguage.value = flashcardSet.value?.language.name ?? null
  flashcardSetDefault.value = flashcardSet.value?.default ?? null
  removeConfirmation.value = false
}

// <state

const cancelButton = ref<HTMLButtonElement>()
const removeButton = ref<HTMLButtonElement>()
const updateButton = ref<HTMLButtonElement>()

function cancel() {
  resetState()
  globalStateStore.toggleFlashcardSetSettingsModalForm()
  emit('update:visible', false)
}

function remove() {
  if (removeConfirmation.value) {
    removeFlashcardSet()
    resetState()
    globalStateStore.toggleFlashcardSetSettingsModalForm()
    emit('update:visible', false)
  } else {
    removeConfirmation.value = true
  }
}

function update() {
  if (stateChanged.value) {
    $v.value.$touch()
    if (!$v.value.$invalid) {
      updateFlashcardSet()
      resetState()
      globalStateStore.toggleFlashcardSetSettingsModalForm()
      emit('update:visible', false)
    }
  }
}

function removeFlashcardSet() {
  if (flashcardSet.value !== null) {
    flashcardDataStore.removeFlashcardSet(flashcardSet.value)
    reviewStateStore.finishReview()
    flashcardStateStore.initFromList(flashcardSets.value)
  }
}

function updateFlashcardSet() {
  if (flashcardSetName.value !== null) {
    flashcardStateStore.setName(flashcardSetName.value)
  }
  if (flashcardSetDefault.value !== null) {
    flashcardStateStore.setDefault(flashcardSetDefault.value)
  }
}

onMounted(() => {
  document.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown)
})

function handleKeydown(event: KeyboardEvent) {
  if (event.shiftKey && (event.key === 'c' || event.key === 'C')) {
    cancelButton.value?.click()
  } else if (event.shiftKey && (event.key === 'u' || event.key === 'U')) {
    updateButton.value?.click()
  } else if (event.shiftKey && (event.key === 'r' || event.key === 'R')) {
    removeButton.value?.click()
  }
}

</script>

<style scoped>
</style>
