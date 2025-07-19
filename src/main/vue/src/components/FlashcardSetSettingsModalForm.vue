<template>
  <div v-if="visible" class="modal-overlay" role="dialog" tabindex="-1">
    <div class="modal-window">
      <h2 class="modal-title">Settings</h2>
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
            <input v-model="flashcardSetTargetLanguage" class="modal-input" disabled="true"/>
          </div>
        </div>
        <div class="modal-form-group-line">
          <label>
            <input type="checkbox" v-model="defaultFlashcardSet"/>
            {{
              defaultFlashcardSet
                ? "This flashcard set is set as the default"
                : "Set this flashcard set as the default (it will appear first if you have more than one)"
            }}
          </label>
        </div>
        <div v-if="flashcardRemoveConfirmation"
             class="modal-message-container modal-warning-message-container">
          <span class="modal-icon">
            <font-awesome-icon icon="fa-solid fa-triangle-exclamation"/></span>
          <span class="modal-message-text">
            Are you sure you want to remove '{{ currFlashcardSet?.name }}'? All progress and flash cards will disappear
          </span>
        </div>
      </div>
      <div class="modal-buttons">
        <button class="modal-button modal-cancel-button" @click="cancel">Cancel</button>
        <button class="modal-button modal-remove-button" @click="remove">Remove</button>
        <button class="modal-button modal-update-button" @click="update"
                :class="{ 'modal-button-disabled': !flashcardSetHasChanges }"
                :disabled="!flashcardSetHasChanges">
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
  type Ref,
  ref,
  watch
} from 'vue';
import { required } from '@vuelidate/validators';
import { useVuelidate } from '@vuelidate/core';
import { useFlashcardDataStore } from '@/stores/flashcard-data.ts';
import { useFlashcardStateStore } from '@/stores/flashcard-state.ts';
import { storeToRefs } from 'pinia';
import { useReviewStateStore } from '@/stores/review-state.ts';

function handleKeydown(event: KeyboardEvent) {
  if (event.key === 'Escape') {
    cancel();
  } else if (event.key === 'Enter' && event.ctrlKey) {
    update();
  }
}

onMounted(() => {
  document.addEventListener('keydown', handleKeydown);
});

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown);
});

const reviewStateStore = useReviewStateStore()
const dataStore = useFlashcardDataStore()
const stateStore = useFlashcardStateStore()
const { currFlashcardSet } = storeToRefs(stateStore)

const flashcardSetName = ref(currFlashcardSet.value?.name)
const flashcardSetTargetLanguage = ref(currFlashcardSet.value?.language.name)
const flashcardRemoveConfirmation = ref(false)
const defaultFlashcardSet: Ref<boolean | undefined> = ref(false)
const flashcardSetHasChanges = computed(() => {
  return currFlashcardSet.value?.name !== flashcardSetName.value
    || currFlashcardSet.value?.default !== defaultFlashcardSet.value
})

watch(flashcardSetName, (_) => {
  flashcardRemoveConfirmation.value = false;
})
watch(defaultFlashcardSet, (_) => {
  flashcardRemoveConfirmation.value = false;
})

const props = defineProps({
  visible: Boolean,
})

stateStore.$subscribe((mutation, state) => {
  flashcardSetName.value = currFlashcardSet.value?.name
  flashcardSetTargetLanguage.value = currFlashcardSet.value?.language.name
})

const emit = defineEmits(['update:visible']);

const validationRules = {
  flashcardSetName: { required },
};

const $v = useVuelidate(validationRules, { flashcardSetName: flashcardSetName });

function cancel() {
  resetState();
  emit('update:visible', false);
}

function remove() {
  if (flashcardRemoveConfirmation.value) {
    removeFlashcardSet();
    resetState();
    emit('update:visible', false);
  } else {
    flashcardRemoveConfirmation.value = true;
  }
}

function update() {
  $v.value.$touch()
  if (!$v.value.$invalid) {
    updateFlashcardSet();
    resetState();
    emit('update:visible', false);
  }
}

function removeFlashcardSet() {
  if (currFlashcardSet.value !== undefined) {
    dataStore.removeFlashcardSet(currFlashcardSet.value)
    reviewStateStore.finishReview()
  }

  const { flashcardSets } = storeToRefs(dataStore)
  stateStore.chooseCurr(flashcardSets)
}

function updateFlashcardSet() {
  if (flashcardSetHasChanges.value) {
    // todo save to DB

    if (flashcardSetName.value !== undefined) {
      stateStore.setName(flashcardSetName.value);
    }
    if (defaultFlashcardSet.value !== undefined) {
      stateStore.setDefault(defaultFlashcardSet.value);
    }
  }
}

function resetState() {
  $v.value.$reset();
  flashcardSetName.value = currFlashcardSet.value?.name;
  flashcardSetTargetLanguage.value = currFlashcardSet.value?.language.name;
  defaultFlashcardSet.value = currFlashcardSet.value?.default;
  flashcardRemoveConfirmation.value = false;
}

</script>

<style scoped>
</style>
