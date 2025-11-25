<template>
  <Modal
    :visible="toggleStore.flashcardSetSettingsOpen"
    :on-press-exit="cancel"
    :on-press-enter="update"
    :on-press-delete="remove"
    :focus-on="nameInput"
    title="Flashcard Set Settings"
  >
    <div class="modal-main-area">
      <div class="modal-main-area--inner">
        <SmartInput
          ref="nameInput"
          v-model="newName"
          type="text"
          :invalid="curNameInvalid"
          placeholder="Name"
        />
        <ErrorText
          :errors="[
            {
              when: curNameMaxLengthInvalid,
              text: 'Too long. Maximum 64 characters'
            },
            {
              when: curNameRegexMismatch,
              text: 'Please use only letters, numbers, dashes, underscores, and spaces'
            },
          ]"
        />
      </div>
      <div class="modal-main-area--inner">
        <AwesomeContainer icon="fa-solid fa-globe" class="awesome-globe">
          <FuzzySelect
            id="language"
            v-model="newLanguage"
            :options="languages"
            :option-label="(lang) => lang.name"
            :invalid="curLanguageInvalid"
            option-placeholder="Language"
            search-placeholder="Search..."
          />
        </AwesomeContainer>
      </div>
      <div class="modal-main-area--checkbox">
        <SmartCheckbox
          v-model="newSuspended"
          label="Active"
          checked-label="Suspended (vacation mode 🌴)"
        />
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
        class="dangerous-button"
        text="Remove"
        :hold-time="4"
        :on-click="remove"
        auto-blur
      />
      <SmartButton
        class="calm-button"
        text="Save"
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
import SmartCheckbox from '@/components/SmartCheckbox.vue'
import FuzzySelect from '@/components/FuzzySelect.vue'
import AwesomeContainer from '@/components/AwesomeContainer.vue'
import SpaceToast from '@/components/SpaceToast.vue'
import ErrorText from '@/components/ErrorText.vue'
import { computed, ref, watch } from 'vue'
import { helpers, maxLength, required } from '@vuelidate/validators'
import { useVuelidate } from '@vuelidate/core'
import { useFlashcardSetStore } from '@/stores/flashcard-set-store.ts'
import { useFlashcardStore } from '@/stores/flashcard-store.ts'
import { storeToRefs } from 'pinia'
import { useToggleStore } from '@/stores/toggle-store.ts'
import { Language } from '@/model/language.ts'
import { useLanguageStore } from '@/stores/language-store.ts'
import {
  sendFlashcardSetRemovalRequest,
  sendFlashcardSetSuspendRequest,
  sendFlashcardSetUpdateRequest,
} from '@/api/api-client.ts'
import { useSpaceToaster } from '@/stores/toast-store.ts'
import { reloadFlashcardRelatedStores } from '@/shared/stores.ts'
import { copyFlashcardSet, flashcardSetStatuses } from '@/core-logic/flashcard-logic.ts'
import { useChronoStore } from '@/stores/chrono-store.ts'

const toggleStore = useToggleStore()
const toaster = useSpaceToaster()
const languageStore = useLanguageStore()
const flashcardSetStore = useFlashcardSetStore()
const flashcardStore = useFlashcardStore()
const chronoStore = useChronoStore()

const { languages } = storeToRefs(languageStore)
const { flashcardSet, language } = storeToRefs(flashcardStore)

const nameInput = ref<HTMLElement>()
const newName = ref<string | undefined>(flashcardSet.value?.name)
const newLanguage = ref<Language | undefined>(language.value)
const newSuspended = ref<boolean>(flashcardSet.value?.status === flashcardSetStatuses.SUSPENDED)
const wasSuspended = computed(() =>
  flashcardSet.value?.status === flashcardSetStatuses.SUSPENDED
)
const becameSuspended = computed(() =>
  !wasSuspended.value && newSuspended.value
)

const stateChanged = computed(() => {
  return flashcardSet.value?.name !== newName.value
    || language.value !== newLanguage.value
    || wasSuspended.value !== newSuspended.value
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
  name: newName,
  language: newLanguage,
})

const formInvalid = computed(() => $v.value.$errors.length > 0)
const curNameInvalid = computed(() =>
  $v.value.name.$errors.length > 0
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

function cancel() {
  toggleStore.toggleFlashcardSetSettings()
  resetState()
}

async function remove() {
  const removed = await removeFlashcardSet()
  if (removed) {
    await reloadFlashcardRelatedStores(true)
      .then(() => {
        toggleStore.toggleFlashcardSetSettings()
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
        toggleStore.toggleFlashcardSetSettings()
        resetState()
      }
    }
  }
}

function resetState() {
  $v.value.$reset()
  newName.value = flashcardSet.value?.name
  newLanguage.value = language.value
  newSuspended.value = flashcardSet.value?.status === flashcardSetStatuses.SUSPENDED
}

async function removeFlashcardSet(): Promise<boolean> {
  if (!flashcardSet.value) return false

  const removedSet = flashcardSet.value
  return await sendFlashcardSetRemovalRequest(removedSet.id)
    .then(() => {
      flashcardSetStore.removeSet(removedSet)
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

  const updatedSet = copyFlashcardSet(flashcardSet.value)
  updatedSet.name = newName.value ?? updatedSet.name
  updatedSet.languageId = newLanguage.value?.id ?? updatedSet.languageId
  updatedSet.status = newSuspended.value ? flashcardSetStatuses.SUSPENDED : flashcardSetStatuses.ACTIVE

  if (becameSuspended.value) {
    return await sendFlashcardSetSuspendRequest(updatedSet.id, updatedSet)
      .then((response) => {
        flashcardSetStore.updateSet(response.data.flashcardSet)
        flashcardStore.changeSet(response.data.flashcardSet)
        chronoStore.loadState(
          response.data.chronodays,
          response.data.currDay,
          0,
        )
        return true
      }).catch((error) => {
        console.error(`Failed to suspend flashcard set ${updatedSet.id}`, error)
        toaster.bakeError(`Couldn't suspend flashcard set`, error.response?.data)
        return false
      })
  } else {
    return await sendFlashcardSetUpdateRequest(updatedSet.id, updatedSet)
      .then((response) => {
        flashcardSetStore.updateSet(response.data)
        flashcardStore.changeSet(response.data)
        return true
      })
      .catch((error) => {
        console.error(`Failed to update flashcard set ${updatedSet.id}`, error)
        toaster.bakeError(`Couldn't update flashcard set`, error.response?.data)
        return false
      })
  }
}

watch(flashcardSet, (newVal) => {
  newName.value = newVal?.name
  newSuspended.value = newVal?.status === flashcardSetStatuses.SUSPENDED
})

watch(language, (newVal) => {
  newLanguage.value = newVal
})

watch(newName, () => {
  if (curNameInvalid.value) {
    $v.value.name.$reset()
  }
})

watch(newLanguage, () => {
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

.modal-main-area--checkbox {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding-left: 0.5rem;
}

.modal-control-buttons {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  gap: 10px;
}
</style>
