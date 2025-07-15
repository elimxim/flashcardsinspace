import type { User } from '@/models/users.ts';

export interface Language {
  code: string;
  name: string;
}

export enum Level {
  FIRST = 1,
  SECOND = 2,
  THIRD = 3,
  FORTH = 4,
  FIFTH = 5,
  SIXTH = 6,
  SEVENTH = 7,
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
  flashcards: Flashcard[];
  createdAt: Date | null;
  lastUpdatedAt: Date | null;
  user: User;
}
