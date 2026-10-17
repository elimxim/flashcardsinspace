import { ReviewSession } from '@/model/review.ts'
import { Flashcard, FlashcardSet } from '@/model/flashcard.ts'
import { sendLatestUncompletedQuizGetRequest } from '@/api/api-client.ts'

export function readQuizSession(session: ReviewSession, flashcards: Flashcard[]) {
  const stages = session.metadata?.stages ?? []
  const round = session.metadata?.round ?? 1
  const overallCorrect = session.metadata?.overallCorrectCount ?? 0
  const overallIncorrect = session.metadata?.overallIncorrectCount ?? 0
  const elapsedTotal = session.metadata?.elapsedTotalTime ?? 0

  const reviewedFlashcardIdSet = new Set(session.flashcardIds ?? [])
  const currRoundFlashcardIdSet = new Set(session.metadata?.currRoundFlashcardIds ?? [])
  const nextRoundFlashcardIdSet = new Set(session.metadata?.nextRoundFlashcardIds ?? [])

  const currRound = flashcards.filter((f) => currRoundFlashcardIdSet.has(f.id))
  const nextRound = currRound.filter((f) => nextRoundFlashcardIdSet.has(f.id))
  const forReview = currRound.filter((f) => !reviewedFlashcardIdSet.has(f.id))

  return {
    stages,
    round,
    overallCorrect,
    overallIncorrect,
    elapsedTotal,
    currRound, // all flashcards for this session / quiz round
    nextRound, // flashcards for the next round
    forReview, // not yet reviewed flashcards
  }
}

export async function getActualLatestUncompletedQuizSession(
  flashcardSet: FlashcardSet | undefined,
  flashcards: Flashcard[],
): Promise<ReviewSession | undefined> {
  if (!flashcardSet || flashcards.length === 0) return

  const session = await getLatestUncompletedQuizSession(flashcardSet)
  if (session) {
    const { forReview, nextRound } = readQuizSession(session, flashcards)
    return forReview.length > 0 || nextRound.length > 0 ? session : undefined
  }
}

async function getLatestUncompletedQuizSession(
  flashcardSet: FlashcardSet,
): Promise<ReviewSession | undefined> {
  return await sendLatestUncompletedQuizGetRequest(flashcardSet.id)
    .then((response) => {
      if (response.status === 204) {
        return undefined
      } else {
        return response.data
      }
    })
    .catch((error) => {
      console.error(`Failed to fetch latest quiz session`, error.response?.data)
      return undefined
    })
}
