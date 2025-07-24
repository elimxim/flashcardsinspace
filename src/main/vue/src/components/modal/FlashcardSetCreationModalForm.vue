<template>
  <div class="modal-overlay" role="dialog" tabindex="-1" v-if="visible">
    <div class="modal-window">
      <h2 class="modal-title">New flashcard set</h2>
      <div class="modal-body">
        <div class="modal-form-group-line">
          <input class="modal-input"
                 v-model="flashcardSetName"
                 placeholder="Flashcard set name"/>
          <span class="modal-error-message" v-if="$v.flashcardSetName.$errors.length">
            Please don't forget to fill this out
          </span>
        </div>
        <div class="modal-form-group-line">
          <div class="modal-form-group-row">
            <span class="modal-icon modal-globe-icon">
              <font-awesome-icon icon="fa-solid fa-globe"/>
            </span>
            <select class="modal-select" v-model="flashcardSetLanguage">
              <option v-for="i in languages" :key="i.alpha2" :value="i">
                {{ i.name }}
              </option>
            </select>
          </div>
          <span class="modal-error-message" v-if="$v.selectedLanguage.$errors.length">
              Please choose one of the languages
          </span>
        </div>
        <div class="modal-message-container modal-info-message-container">
            <span class="modal-icon">
              <font-awesome-icon icon="fa-solid fa-circle-info"/>
            </span>
          <span class="modal-message-text">
              Please be careful. You will not be able to change the language in the future
          </span>
        </div>
      </div>
      <div class="modal-buttons">
        <button class="modal-button modal-cancel-button"
                ref="cancelButton"
                @click="cancel">
          Cancel
        </button>
        <button class="modal-button modal-create-button"
                ref="createButton"
                @click="create">
          Create
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import '@/assets/modal.css'
import { defineEmits, defineProps, onMounted, onUnmounted, type Ref, ref } from 'vue'
import { useFlashcardDataStore } from '@/stores/flashcard-data.ts'
import { useFlashcardStateStore } from '@/stores/flashcard-state.ts'
import { type FlashcardSet } from '@/models/flashcard.ts'
import { type User } from '@/models/user.ts'
import { required } from '@vuelidate/validators'
import { useVuelidate } from '@vuelidate/core'
import type { Language } from '@/models/language.ts'
import { storeToRefs } from 'pinia'
import { useLanguageDataStore } from '@/stores/language-data.ts'
import { useReviewStateStore } from '@/stores/review-state.ts'
import { useGlobalStateStore } from '@/stores/global-state.ts'
import { newFlashcardSet } from '@/core-logic/flashcard-logic.ts';

defineProps({
  visible: Boolean,
})

const emit = defineEmits(['update:visible'])

const globalStateStore = useGlobalStateStore()
const flashcardDataStore = useFlashcardDataStore()
const flashcardStateStore = useFlashcardStateStore()
const reviewStateStore = useReviewStateStore()
const languageDataStore = useLanguageDataStore()

const { languages } = storeToRefs(languageDataStore)

// state>

const flashcardSetName = ref('')
const flashcardSetLanguage: Ref<Language | null> = ref(null)

const validationRules = {
  flashcardSetName: { required, blank: false },
  selectedLanguage: { required },
}

const $v = useVuelidate(validationRules, {
  flashcardSetName: flashcardSetName,
  selectedLanguage: flashcardSetLanguage,
})

function resetState() {
  flashcardSetName.value = ''
  flashcardSetLanguage.value = null
  $v.value.$reset()
}

// <state

const cancelButton = ref<HTMLButtonElement>()
const createButton = ref<HTMLButtonElement>()

function cancel() {
  resetState()
  globalStateStore.toggleFlashcardSetCreationModalForm()
  emit('update:visible', false)
  cancelButton.value?.blur()
}

function create() {
  $v.value.$touch()
  if (!$v.value.$invalid) {
    createNewFlashcardSet()
    reviewStateStore.finishReview()
    resetState()
    globalStateStore.toggleFlashcardSetCreationModalForm()
    emit('update:visible', false)
  }
  createButton.value?.blur()
}

function createNewFlashcardSet() {
  if (flashcardSetLanguage.value === null) {
    throw new Error('Can\'t a new flashcard set because language is not set')
  }
  const flashcardSet = newFlashcardSet(flashcardSetName.value, flashcardSetLanguage.value)
  flashcardDataStore.addFlashcardSet(flashcardSet)
  flashcardStateStore.init(flashcardSet)
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
  } else if (event.shiftKey && (event.key === 'e' || event.key === 'E')) {
    createButton.value?.click()
  }
}

</script>

<style scoped>
</style>
