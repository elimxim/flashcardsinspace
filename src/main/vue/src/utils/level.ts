import { Level } from '@/models/flashcard.ts';

export function levelNumbers(): number[] {
  return Object.values(Level).filter(v => typeof v === 'number') as number[]
}

export function nextLevel(currLevel: Level): Level {
  if (currLevel === Level.SEVENTH) {
    throw new Error('NEXT LEVEL IS NOT IMPLEMENTED YET')
  }
  const levels = levelNumbers()
  const currIdx = levels.indexOf(currLevel)
  return levels[currIdx + 1]
}
