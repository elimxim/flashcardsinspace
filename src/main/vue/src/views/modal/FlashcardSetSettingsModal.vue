<template>
  <Modal
    :visible="modalStore.flashcardSetSettingsOpen"
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
  <SpaceToast/>
</template>

<script setup lang="ts">
import Modal from '@/components/Modal.vue'
import SmartInput from '@/components/SmartInput.vue'
import SmartButton from '@/components/SmartButton.vue'
import FuzzySelect from '@/components/FuzzySelect.vue'
import AwesomeContainer from '@/components/AwesomeContainer.vue'
import SpaceToast from '@/components/SpaceToast.vue'
import { computed, ref, watch } from 'vue'
import { helpers, maxLength, required } from '@vuelidate/validators'
import { useVuelidate } from '@vuelidate/core'
import { useFlashcardSetsStore } from '@/stores/flashcard-sets-store.ts'
import { useFlashcardSetStore } from '@/stores/flashcard-set-store.ts'
import { storeToRefs } from 'pinia'
import { useModalStore } from '@/stores/modal-store.ts'
import { Language } from '@/model/language.ts'
import { useLanguageStore } from '@/stores/language-store.ts'
import { FlashcardSet } from '@/model/flashcard.ts'
import {
  sendFlashcardSetRemovalRequest,
  sendFlashcardSetUpdateRequest,
} from '@/api/api-client.ts'
import { useSpaceToaster } from '@/stores/toast-store.ts'
import { reloadFlashcardSetStore } from '@/core-logic/flashcard-logic.ts';

const modalStore = useModalStore()
const toaster = useSpaceToaster()
const languageStore = useLanguageStore()
const flashcardSetsStore = useFlashcardSetsStore()
const flashcardSetStore = useFlashcardSetStore()

const { languages } = storeToRefs(languageStore)
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
  modalStore.toggleFlashcardSetSettings()
  resetState()
}

async function remove() {
  const removed = await removeFlashcardSet()
  if (removed) {
    await reloadFlashcardSetStore()
      .then(() => {
        modalStore.toggleFlashcardSetSettings()
        resetState()
      })
  }
}

async function update() {
  if (stateChanged.value) {
    $v.value.$touch()
    if (!formInvalid.value) {
      const updated = await updateFlashcardSet()
      if (updated) {
        modalStore.toggleFlashcardSetSettings()
        resetState()
      }
    }
  }
}

function resetState() {
  $v.value.$reset()
  curName.value = flashcardSet.value?.name
  curLanguage.value = language.value
  curFirst.value = flashcardSet.value?.default
}

async function removeFlashcardSet(): Promise<boolean> {
  if (!flashcardSet.value) return false

  const removedSet = flashcardSet.value
  return await sendFlashcardSetRemovalRequest(removedSet.id)
    .then(() => {
      flashcardSetsStore.removeSet(removedSet)
      return true
    })
    .catch((error) => {
      console.error(`Failed to remove flashcard set ${removedSet.id}`, error)
      toaster.bakeError(`Couldn't remove flashcard set`, error.response?.data)
      return false
    })
}

async function updateFlashcardSet(): Promise<boolean> {
  if (!flashcardSet.value) return false

  const updatedSet: FlashcardSet = JSON.parse(JSON.stringify(flashcardSet.value));
  updatedSet.name = curName.value ?? updatedSet.name
  updatedSet.languageId = curLanguage.value?.id ?? updatedSet.languageId
  updatedSet.default = curFirst.value ?? updatedSet.default

  return await sendFlashcardSetUpdateRequest(updatedSet.id, updatedSet)
    .then((response) => {
      flashcardSetStore.changeFlashcardSet(response.data)
      return true
    })
    .catch((error) => {
      console.error(`Failed to update flashcard set ${updatedSet.id}`, error)
      toaster.bakeError(`Couldn't update flashcard set`, error.response?.data)
      return false
    })
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
