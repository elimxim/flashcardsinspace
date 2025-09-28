<template>
  <ModalForm
    :visible="visible"
    :onPressExit="cancel"
    :onPressEnter="update"
    :onPressDelete="remove"
    title="Settings"
  >
    <div class="form-body">
      <div class="modal-vertical-group">
        <input class="modal-input"
               placeholder="Flashcard set name"
               v-model="flashcardSetName"/>
        <span class="modal-error-text" v-if="$v.flashcardSetName.$errors.length">
            Please don't forget to fill this out
        </span>
      </div>
      <div class="modal-vertical-group">
        <div class="modal-horizontal-group">
            <span class="modal-icon modal-globe-icon">
              <font-awesome-icon icon="fa-solid fa-globe"/>
            </span>
          <input class="modal-input" v-model="flashcardSetLanguage" disabled/>
        </div>
      </div>
      <div class="modal-vertical-group">
        <label>
          <input type="checkbox" v-model="flashcardSetDefault"/>
          {{
            flashcardSetDefault
              ? "This flashcard set is set as the default"
              : "Set this flashcard set as the default (it will appear first if you have more than one)"
          }}
        </label>
      </div>
      <!-- todo suspend calendar -->
      <div class="modal-message-group modal-warning"
           v-if="removeConfirmation">
          <span class="modal-icon">
            <font-awesome-icon icon="fa-solid fa-triangle-exclamation"/></span>
        <span class="modal-message-text">
            Are you sure you want to remove '{{ flashcardSet?.name }}'? All progress and flash cards will disappear
          </span>
      </div>
    </div>
    <div class="modal-bottom">
      <button class="modal-button modal-cancel-button"
              @click="cancel">
        Cancel
      </button>
      <button class="modal-button modal-remove-button"
              @click="remove">
        Remove
      </button>
      <button class="modal-button modal-update-button"
              :class="{ 'modal-button-disabled': !stateChanged }"
              :disabled="!stateChanged"
              @click="update">
        Update
      </button>
    </div>
  </ModalForm>
</template>

<script setup lang="ts">
import ModalForm from '@/components/modal/ModalForm.vue'
import {
  computed,
  defineEmits,
  defineProps,
  ref,
  watch
} from 'vue'
import { required } from '@vuelidate/validators'
import { useVuelidate } from '@vuelidate/core'
import { useFlashcardSetsStore } from '@/stores/flashcard-sets-store.ts'
import { useFlashcardSetStore } from '@/stores/flashcard-set-store.ts'
import { storeToRefs } from 'pinia'
import { useReviewStore } from '@/stores/review-store.ts'
import { useGlobalStore } from '@/stores/global-store.ts'

const emit = defineEmits(['update:visible'])

defineProps({
  visible: Boolean,
})

const globalStore = useGlobalStore()
const reviewStore = useReviewStore()
const flashcardSetsStore = useFlashcardSetsStore()
const flashcardSetStore = useFlashcardSetStore()

const { firstFlashcardSet } = storeToRefs(flashcardSetsStore)
const { flashcardSet, language } = storeToRefs(flashcardSetStore)

// state>

const flashcardSetName = ref(flashcardSet.value?.name ?? null)
const flashcardSetLanguage = ref(language.value?.name ?? null)
const flashcardSetDefault = ref(flashcardSet.value?.default ?? null)

const stateChanged = computed(() => {
  return flashcardSet.value?.name !== flashcardSetName.value
    || flashcardSet.value?.default !== flashcardSetDefault.value
})

const removeConfirmation = ref(false)

watch(language, (newValue) => {
  flashcardSetLanguage.value = newValue?.name ?? null
})

watch(flashcardSetName, (_) => {
  removeConfirmation.value = false
})

watch(flashcardSetDefault, (_) => {
  removeConfirmation.value = false
})

flashcardSetStore.$subscribe((_, newState) => {
  flashcardSetName.value = newState.flashcardSet?.name ?? null
  flashcardSetDefault.value = newState.flashcardSet?.default ?? null
})

const validationRules = {
  flashcardSetName: { required },
}

const $v = useVuelidate(validationRules, { flashcardSetName: flashcardSetName })

function resetState() {
  $v.value.$reset()
  flashcardSetName.value = flashcardSet.value?.name ?? null
  flashcardSetLanguage.value = language.value?.name ?? null
  flashcardSetDefault.value = flashcardSet.value?.default ?? null
  removeConfirmation.value = false
}

// <state

function cancel() {
  resetState()
  globalStore.toggleFlashcardSetSettingsModalForm()
  emit('update:visible', false)
}

function remove() {
  if (removeConfirmation.value) {
    removeFlashcardSet()
    resetState()
    globalStore.toggleFlashcardSetSettingsModalForm()
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
      globalStore.toggleFlashcardSetSettingsModalForm()
      emit('update:visible', false)
    }
  }
}

function removeFlashcardSet() {
  if (flashcardSet.value !== null) {
    flashcardSetsStore.removeFlashcardSet(flashcardSet.value).then(() => {
      reviewStore.finishReview()
      if (firstFlashcardSet.value !== null) {
        flashcardSetStore.loadFlashcardsFor(firstFlashcardSet.value)
      } else {
        flashcardSetStore.resetState()
      }
    })
  }
}

function updateFlashcardSet() {
  flashcardSetStore.updateFlashcardSet(flashcardSetName.value, flashcardSetDefault.value)
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
