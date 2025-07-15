<template>
  <div v-if="visible" class="modal-overlay" role="dialog" tabindex="-1">
    <div class="modal-window">
      <h2 class="modal-title">Settings</h2>
      <div class="modal-body">
        <div class="modal-form-group">
          <input id="flashcardSetName" v-model="flashcardSetName"
                 placeholder="Flashcard set name"
                 class="modal-input"/>
          <span v-if="$v.flashcardSetName.$errors.length" class="modal-error-message">
            Please don't forget to fill this out
          </span>
        </div>
        <div class="modal-form-group">
          <label>
            <input type="checkbox" v-model="defaultFlashcardSet"/>
            {{
              defaultFlashcardSet
                ? "This flashcard set is set as the default"
                : "Set this flashcard set as the default (it will appear first if you have more than one)"
            }}
          </label>
        </div>
        <div v-if="flashcardRemoveConfirmation" class="modal-remove-confirmation-container">
          <span class="modal-remove-confirmation-sign"><font-awesome-icon
            icon="fa-solid fa-triangle-exclamation"/></span>
          <span class="modal-remove-confirmation">
            Are you sure you want to remove '{{ currFlashcardSet?.name }}' flashcard set? You will lose all flashcards.
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
import { computed, defineEmits, defineProps, onMounted, onUnmounted, type Ref, ref } from 'vue';
import { required } from '@vuelidate/validators';
import { useVuelidate } from '@vuelidate/core';
import { useFlashcardDataStore } from '@/stores/flashcard-data.ts';
import { useFlashcardStateStore } from '@/stores/flashcard-state.ts';
import { storeToRefs } from 'pinia';

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

const dataStore = useFlashcardDataStore()
const stateStore = useFlashcardStateStore()
const { currFlashcardSet } = storeToRefs(stateStore)

const flashcardSetName = ref(currFlashcardSet.value?.name)
const flashcardRemoveConfirmation = ref(false)
const defaultFlashcardSet: Ref<boolean | undefined> = ref(false)
const flashcardSetHasChanges = computed(() => {
  return currFlashcardSet.value?.name !== flashcardSetName.value
    || currFlashcardSet.value?.default !== defaultFlashcardSet.value
})

const props = defineProps({
  visible: Boolean,
})

stateStore.$subscribe((mutation, state) => {
  flashcardSetName.value = currFlashcardSet.value?.name
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

function remove() {
  if (flashcardRemoveConfirmation.value) {
    removeFlashcardSet();
    cleanState();
    emit('update:visible', false);
  } else {
    flashcardRemoveConfirmation.value = true;
  }
}

function update() {
  $v.value.$touch()
  if (!$v.value.$invalid) {
    updateFlashcardSet();
    cleanState();
    emit('update:visible', false);
  }
}

function removeFlashcardSet() {
  if (currFlashcardSet.value !== undefined) {
    dataStore.removeFlashcardSet(currFlashcardSet.value)
  }

  const { flashcardSets } = storeToRefs(dataStore)
  stateStore.chooseCurrFlashcardSet(flashcardSets)
}

function updateFlashcardSet() {
  if (flashcardSetHasChanges.value) {
    // todo save to DB

    if (flashcardSetName.value !== undefined) {
      stateStore.setFlashcardSetName(flashcardSetName.value);
    }
    if (defaultFlashcardSet.value !== undefined) {
      stateStore.setFlashcardSetDefault(defaultFlashcardSet.value);
    }
  }
}

function cleanState() {
  $v.value.$reset();
  flashcardSetName.value = currFlashcardSet.value?.name;
  defaultFlashcardSet.value = currFlashcardSet.value?.default;
  flashcardRemoveConfirmation.value = false;
}

</script>

<style scoped>
</style>
