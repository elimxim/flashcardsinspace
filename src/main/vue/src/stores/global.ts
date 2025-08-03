import { defineStore } from 'pinia'
import { useReviewStore } from '@/stores/review.ts'

export interface GlobalState {
  flashcardSetSettingsModalFormOpen: boolean
  flashcardSetCreationModalFormOpen: boolean
  flashcardCreationModalFormOpen: boolean
  flashcardEditModalFormOpen: boolean
  calendarModalFormOpen: boolean
}

export const useGlobalStore = defineStore('global', {
  state: (): GlobalState => {
    return {
      flashcardSetSettingsModalFormOpen: false,
      flashcardSetCreationModalFormOpen: false,
      flashcardCreationModalFormOpen: false,
      flashcardEditModalFormOpen: false,
      calendarModalFormOpen: false,
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
      const reviewStore = useReviewStore()
      reviewStore.setEditFormWasOpened(true)
    },
    toggleCalendarModalForm() {
      this.calendarModalFormOpen = !this.calendarModalFormOpen
    },
    isAnyModalFormOpen(): boolean {
      return this.flashcardSetSettingsModalFormOpen
        || this.flashcardSetCreationModalFormOpen
        || this.flashcardCreationModalFormOpen
        || this.flashcardEditModalFormOpen
        || this.calendarModalFormOpen
    },
  }
})
