<template>
  <Modal
    :visible="toggleStore.flashcardSetCreationOpen"
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
          placeholder="Name"
        />
        <ErrorText
          :errors="[
            {
              when: nameMaxLengthInvalid,
              text: 'Too long. Maximum 64 characters'
            },
            {
              when: nameRegexMismatch,
              text: 'Please use only letters, numbers, dashes, underscores, and spaces'
            },
          ]"
        />
      </div>
      <div class="modal-main-area--inner">
        <AwesomeContainer icon="fa-solid fa-globe" class="awesome-globe">
          <FuzzySelect
            id="language"
            v-model="language"
            :options="languages"
            :option-label="(lang) => lang.name"
            :invalid="languageInvalid"
            option-placeholder="Language"
            search-placeholder="Search..."
          />
        </AwesomeContainer>
      </div>
    </div>
    <div class="modal-control-buttons">
      <SmartButton
        class="off-button"
        text="Cancel"
        :on-click="cancel"
        auto-blur
      />
      <SmartButton
        class="safe-button"
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
import ErrorText from '@/components/ErrorText.vue'
import { computed, ref, watch } from 'vue'
import { useFlashcardSetStore } from '@/stores/flashcard-set-store.ts'
import { useFlashcardStore } from '@/stores/flashcard-store.ts'
import { helpers, maxLength, required } from '@vuelidate/validators'
import { useVuelidate } from '@vuelidate/core'
import type { Language } from '@/model/language.ts'
import { storeToRefs } from 'pinia'
import { useLanguageStore } from '@/stores/language-store.ts'
import { useToggleStore } from '@/stores/toggle-store.ts'
import { createFlashcardSet } from '@/core-logic/flashcard-logic.ts'
import { useChronoStore } from '@/stores/chrono-store.ts'
import {
  sendChronoSyncRequest,
  sendFlashcardSetCreationRequest
} from '@/api/api-client.ts'
import { useSpaceToaster } from '@/stores/toast-store.ts'

const toggleStore = useToggleStore()
const toaster = useSpaceToaster()
const flashcardSetStore = useFlashcardSetStore()
const flashcardStore = useFlashcardStore()
const chronoStore = useChronoStore()
const languageStore = useLanguageStore()

const { languages } = storeToRefs(languageStore)

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
const nameMaxLengthInvalid = computed(() =>
  $v.value.name.$errors.find(v => v.$validator === 'maxLength') !== undefined
)
const nameRegexMismatch = computed(() =>
  $v.value.name.$errors.find(v => v.$validator === 'regex') !== undefined
)
const languageInvalid = computed(() =>
  $v.value.language.$errors.length > 0
)

function cancel() {
  toggleStore.toggleFlashcardSetCreation()
  resetState()
}

async function create() {
  $v.value.$touch()
  if (!formInvalid.value) {
    const created = await createNewFlashcardSet()
    if (created) {
      toggleStore.toggleFlashcardSetCreation()
      resetState()
    }
  }
}

async function createNewFlashcardSet(): Promise<boolean> {
  if (!name.value || !language.value) return false

  const newSet = createFlashcardSet(name.value, language.value)
  return await sendFlashcardSetCreationRequest(newSet)
    .then((response) => {
      flashcardSetStore.addSet(response.data)
      flashcardStore.loadState(response.data, [])
      return sendChronoSyncRequest(response.data.id)
    })
    .then((response) => {
      chronoStore.loadState(
        response.data.chronodays,
        response.data.currDay,
        response.data.dayStreak,
      )
      return true
    })
    .catch((error) => {
      console.error(`Failed to create flashcard set`, error.response?.data)
      toaster.bakeError(`Couldn't create a flashcard set`, error.response?.data)
      return false
    })
}

function resetState() {
  name.value = ''
  language.value = undefined
  $v.value.$reset()
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
