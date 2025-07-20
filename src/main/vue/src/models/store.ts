import type { Flashcard, FlashcardSet } from '@/models/flashcard.ts'

export interface FlashcardData {
  flashcardSets: FlashcardSet[],
}

export interface FlashcardSetState {
  flashcardSet: FlashcardSet | null,
}

export interface ReviewState {
  started: boolean,
  isFrontSide: boolean,
  reviewQueue: Flashcard[],
  currFlashcard: Flashcard | null,
  editFormWasOpened: boolean,
}

export interface GlobalState {
  flashcardSetSettingsModalFormOpen: boolean,
  flashcardSetCreationModalFormOpen: boolean,
  flashcardCreationModalFormOpen: boolean,
  flashcardEditModalFormOpen: boolean,
}

