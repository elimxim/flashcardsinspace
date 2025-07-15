<template>
  <div v-if="visible" class="modal-overlay" role="dialog" tabindex="-1">
    <div class="modal-window">
      <h2 class="modal-title">New flashcard set</h2>
      <div class="modal-body">
        <div class="modal-form-group">
          <input id="flashcardSetName" v-model="flashcardSetName" rows="1"
                    placeholder="Flashcard set name"
                    class="modal-input"/>
          <span v-if="$v.flashcardSetName.$errors.length" class="modal-error-message">Please don't forget to fill this out</span>
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
import { defineEmits, defineProps, onMounted, onUnmounted, ref } from 'vue';
import { useFlashcardDataStore } from '@/stores/flashcard-data.ts';
import { useFlashcardStateStore } from '@/stores/flashcard-state.ts';
import { type FlashcardSet, type Language } from '@/models/flashcards.ts';
import { type User } from '@/models/users.ts'
import { required } from '@vuelidate/validators';
import { useVuelidate } from '@vuelidate/core';

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

const flashcardSetName = ref('')

const props = defineProps({
  visible: Boolean,
})

const emit = defineEmits(['update:visible']);

const validationRules = {
  flashcardSetName: { required },
};

const $v = useVuelidate(validationRules, { flashcardSetName: flashcardSetName });

function cancel() {
  cleanState();
  emit('update:visible', false);
}

function create() {
  $v.value.$touch()
  if (!$v.value.$invalid) {
    createNewFlashcardSet();
    cleanState();
    emit('update:visible', false);
  }
}

function createNewFlashcardSet() {
  // todo
  const language: Language = {
    code: "la",
    name: "Trulalalalal",
  }

  // todo
  const user: User = {
    id: 1,
    name: "Billy Bob",
    registeredAt: new Date(),
  }

  const flashcardSet: FlashcardSet = {
    id: 0,
    name: flashcardSetName.value,
    targetLanguage: language,
    flashcards: [],
    createdAt: null,
    lastUpdatedAt: null,
    default: false,
    user: user,
  }

  // todo save to DB
  dataStore.addFlashcardSet(flashcardSet)
  stateStore.setCurrFlashcardSet(flashcardSet)
}

function cleanState() {
  flashcardSetName.value = '';
  $v.value.$reset();
}

</script>

<style scoped>
</style>
