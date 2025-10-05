import { defineStore } from 'pinia'

export interface ModalState {
  flashcardSetSettingsOpen: boolean
  flashcardSetCreationOpen: boolean
  flashcardCreationOpen: boolean
  flashcardEditOpen: boolean
  calendarOpen: boolean
}

export const useModalStore = defineStore('modal', {
  state: (): ModalState => {
    return {
      flashcardSetSettingsOpen: false,
      flashcardSetCreationOpen: false,
      flashcardCreationOpen: false,
      flashcardEditOpen: false,
      calendarOpen: false,
    }
  },
  actions: {
    toggleFlashcardSetSettings() {
      this.flashcardSetSettingsOpen = !this.flashcardSetSettingsOpen
    },
    toggleFlashcardSetCreation() {
      this.flashcardSetCreationOpen = !this.flashcardSetCreationOpen
    },
    toggleFlashcardCreation() {
      this.flashcardCreationOpen = !this.flashcardCreationOpen
    },
    toggleFlashcardEdit() {
      this.flashcardEditOpen = !this.flashcardEditOpen
    },
    toggleCalendar() {
      this.calendarOpen = !this.calendarOpen
    },
    isAnyModalOpen(): boolean {
      return this.flashcardSetSettingsOpen
        || this.flashcardSetCreationOpen
        || this.flashcardCreationOpen
        || this.flashcardEditOpen
        || this.calendarOpen
    },
  }
})
