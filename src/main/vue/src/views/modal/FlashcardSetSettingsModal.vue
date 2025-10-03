<template>
  <Modal
    :visible="visible"
    :on-press-exit="cancel"
    :on-press-enter="update"
    :on-press-delete="remove"
    :focus-on="curNameInput"
    title="Flashcard Set Settings"
  >
    <div class="modal-main-area">
      <div class="modal-main-area--inner">
        <SmartInput
          ref="curNameInput"
          v-model="curName"
          type="text"
          :invalid="curNameInvalid"
          :placeholder="curNameNotSet ? 'Name is required' : 'Name'"
        />
        <span v-if="curNameMaxLengthInvalid" class="text-error">
          Too long. Maximum 64 characters
        </span>
        <span v-else-if="curNameRegexMismatch" class=text-error>
          Please use only letters, numbers, dashes, underscores, and spaces
        </span>
      </div>
      <div class="modal-main-area--inner">
        <AwesomeContainer icon="fa-solid fa-globe" class="awesome-globe">
          <FuzzySelect
            id="language"
            v-model="curLanguage"
            :options="languages"
            :option-label="(lang) => lang.name"
            :invalid="curLanguageInvalid"
            :option-placeholder="curLanguageNotSet ? 'Language is required' : 'Language'"
            search-placeholder="Search..."
          />
        </AwesomeContainer>
      </div>
      <div class="modal-main-area--inner">
        <label>
          <input v-model="curFirst" type="checkbox"/>
          {{
            curFirst
              ? "This flashcard set is set as the default"
              : "Set this flashcard set as the default"
          }}
        </label>
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
        class="remove-button"
        text="Remove"
        :hold-time="4"
        :on-click="remove"
        auto-blur
      />
      <SmartButton
        class="update-button"
        text="Update"
        :on-click="update"
        :disabled="!stateChanged || formInvalid"
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
import { helpers, maxLength, required } from '@vuelidate/validators'
import { useVuelidate } from '@vuelidate/core'
import { useFlashcardSetsStore } from '@/stores/flashcard-sets-store.ts'
import { useFlashcardSetStore } from '@/stores/flashcard-set-store.ts'
import { storeToRefs } from 'pinia'
import { useGlobalStore } from '@/stores/global-store.ts'
import { Language } from '@/model/language.ts'
import { useLanguageStore } from '@/stores/language-store.ts'

const visible = defineModel<boolean>('visible', { default: false })

const globalStore = useGlobalStore()
const languageStore = useLanguageStore()
const flashcardSetsStore = useFlashcardSetsStore()
const flashcardSetStore = useFlashcardSetStore()

const { languages } = storeToRefs(languageStore)
const { firstFlashcardSet } = storeToRefs(flashcardSetsStore)
const { flashcardSet, language } = storeToRefs(flashcardSetStore)

const curName = ref<string | undefined>(flashcardSet.value?.name)
const curNameInput = ref<HTMLElement>()
const curLanguage = ref<Language | undefined>(language.value)
const curFirst = ref<boolean | undefined>(flashcardSet.value?.default)

const stateChanged = computed(() => {
  return flashcardSet.value?.name !== curName.value
    || flashcardSet.value?.default !== curFirst.value
    || language.value !== curLanguage.value
})

const validationRules = {
  name: {
    required,
    maxLength: maxLength(64),
    regex: helpers.regex(/^[A-Za-z0-9 _-]+$/),
  },
  language: { required },
}

const $v = useVuelidate(validationRules, {
  name: curName,
  language: curLanguage,
})

const formInvalid = computed(() => $v.value.$errors.length > 0)
const curNameInvalid = computed(() =>
  $v.value.name.$errors.length > 0
)
const curNameNotSet = computed(() =>
  $v.value.name.$errors.find(v => v.$validator === 'required') !== undefined
)
const curNameMaxLengthInvalid = computed(() =>
  $v.value.name.$errors.find(v => v.$validator === 'maxLength') !== undefined
)
const curNameRegexMismatch = computed(() =>
  $v.value.name.$errors.find(v => v.$validator === 'regex') !== undefined
)
const curLanguageInvalid = computed(() =>
  $v.value.language.$errors.length > 0
)
const curLanguageNotSet = computed(() =>
  $v.value.language.$errors.find(v => v.$validator === 'required') !== undefined
)

function cancel() {
  resetState()
  globalStore.toggleFlashcardSetSettingsModalForm()
}

async function remove() {
  await removeFlashcardSet()
  resetState()
  globalStore.toggleFlashcardSetSettingsModalForm()
}

function update() {
  if (stateChanged.value) {
    $v.value.$touch()
    if (!formInvalid.value) {
      updateFlashcardSet()
      resetState()
      globalStore.toggleFlashcardSetSettingsModalForm()
    }
  }
}

function resetState() {
  $v.value.$reset()
  curName.value = flashcardSet.value?.name
  curLanguage.value = language.value
  curFirst.value = flashcardSet.value?.default
}

async function removeFlashcardSet() {
  if (flashcardSet.value !== null) {
    flashcardSetsStore.removeFlashcardSet(flashcardSet.value).then(() => {
      if (firstFlashcardSet.value !== null) {
        flashcardSetStore.loadFlashcardsFor(firstFlashcardSet.value)
      } else {
        flashcardSetStore.resetState()
      }
    })
  }
}

function updateFlashcardSet() {
  flashcardSetStore.updateFlashcardSet(curName.value, curLanguage.value, curFirst.value)
}

watch(flashcardSet, (newVal) => {
  curName.value = newVal?.name
  curFirst.value = newVal?.default
})

watch(language, (newVal) => {
  curLanguage.value = newVal
})

watch(curName, () => {
  if (curNameInvalid.value) {
    $v.value.name.$reset()
  }
})

watch(curLanguage, () => {
  if (curLanguageInvalid.value) {
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
