<template>
  <div v-if="visible" class="modal-overlay" role="dialog" tabindex="-1">
    <div class="modal-window">
      <h2 class="modal-title">{{ title }}</h2>
      <div class="modal-body">
        <div class="modal-form-group-line">
          <textarea id="frontSide" v-model="frontSide" rows="3" placeholder="Front side"
                    class="modal-input"></textarea>
          <span v-if="$v.frontSide.$errors.length" class="modal-error-message">
            Please don't forget to fill this out
          </span>
        </div>
        <div class="modal-form-group-line">
          <textarea id="backSide" v-model="backSide" rows="3" placeholder="Back side"
                    class="modal-input"></textarea>
          <span v-if="$v.backSide.$errors.length" class="modal-error-message">
            Please don't forget to fill this out
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
import { defineEmits, defineProps, ref } from 'vue';
import { onMounted, onUnmounted } from 'vue';
import { useVuelidate } from '@vuelidate/core';
import { required } from '@vuelidate/validators';
import { useFlashcardDataStore } from '@/stores/flashcard-data.ts';
import { useFlashcardStateStore } from '@/stores/flashcard-state.ts';
import { type Flashcard, Level } from '@/models/flashcard.ts';
import { storeToRefs } from 'pinia';

function handleKeydown(event: KeyboardEvent) {
  if (event.key === 'Escape') {
    cancel();
  } else if (event.key === 'Enter' && event.ctrlKey) {
    create();
  }
}

onMounted(() => {
  document.addEventListener('keydown', handleKeydown)
});

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown)
});

const stateStore = useFlashcardStateStore()

const props = defineProps({
  visible: Boolean,
  title: String,
})

const emit = defineEmits(['update:visible'])

const frontSide = ref('')
const backSide = ref('')

const validationRules = {
  frontSide: { required },
  backSide: { required },
};

const $v = useVuelidate(validationRules, { frontSide, backSide })

function cancel() {
  cleanState()
  emit('update:visible', false)
}

function create() {
  $v.value.$touch()
  if (!$v.value.$invalid) {
    createNewFlashcard()
    cleanState()
    emit('update:visible', false)
  }
}

const flashcard: Flashcard = {
  id: 0,
  frontSide: frontSide.value,
  backSide: backSide.value,
  level: Level.FIRST,
  reviewedAt: null,
  reviewCount: 0,
  reviewHistory: [],
  createdAt: new Date().toISOString(),
  lastUpdatedAt: null,
}

function createNewFlashcard() {

  // todo save to DB
  stateStore.addFlashcard(flashcard)
}

function cleanState() {
  frontSide.value = ''
  backSide.value = ''
  $v.value.$reset()
}

</script>

<style scoped>
</style>
