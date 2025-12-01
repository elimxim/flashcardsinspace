import { defineStore } from 'pinia'

export interface ToggleState {
  flashcardSetSettingsOpen: boolean
  flashcardSetCreationOpen: boolean
  flashcardCreationOpen: boolean
  flashcardEditOpen: boolean
  calendarOpen: boolean
  quizOpen: boolean
}

export const useToggleStore = defineStore('toggle', {
  state: (): ToggleState => {
    return {
      flashcardSetSettingsOpen: false,
      flashcardSetCreationOpen: false,
      flashcardCreationOpen: false,
      flashcardEditOpen: false,
      calendarOpen: false,
      quizOpen: false,
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
    toggleQuiz() {
      this.quizOpen = !this.quizOpen
    },
    isAnyModalOpen(): boolean {
      return this.flashcardSetSettingsOpen
        || this.flashcardSetCreationOpen
        || this.flashcardCreationOpen
        || this.flashcardEditOpen
        || this.calendarOpen
        || this.quizOpen
    },
  }
})
