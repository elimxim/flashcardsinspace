import type { User } from '@/models/users.ts';
import type { Language } from '@/models/language.ts';

export enum Level {
  FIRST = 1,
  SECOND,
  THIRD,
  FORTH,
  FIFTH,
  SIXTH,
  SEVENTH,
}

export interface Flashcard {
  id: number;
  frontSide: string;
  backSide: string;
  level: Level;
  createdAt: Date | null;
  lastUpdatedAt: Date | null;
}

export interface FlashcardSet {
  id: number;
  name: string;
  targetLanguage: Language;
  flashcardMap: Map<number, Flashcard>;
  createdAt: Date | null;
  lastUpdatedAt: Date | null;
  default: boolean;
  user: User;
}
