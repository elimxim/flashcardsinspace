package com.github.elimxim.flashcardsinspace.service

import com.github.elimxim.flashcardsinspace.entity.Flashcard
import com.github.elimxim.flashcardsinspace.entity.FlashcardStage
import com.github.elimxim.flashcardsinspace.entity.ReviewHistory
import com.github.elimxim.flashcardsinspace.entity.ReviewInfo
import com.github.elimxim.flashcardsinspace.entity.repository.FlashcardRepository
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardDto
import com.github.elimxim.flashcardsinspace.web.dto.toDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.ZonedDateTime

@Service
class FlashcardService(
    private val flashcardSetService: FlashcardSetService,
    private val flashcardRepository: FlashcardRepository,
) {
    @Transactional
    fun getFlashcards(flashcardSetId: Long): List<FlashcardDto> {
        // todo check for existence
        return flashcardSetService.getFlashcardSetInt(flashcardSetId).flashcards.map { it.toDto() }
    }

    fun getFlashcard(id: Long): Flashcard {
        return flashcardRepository.getReferenceById(id) // fixme
    }

    @Transactional
    fun addFlashcard(flashcardSetId: Long, dto: FlashcardDto): FlashcardDto {
        // todo validate
        val flashcardSet = flashcardSetService.getFlashcardSetInt(flashcardSetId)
        // todo check for existence
        val flashcard = Flashcard(
            frontSide = dto.frontSide,
            backSide = dto.backSide,
            stage = FlashcardStage.valueOf(dto.stage),
            timesReviewed = dto.reviewCount,
            creationDate = LocalDate.parse(dto.createdAt),
            flashcardSet = flashcardSet,
        )

        flashcardSet.flashcards.add(flashcard)
        return flashcardRepository.save(flashcard).toDto()
    }

    @Transactional
    fun updateFlashcard(flashcardSetId: Long, id: Long, dto: FlashcardDto): FlashcardDto {
        // todo validate
        // todo check for existence
        val flashcardSet = flashcardSetService.getFlashcardSetInt(flashcardSetId)
        // todo check for existence
        val flashcard = getFlashcard(id)
        // todo move merge to a separate method
        var changed = false
        if (flashcard.frontSide != dto.frontSide) {
            flashcard.frontSide = dto.frontSide
            changed = true
        }
        if (flashcard.backSide != dto.backSide) {
            flashcard.backSide = dto.backSide
            changed = true
        }
        val stage = FlashcardStage.valueOf(dto.stage)
        if (flashcard.stage != stage) {
            flashcard.stage = stage
            changed = true
        }
        if (flashcard.timesReviewed != dto.reviewCount) {
            flashcard.timesReviewed = dto.reviewCount
            changed = true
        }
        if (flashcard.reviewHistory.history.size != dto.reviewHistory.history.size) {
            flashcard.reviewHistory = ReviewHistory(
                history = dto.reviewHistory.history.map {
                    ReviewInfo(
                        stage = FlashcardStage.valueOf(it.stage),
                        reviewDate = LocalDate.parse(it.reviewedAt)
                    )
                }.toMutableList()
            )
            changed = true
        }
        if (flashcard.lastReviewDate?.toString() != dto.reviewedAt) {
            flashcard.lastReviewDate = dto.reviewedAt?.let { LocalDate.parse(it) }
        }

        if (changed) {
            flashcard.lastUpdatedAt = ZonedDateTime.now()
            return flashcardRepository.save(flashcard).toDto()
        } else {
            // todo log
            return dto
        }
    }

    @Transactional
    fun removeFlashcard(flashcardSetId: Long, id: Long) {
        // check for existence
        val flashcardSet = flashcardSetService.getFlashcardSetInt(flashcardSetId)
        // check for existence
        val flashcard = getFlashcard(id)
        flashcardSet.flashcards.remove(flashcard)
        flashcardRepository.delete(flashcard)
    }

}