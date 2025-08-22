<template>
  <ModalForm :visible="visible"
             :onExit="cancel"
             title="New flashcard set">
    <div class="form-body">
      <div class="modal-vertical-group">
        <input class="modal-input"
               v-model="flashcardSetName"
               placeholder="Flashcard set name"/>
        <span class="modal-error-text" v-if="$v.flashcardSetName.$errors.length">
            Please don't forget to fill this out
          </span>
      </div>
      <div class="modal-vertical-group">
        <div class="modal-horizontal-group">
            <span class="modal-icon modal-globe-icon">
              <font-awesome-icon icon="fa-solid fa-globe"/>
            </span>
          <select class="modal-select" v-model="flashcardSetLanguage">
            <option v-for="i in languages" :key="i.id" :value="i">
              {{ i.name }}
            </option>
          </select>
        </div>
        <span class="modal-error-text" v-if="$v.selectedLanguage.$errors.length">
              Please choose one of the languages
          </span>
      </div>
      <div class="modal-message-group modal-info">
            <span class="modal-icon">
              <font-awesome-icon icon="fa-solid fa-circle-info"/>
            </span>
        <span class="modal-message-text">
              Please be careful. You will not be able to change the language in the future
          </span>
      </div>
    </div>
    <div class="modal-bottom">
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
  </ModalForm>
</template>

<script setup lang="ts">
import '@/assets/modal.css'
import ModalForm from '@/components/modal/ModalForm.vue'
import { defineEmits, defineProps, onMounted, onUnmounted, type Ref, ref } from 'vue'
import { useFlashcardDataStore } from '@/stores/flashcard-data-store.ts'
import { useFlashcardSetStore } from '@/stores/flashcard-set-store.ts'
import { type FlashcardSet } from '@/model/flashcard.ts'
import { type User } from '@/model/user.ts'
import { required } from '@vuelidate/validators'
import { useVuelidate } from '@vuelidate/core'
import type { Language } from '@/model/language.ts'
import { storeToRefs } from 'pinia'
import { useLanguageStore } from '@/stores/language-store.ts'
import { useReviewStore } from '@/stores/review-store.ts'
import { useGlobalStore } from '@/stores/global-store.ts'
import { createFlashcardSet } from '@/core-logic/flashcard-logic.ts'
import { useChronoStore } from '@/stores/chrono-store.ts'

defineProps({
  visible: Boolean,
})

const emit = defineEmits(['update:visible'])

const globalStore = useGlobalStore()
const flashcardDataStore = useFlashcardDataStore()
const flashcardSetStore = useFlashcardSetStore()
const chronoStore = useChronoStore()
const reviewStore = useReviewStore()
const languageStore = useLanguageStore()

const { languages } = storeToRefs(languageStore)
const { lastFlashcardSet } = storeToRefs(flashcardDataStore)
const { flashcardSet } = storeToRefs(flashcardSetStore)

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
  globalStore.toggleFlashcardSetCreationModalForm()
  emit('update:visible', false)
  cancelButton.value?.blur()
}

function create() {
  $v.value.$touch()
  if (!$v.value.$invalid) {
    createNewFlashcardSet()
    reviewStore.finishReview()
    resetState()
    globalStore.toggleFlashcardSetCreationModalForm()
    emit('update:visible', false)
  }
  createButton.value?.blur()
}

function createNewFlashcardSet() {
  if (flashcardSetLanguage.value === null) {
    throw new Error('Can\'t a new flashcard set because language is not set')
  }
  const newSet = createFlashcardSet(flashcardSetName.value, flashcardSetLanguage.value)
  flashcardDataStore.addFlashcardSet(newSet).then(() => {
    if (lastFlashcardSet.value !== null) {
      flashcardSetStore.loadData(lastFlashcardSet.value).then(() => {
        if (flashcardSet.value !== null) {
          chronoStore.loadData(flashcardSet.value)
        }
      })
    }
  })
}

onMounted(() => {
  document.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown)
})

function handleKeydown(event: KeyboardEvent) {
  if (event.key === 'Escape') {
    cancelButton.value?.click()
  } else if (event.key === 'Enter') {
    createButton.value?.click()
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
