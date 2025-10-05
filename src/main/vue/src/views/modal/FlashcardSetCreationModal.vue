<template>
  <Modal
    :visible="modalStore.flashcardSetCreationOpen"
    :on-press-exit="cancel"
    :on-press-enter="create"
    :focus-on="nameInput"
    title="New Flashcard Set"
    focusable
  >
    <div class="modal-main-area">
      <div class="modal-main-area--inner">
        <SmartInput
          ref="nameInput"
          v-model="name"
          type="text"
          :invalid="nameInvalid"
          :placeholder="nameNotSet ? 'Name is required' : 'Name'"
        />
        <span v-if="nameMaxLengthInvalid" class="text-error">
          Too long. Maximum 64 characters
        </span>
        <span v-else-if="nameRegexMismatch" class=text-error>
          Please use only letters, numbers, dashes, underscores, and spaces
        </span>
      </div>
      <div class="modal-main-area--inner">
        <AwesomeContainer icon="fa-solid fa-globe" class="awesome-globe">
          <FuzzySelect
            id="language"
            v-model="language"
            :options="languages"
            :option-label="(lang) => lang.name"
            :invalid="languageInvalid"
            :option-placeholder="languageNotSet ? 'Language is required' : 'Language'"
            search-placeholder="Search..."
          />
        </AwesomeContainer>
      </div>
    </div>
    <div class="modal-control-buttons">
      <SmartButton
        class="cancel-button"
        text="Cancel"
        :on-click="cancel"
        auto-blur
      />
      <SmartButton
        class="create-button"
        text="Create"
        :on-click="create"
        :disabled="formInvalid"
        auto-blur
      />
    </div>
  </Modal>
</template>

<script setup lang="ts">
import Modal from '@/components/Modal.vue'
import SmartInput from '@/components/SmartInput.vue'
import SmartButton from '@/components/SmartButton.vue'
import FuzzySelect from '@/components/FuzzySelect.vue'
import AwesomeContainer from '@/components/AwesomeContainer.vue'
import { computed, ref, watch } from 'vue'
import { useFlashcardSetsStore } from '@/stores/flashcard-sets-store.ts'
import { useFlashcardSetStore } from '@/stores/flashcard-set-store.ts'
import { helpers, maxLength, required } from '@vuelidate/validators'
import { useVuelidate } from '@vuelidate/core'
import type { Language } from '@/model/language.ts'
import { storeToRefs } from 'pinia'
import { useLanguageStore } from '@/stores/language-store.ts'
import { useReviewStore } from '@/stores/review-store.ts'
import { useModalStore } from '@/stores/modal-store.ts'
import { createFlashcardSet } from '@/core-logic/flashcard-logic.ts'
import { useChronoStore } from '@/stores/chrono-store.ts'

const modalStore = useModalStore()
const flashcardSetsStore = useFlashcardSetsStore()
const flashcardSetStore = useFlashcardSetStore()
const chronoStore = useChronoStore()
const reviewStore = useReviewStore()
const languageStore = useLanguageStore()

const { languages } = storeToRefs(languageStore)
const { lastFlashcardSet } = storeToRefs(flashcardSetsStore)
const { flashcardSet } = storeToRefs(flashcardSetStore)

const name = ref('')
const nameInput = ref<HTMLElement>()
const language = ref<Language>()

const validationRules = {
  name: {
    required,
    maxLength: maxLength(64),
    regex: helpers.regex(/^[A-Za-z0-9 _-]+$/),
  },
  language: { required },
}

const $v = useVuelidate(validationRules, {
  name: name,
  language: language,
})

const formInvalid = computed(() => $v.value.$errors.length > 0)
const nameInvalid = computed(() =>
  $v.value.name.$errors.length > 0
)
const nameNotSet = computed(() =>
  $v.value.name.$errors.find(v => v.$validator === 'required') !== undefined
)
const nameMaxLengthInvalid = computed(() =>
  $v.value.name.$errors.find(v => v.$validator === 'maxLength') !== undefined
)
const nameRegexMismatch = computed(() =>
  $v.value.name.$errors.find(v => v.$validator === 'regex') !== undefined
)
const languageInvalid = computed(() =>
  $v.value.language.$errors.length > 0
)
const languageNotSet = computed(() =>
  $v.value.language.$errors.find(v => v.$validator === 'required') !== undefined
)

function cancel() {
  resetState()
  modalStore.toggleFlashcardSetCreation()
}

function create() {
  $v.value.$touch()
  if (!formInvalid.value) {
    createNewFlashcardSet()
    reviewStore.finishReview()
    resetState()
    modalStore.toggleFlashcardSetCreation()
  }
}

function resetState() {
  name.value = ''
  language.value = undefined
  $v.value.$reset()
}

function createNewFlashcardSet() {
  if (name.value && language.value) {
    const newSet = createFlashcardSet(name.value, language.value)
    flashcardSetsStore.addFlashcardSet(newSet).then(() => {
      if (lastFlashcardSet.value !== null) {
        flashcardSetStore.loadFlashcardsFor(lastFlashcardSet.value).then(() => {
          if (flashcardSet.value !== null) {
            chronoStore.loadChronodays(flashcardSet.value)
          }
        })
      }
    })
  }
}

watch(name, () => {
  if (nameInvalid.value) {
    $v.value.name.$reset()
  }
})

watch(language, () => {
  if (languageInvalid.value) {
    $v.value.language.$reset()
  }
})

</script>

<style scoped>
.modal-main-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: start;
  gap: 10px;
}

.modal-main-area--inner {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.modal-control-buttons {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  gap: 10px;
}
</style>
