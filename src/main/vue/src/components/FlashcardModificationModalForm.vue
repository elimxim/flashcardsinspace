<template>
  <div v-if="visible" class="modal-overlay" role="dialog" tabindex="-1">
    <div class="modal-window">
      <h2 class="modal-title">{{ editMode ? 'Edit flashcard' : 'New flashcard' }}</h2>
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
        <div v-if="flashcardRemoveConfirmation"
             class="modal-message-container modal-warning-message-container">
          <span class="modal-icon">
            <font-awesome-icon icon="fa-solid fa-triangle-exclamation"/></span>
          <span class="modal-message-text">
            Are you sure you want to remove this flashcard?
          </span>
        </div>
      </div>
      <div class="modal-buttons">
        <button class="modal-button modal-cancel-button" @click="cancel">Cancel</button>
        <button v-if="editMode" class="modal-button modal-remove-button" @click="remove">Remove
        </button>
        <button v-if="editMode" class="modal-button modal-create-button" @click="update"
                :class="{ 'modal-button-disabled': !flashcardHasChanges }"
                :disabled="!flashcardHasChanges">
          Update
        </button>
        <button v-if="!editMode" class="modal-button modal-create-button" @click="create">Create
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import '@/assets/modal.css'
import { computed, defineEmits, defineProps, type PropType, type Ref, ref, watch } from 'vue';
import { onMounted, onUnmounted } from 'vue';
import { useVuelidate } from '@vuelidate/core';
import { required } from '@vuelidate/validators';
import { useFlashcardDataStore } from '@/stores/flashcard-data.ts';
import { useFlashcardStateStore } from '@/stores/flashcard-state.ts';
import { type Flashcard, Level } from '@/models/flashcard.ts';
import { storeToRefs } from 'pinia';
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
  document.addEventListener('keydown', handleKeydown)
});

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown)
});

const stateStore = useFlashcardStateStore()

const props = defineProps({
  visible: Boolean,
  editMode: Boolean,
  removed: Boolean,
  flashcard: {
    type: Object as PropType<Flashcard | undefined>,
    required: false,
  },
})

const emit = defineEmits([
  'update:visible',
  'update:removed',
])

const frontSide = ref(props.flashcard?.frontSide ?? '')
const backSide = ref(props.flashcard?.backSide ?? '')

const validationRules = {
  frontSide: { required },
  backSide: { required },
};

const $v = useVuelidate(validationRules, { frontSide: frontSide, backSide })

const globalStateStore = useGlobalStateStore()

function cancel() {
  resetState()
  if (props.editMode) {
    globalStateStore.toggleFlashcardEditModalForm()
  } else {
    globalStateStore.toggleFlashcardCreationModalForm()
  }
  emit('update:visible', false)
  emit('update:removed', false)
}

const flashcardRemoveConfirmation = ref(false)

function remove() {
  if (flashcardRemoveConfirmation.value) {
    if (props.flashcard === undefined) {
      console.error("can't remove an undefined flashcard")
      return
    }
    stateStore.removeFlashcard(props.flashcard?.id)
    resetState();
    if (props.editMode) {
      globalStateStore.toggleFlashcardEditModalForm()
    } else {
      globalStateStore.toggleFlashcardCreationModalForm()
    }
    emit('update:visible', false);
    emit('update:removed', true)
  } else {
    flashcardRemoveConfirmation.value = true;
  }
}

function create() {
  $v.value.$touch()
  if (!$v.value.$invalid) {
    createNewFlashcard()
    resetState()
    if (props.editMode) {
      globalStateStore.toggleFlashcardEditModalForm()
    } else {
      globalStateStore.toggleFlashcardCreationModalForm()
    }
    emit('update:visible', false)
  }
}

function update() {
  if (flashcardHasChanges.value) {
    $v.value.$touch()
    if (!$v.value.$invalid) {
      if (props.flashcard === undefined) {
        console.error("can't update an undefined flashcard")
        return
      }
      const flashcard = props.flashcard
      flashcard.frontSide = frontSide.value
      flashcard.backSide = backSide.value
      flashcard.lastUpdatedAt = new Date().toISOString()
      stateStore.updateFlashcard(flashcard)
      resetState()
      if (props.editMode) {
        globalStateStore.toggleFlashcardEditModalForm()
      } else {
        globalStateStore.toggleFlashcardCreationModalForm()
      }
      emit('update:visible', false)
    }
  }
}

function createNewFlashcard() {
  // todo save to DB
  const newFlashcard: Flashcard = {
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

  stateStore.addFlashcard(newFlashcard)
}

function resetState() {
  frontSide.value = props.flashcard?.frontSide ?? ''
  backSide.value = props.flashcard?.backSide ?? ''
  flashcardRemoveConfirmation.value = false
  $v.value.$reset()
}

watch(frontSide, (_) => {
  flashcardRemoveConfirmation.value = false;
})
watch(backSide, (_) => {
  flashcardRemoveConfirmation.value = false;
})
watch(() => props.flashcard, (newValue, oldValue) => {
  frontSide.value = newValue?.frontSide ?? ''
  backSide.value = newValue?.backSide ?? ''
})

const flashcardHasChanges = computed(() => {
  return props.flashcard?.frontSide !== frontSide.value
    || props.flashcard?.backSide !== backSide.value
})

</script>

<style scoped>
</style>
