export interface User {
  id: number;
  name: string;
  registeredAt: Date;
}

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
  frontText: string;
  backText: string;
  level: Level;
  createdAt: Date;
  lastUpdatedAt: Date;
}

export interface FlashcardSet {
  id: number;
  targetLanguage: Language;
  flashcards: Flashcard[];
  createdAt: Date;
  lastUpdatedAt: Date;
  user: User;
}
