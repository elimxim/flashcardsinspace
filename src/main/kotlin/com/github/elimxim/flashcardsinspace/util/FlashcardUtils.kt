package com.github.elimxim.flashcardsinspace.util

import com.github.elimxim.flashcardsinspace.entity.Flashcard
import com.github.elimxim.flashcardsinspace.web.dto.ValidFlashcardUpdateRequest

fun isFrontSideChanged(flashcard: Flashcard, request: ValidFlashcardUpdateRequest): Boolean {
    return request.frontSide != null && request.frontSide != flashcard.frontSide
}

fun isBackSideChanged(flashcard: Flashcard, request: ValidFlashcardUpdateRequest): Boolean {
    return request.backSide != null && request.backSide != flashcard.backSide
}

fun isStageChanged(flashcard: Flashcard, request: ValidFlashcardUpdateRequest): Boolean {
    return request.stage != null && request.stage != flashcard.stage
}

fun isTimesReviewedChanged(flashcard: Flashcard, request: ValidFlashcardUpdateRequest): Boolean {
    return request.timesReviewed != null && request.timesReviewed != flashcard.timesReviewed
}

fun isReviewHistoryChanged(flashcard: Flashcard, request: ValidFlashcardUpdateRequest): Boolean {
    return request.reviewHistory != null && request.reviewHistory.history.size != flashcard.reviewHistory.history.size
}

fun isLastReviewDateChanged(flashcard: Flashcard, request: ValidFlashcardUpdateRequest): Boolean {
    return request.lastReviewDate != null &&
            (flashcard.lastReviewDate == null || !request.lastReviewDate.isEqual(flashcard.lastReviewDate))
}

