<template>
  <Modal
    :visible="visible"
    :onPressExit="cancel"
    :onPressEnter="create"
    :focusOn="nameInput"
    title="New Flashcard Set"
    focusable
  >
    <div class="modal-main-area">
      <div class="modal-main-area--inner">
        <SmartInput
          v-model="name"
          ref="nameInput"
          type="text"
          :invalid="nameInvalid"
          :placeholder="nameNotSet ? 'Name is required' : 'Name'"
        />
        <span class="text-error" v-if="nameMaxLengthInvalid">
          Too long. Maximum 64 characters
        </span>
        <span class=text-error v-else-if="nameRegexMismatch">
          Please use only letters, numbers, dashes, underscores, and spaces
        </span>
      </div>
      <div class="modal-main-area--inner">
        <AwesomeContainer icon="fa-solid fa-globe" class="awesome-globe">
          <FuzzySelect
            :options="languages"
            v-model="language"
            id="language"
            :optionLabel="(lang) => lang.name"
            :invalid="languageInvalid"
            :optionPlaceholder="languageNotSet ? 'Language is required' : 'Language'"
            searchPlaceholder="Search..."
          />
        </AwesomeContainer>
      </div>
    </div>
    <div class="modal-control-buttons">
      <SmartButton
        class="cancel-button"
        text="Cancel"
        :onClick="cancel"
        autoBlur
      />
      <SmartButton
        class="create-button"
        text="Create"
        :onClick="create"
        :disabled="formInvalid"
        autoBlur
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
import { useGlobalStore } from '@/stores/global-store.ts'
import { createFlashcardSet } from '@/core-logic/flashcard-logic.ts'
import { useChronoStore } from '@/stores/chrono-store.ts'

const visible = defineModel<boolean>('visible', { default: false })

const globalStore = useGlobalStore()
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
  globalStore.toggleFlashcardSetCreationModalForm()
}

function create() {
  $v.value.$touch()
  if (!formInvalid.value) {
    createNewFlashcardSet()
    reviewStore.finishReview()
    resetState()
    globalStore.toggleFlashcardSetCreationModalForm()
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
