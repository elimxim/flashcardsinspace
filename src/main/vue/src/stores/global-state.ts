import { defineStore } from 'pinia'
import { useReviewStateStore } from '@/stores/review-state'
import { type GlobalState } from '@/models/store.ts'

export const useGlobalStateStore = defineStore('global-state', {
  state: (): GlobalState => {
    return {
      flashcardSetSettingsModalFormOpen: false,
      flashcardSetCreationModalFormOpen: false,
      flashcardCreationModalFormOpen: false,
      flashcardEditModalFormOpen: false,
    }
  },
  actions: {
    toggleFlashcardSetSettingsModalForm() {
      this.flashcardSetSettingsModalFormOpen = !this.flashcardSetSettingsModalFormOpen
    },
    toggleFlashcardSetCreationModalForm() {
      this.flashcardSetCreationModalFormOpen = !this.flashcardSetCreationModalFormOpen
    },
    toggleFlashcardCreationModalForm() {
      this.flashcardCreationModalFormOpen = !this.flashcardCreationModalFormOpen
    },
    toggleFlashcardEditModalForm() {
      this.flashcardEditModalFormOpen = !this.flashcardEditModalFormOpen
      const reviewStateStore = useReviewStateStore()
      reviewStateStore.setEditFormWasOpened(true)
    },
    isAnyModalFormOpen() {
      return this.flashcardSetSettingsModalFormOpen
        || this.flashcardSetCreationModalFormOpen
        || this.flashcardCreationModalFormOpen
        || this.flashcardEditModalFormOpen
    },
  }
})
