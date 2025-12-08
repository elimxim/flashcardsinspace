export interface ReviewSession {
  id: number
  type: string
  flashcardIds: number[]
  elapsedTime: number
  startedAt: Date
  finished: boolean | undefined
  finishedAt?: Date | undefined
  lastUpdatedAt?: Date | undefined
  metadata?: QuizMetadata // use discriminated union if there is more than one type
}

export interface QuizMetadata {
  round: number
  nextRoundFlashcardIds: number[]
  overallCorrectCount: number
  overallTotalCount: number
}
