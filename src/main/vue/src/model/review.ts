export interface ReviewSession {
  id: number
  type: string
  flashcardIds: number[]
  elapsedTime: number
  startedAt: Date // ZonedDateTime
  finished: boolean | undefined
  finishedAt?: Date | undefined // ZonedDateTime
  lastUpdatedAt?: Date | undefined // ZonedDateTime
  metadata?: QuizMetadata // use discriminated union if there is more than one type
}

export interface QuizMetadata {
  round: number
  currRoundFlashcardIds: number[]
  nextRoundFlashcardIds: number[]
  overallCorrectCount: number
  overallTotalCount: number
}
