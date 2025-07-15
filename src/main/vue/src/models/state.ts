import type { FlashcardSet } from '@/models/flashcards.ts';

export interface FlashcardState {
  currFlashcardSet: FlashcardSet | undefined,
}
