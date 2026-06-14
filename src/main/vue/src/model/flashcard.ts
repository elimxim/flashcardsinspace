export interface ReviewHistory {
  history: ReviewInfo[]
}

export interface ReviewInfo {
  stage: string
  reviewDate: string // "yyyy-MM-dd"
}

export interface Flashcard {
  id: number
  frontSide?: string | undefined
  backSide?: string | undefined
  stage: string
  timesReviewed: number
  reviewHistory: ReviewHistory
  creationDate: string // "yyyy-MM-dd"
  lastReviewDate?: string | undefined // "yyyy-MM-dd"
  lastUpdatedAt?: Date | undefined // ZonedDateTime
}

export interface FlashcardAudio {
  id: number
  side: string
  mimeType?: string | undefined
  audioSize: number
  flashcardId: number
  uploadedAt?: Date | undefined // ZonedDateTime
}

export interface FlashcardAudioMetadata {
  audioId: number
  flashcardSide: string
  flashcardId: number
}

export interface FlashcardPicture {
  id: number
  side: string
  mimeType: string
  pictureSize: number
  width: number
  height: number
  flashcardId: number
  uploadedAt?: Date | undefined // ZonedDateTime
}

export interface FlashcardPictureMetadata {
  pictureId: number
  flashcardSide: string
  flashcardId: number
  width: number
  height: number
}

export interface FlashcardSet {
  id: number
  name: string
  status: string
  languageId: number
  createdAt: Date // ZonedDateTime
  startedAt?: Date | undefined // ZonedDateTime
  lastUpdatedAt?: Date | undefined // ZonedDateTime
}

export interface FlashcardSetExtra {
  id: number
  flashcardsNumber: number
}

export interface FlashcardContent {
  frontSide?: string | undefined
  backSide? : string | undefined
}
