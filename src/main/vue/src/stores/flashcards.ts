import {defineStore} from 'pinia'
import {type FlashcardSet, Level, type User} from "@/stores/model.ts";

export const useFlashcardStore = defineStore('flashcards', () => {
  const user: User = {
    id: 1,
    name: "Emma Rose",
    registeredAt: new Date(),
  };

  const flashcardSets: FlashcardSet[] = [
    {
      id: 1,
      user: user,
      targetLanguage: {
        code: "bs",
        name: "Bosnian",
      },
      flashcards: [
        {
          id: 1,
          frontText: "Prdnuti",
          backText: "To fart",
          level: Level.FIRST,
          createdAt: new Date(),
          lastUpdatedAt: new Date(),
        },
        {
          id: 1,
          frontText: "Kakati",
          backText: "To poop",
          level: Level.FIRST,
          createdAt: new Date(),
          lastUpdatedAt: new Date(),
        },
      ],
      createdAt: new Date(),
      lastUpdatedAt: new Date(),
    },
    {
      id: 2,
      user: user,
      targetLanguage: {
        code: "ru",
        name: "Russian",
      },
      flashcards: [
        {
          id: 1,
          frontText: "Пукать",
          backText: "To fart",
          level: Level.FIRST,
          createdAt: new Date(),
          lastUpdatedAt: new Date(),
        },
        {
          id: 1,
          frontText: "Какать",
          backText: "To poop",
          level: Level.FIRST,
          createdAt: new Date(),
          lastUpdatedAt: new Date(),
        },
      ],
      createdAt: new Date(),
      lastUpdatedAt: new Date(),
    }
  ];

  const loadFlashcardSets = () => { return flashcardSets }

  return { loadFlashcardSets }
})
