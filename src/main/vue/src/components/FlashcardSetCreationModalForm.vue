<template>
  <div v-if="visible" class="modal-overlay" role="dialog" tabindex="-1">
    <div class="modal-window">
      <h2 class="modal-title">New flashcard set</h2>
      <div class="modal-body">
        <div class="modal-form-group-line">
          <input v-model="flashcardSetName" placeholder="Flashcard set name" class="modal-input"/>
          <span v-if="$v.flashcardSetName.$errors.length" class="modal-error-message">
            Please don't forget to fill this out
          </span>
        </div>
        <div class="modal-form-group-line">
          <div class="modal-form-group-row">
            <span class="modal-icon" style="color: #686868;">
              <font-awesome-icon icon="fa-solid fa-globe"/>
            </span>
            <select v-model="selectedLanguage" class="modal-select">
              <option v-for="i in languages" :key="i.alpha2" :value="i">
                {{ i.name }}
              </option>
            </select>
          </div>
          <span v-if="$v.selectedLanguage.$errors.length" class="modal-error-message">
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
        <button class="modal-button modal-cancel-button" @click="cancel">Cancel</button>
        <button class="modal-button modal-create-button" @click="create">Create</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import '@/assets/modal.css'
import {
  computed,
  type ComputedRef,
  defineEmits,
  defineProps,
  onMounted,
  onUnmounted,
  type Ref,
  ref
} from 'vue';
import { useFlashcardDataStore } from '@/stores/flashcard-data.ts';
import { useFlashcardStateStore } from '@/stores/flashcard-state.ts';
import { type FlashcardSet } from '@/models/flashcard.ts';
import { type User } from '@/models/user.ts'
import { required } from '@vuelidate/validators';
import { useVuelidate } from '@vuelidate/core';
import type { Language } from '@/models/language.ts';
import { storeToRefs } from 'pinia';
import { useLanguageStore } from '@/stores/language.ts';
import { language } from '@vue/eslint-config-prettier';
import { useReviewStateStore } from '@/stores/review-state.ts';
import { useGlobalStateStore } from '@/stores/global-state.ts';

function handleKeydown(event: KeyboardEvent) {
  if (event.key === 'Escape') {
    cancel();
  } else if (event.key === 'Enter' && event.ctrlKey) {
    create();
  }
}

onMounted(() => {
  document.addEventListener('keydown', handleKeydown);
});

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown);
});

const dataStore = useFlashcardDataStore()
const stateStore = useFlashcardStateStore()
const reviewStateStore = useReviewStateStore()
const langStore = useLanguageStore()
const { languages } = storeToRefs(langStore)

const flashcardSetName = ref('')
const selectedLanguage: Ref<Language | undefined> = ref(undefined)

const props = defineProps({
  visible: Boolean,
})

const emit = defineEmits(['update:visible']);

const validationRules = {
  flashcardSetName: { required, blank: false },
  selectedLanguage: { required },
};

const $v = useVuelidate(validationRules, {
  flashcardSetName: flashcardSetName,
  selectedLanguage: selectedLanguage,
});

function cancel() {
  resetState();
  globalStateStore.toggleFlashcardSetCreationModalForm()
  emit('update:visible', false);
}

function create() {
  $v.value.$touch()
  if (!$v.value.$invalid) {
    createNewFlashcardSet();
    reviewStateStore.finishReview()
    resetState();
    globalStateStore.toggleFlashcardSetCreationModalForm()
    emit('update:visible', false);
  }
}

function createNewFlashcardSet() {
  // todo
  const user: User = {
    id: 1,
    name: "Billy Bob",
    registeredAt: new Date(),
  }

  const flashcardSet: FlashcardSet = {
    id: 0,
    name: flashcardSetName.value,
    language: selectedLanguage.value!,
    flashcardMap: new Map(),
    createdAt: new Date().toISOString(),
    lastUpdatedAt: null,
    default: false,
    user: user,
  }

  // todo save to DB
  dataStore.addFlashcardSet(flashcardSet)
  stateStore.setCurr(flashcardSet)
}

function resetState() {
  flashcardSetName.value = '';
  selectedLanguage.value = undefined;
  $v.value.$reset();
}

const globalStateStore = useGlobalStateStore()

</script>

<style scoped>
</style>
