package com.github.elimxim.flashcardsinspace.service

import com.github.elimxim.flashcardsinspace.entity.Flashcard
import com.github.elimxim.flashcardsinspace.entity.FlashcardSet
import com.github.elimxim.flashcardsinspace.entity.isNotStarted
import com.github.elimxim.flashcardsinspace.entity.repository.FlashcardRepository
import com.github.elimxim.flashcardsinspace.service.validation.RequestValidator
import com.github.elimxim.flashcardsinspace.web.dto.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FlashcardAddService(
    private val flashcardSetService: FlashcardSetService,
    private val flashcardSetInitService: FlashcardSetInitService,
    private val flashcardRepository: FlashcardRepository,
    private val requestValidator: RequestValidator,
) {
    @Transactional
    fun add(setId: Long, request: FlashcardCreationRequest): FlashcardCreationResponse {
        val flashcardSet = flashcardSetService.getEntity(setId)
        val validRequest = requestValidator.validate(request)
        val flashcard = createFlashcard(flashcardSet, validRequest)
        return if (flashcardSet.isNotStarted()) {
            val result = flashcardSetInitService.init(flashcardSet, listOf(flashcard))
            FlashcardCreationResponse(
                initialized = true,
                flashcardSet = result.flashcardSet,
                flashcard = result.flashcards.first(),
                currDay = result.schedule.first(),
                chronodays = result.schedule,
            )
        } else {
            val savedFlashcard = flashcardRepository.save(flashcard)
            FlashcardCreationResponse(
                initialized = false,
                flashcard = savedFlashcard.toDto(),
            )
        }
    }

    @Transactional
    fun add(setId: Long, request: FlashcardBulkCreationRequest): FlashcardCreationResponse {
        val flashcardSet = flashcardSetService.getEntity(setId)
        val validBulkRequest = requestValidator.validate(request)
        val flashcards = validBulkRequest.requests.map { req ->
            createFlashcard(flashcardSet, req)
        }
        return if (flashcardSet.isNotStarted()) {
            val result = flashcardSetInitService.init(flashcardSet, flashcards)
            FlashcardCreationResponse(
                initialized = true,
                flashcardSet = result.flashcardSet,
                flashcards = result.flashcards,
                currDay = result.schedule.first(),
                chronodays = result.schedule,
            )
        } else {
            val savedFlashcards = flashcardRepository.saveAll(flashcards)
            FlashcardCreationResponse(
                initialized = false,
                flashcards = savedFlashcards.map { it.toDto() }
            )
        }
    }

}

private fun createFlashcard(flashcardSet: FlashcardSet, req: ValidFlashcardCreationRequest): Flashcard {
    return Flashcard(
        frontSide = req.frontSide,
        backSide = req.backSide,
        stage = req.stage,
        timesReviewed = 0,
        creationDate = req.creationDate,
        flashcardSet = flashcardSet,
    )
}
