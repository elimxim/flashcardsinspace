import { defineStore } from 'pinia'
import { useReviewStateStore } from '@/stores/review-state'
import { type GlobalState } from '@/model/state.ts'

export const useGlobalStateStore = defineStore('global-state', {
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
      const reviewStateStore = useReviewStateStore()
      reviewStateStore.setEditFormWasOpened(true)
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
