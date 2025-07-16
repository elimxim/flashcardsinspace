import type { FlashcardSet } from '@/models/flashcard.ts';

export interface FlashcardState {
  currFlashcardSet: FlashcardSet | undefined,
}
