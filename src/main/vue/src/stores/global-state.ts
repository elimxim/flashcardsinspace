import { defineStore } from 'pinia';
import { useReviewStateStore } from '@/stores/review-state';

export const useGlobalStateStore = defineStore('global-state', {
  state: () => {
    return {
      flashcardSetSettingsModalFormOpen: false,
      flashcardSetCreationModalFormOpen: false,
      flashcardCreationModalFormOpen: false,
      flashcardEditModalFormOpen: false,
    }
  },
  getters: {
    isFlashcardSetSettingsModalFormOpen() {
      return this.flashcardSetSettingsModalFormOpen
    },
    isFlashcardSetCreationModalFormOpen() {
      return this.flashcardSetCreationModalFormOpen
    },
    isFlashcardCreationModalFormOpen() {
      return this.flashcardCreationModalFormOpen
    },
    isFlashcardEditModalFormOpen() {
      return this.flashcardEditModalFormOpen
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
      if (this.flashcardEditModalFormOpen) {
        const reviewStateStore = useReviewStateStore()
        reviewStateStore.setEditFormWasOpened(true)
      }
    },
    isAnyModalOpen() {
      return this.flashcardSetCreationModalFormOpen
        || this.flashcardSetSettingsModalFormOpen
        || this.flashcardCreationModalFormOpen
        || this.flashcardSetSettingsModalFormOpen
    },
  }
})
