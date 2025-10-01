package com.github.elimxim.flashcardsinspace.service

import com.github.elimxim.flashcardsinspace.entity.Flashcard
import com.github.elimxim.flashcardsinspace.entity.ReviewHistory
import com.github.elimxim.flashcardsinspace.entity.ReviewInfo
import com.github.elimxim.flashcardsinspace.entity.User
import com.github.elimxim.flashcardsinspace.entity.repository.FlashcardRepository
import com.github.elimxim.flashcardsinspace.service.validation.RequestValidator
import com.github.elimxim.flashcardsinspace.web.dto.*
import com.github.elimxim.flashcardsinspace.web.exception.FlashcardNotFoundException
import com.github.elimxim.flashcardsinspace.web.exception.UnmatchedFlashcardSetIdException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZonedDateTime

private val log = LoggerFactory.getLogger(FlashcardService::class.java)

@Service
class FlashcardService(
    private val flashcardSetService: FlashcardSetService,
    private val flashcardRepository: FlashcardRepository,
    private val requestValidator: RequestValidator,
) {
    @Transactional
    fun get(user: User, setId: Long): List<FlashcardDto> {
        log.info("User ${user.id}: retrieving flashcards from set $setId")
        flashcardSetService.verifyUserHasAccess(user, setId)
        return flashcardSetService.getEntity(setId)
            .flashcards.map { it.toDto() }
    }

    @Transactional
    fun add(user: User, setId: Long, request: FlashcardCreationRequest): FlashcardDto {
        log.info("User ${user.id}: adding a new flashcard to set $setId")
        flashcardSetService.verifyUserHasAccess(user, setId)
        return add(setId, requestValidator.validate(request))
    }

    @Transactional
    fun add(setId: Long, request: ValidFlashcardCreationRequest): FlashcardDto {
        val flashcardSet = flashcardSetService.getEntity(setId)
        val flashcard = Flashcard(
            frontSide = request.frontSide,
            backSide = request.backSide,
            stage = request.stage,
            timesReviewed = 0,
            creationDate = request.creationDate,
            flashcardSet = flashcardSet,
        )

        flashcardSet.flashcards.add(flashcard)
        return flashcardRepository.save(flashcard).toDto()
    }

    @Transactional
    fun update(user: User, setId: Long, id: Long, request: FlashcardUpdateRequest): FlashcardDto {
        log.info("User ${user.id}: updating flashcard $id in set $setId")
        flashcardSetService.verifyUserHasAccess(user, setId)
        verifyUserOperation(user, setId, id)
        return update(setId, id, requestValidator.validate(request))
    }

    @Transactional
    fun update(setId: Long, id: Long, request: ValidFlashcardUpdateRequest): FlashcardDto {
        flashcardSetService.getEntity(setId)
        val flashcard = getEntity(id)
        if (mergeFlashcard(flashcard, request)) {
            flashcard.lastUpdatedAt = ZonedDateTime.now()
            return flashcardRepository.save(flashcard).toDto()
        } else {
            return flashcard.toDto()
        }
    }

    private fun mergeFlashcard(flashcard: Flashcard, request: ValidFlashcardUpdateRequest): Boolean {
        var changed = false
        if (request.frontSide != null && request.frontSide != flashcard.frontSide) {
            flashcard.frontSide = request.frontSide
            changed = true
        }
        if (request.backSide != null && request.backSide != flashcard.backSide) {
            flashcard.backSide = request.backSide
            changed = true
        }
        if (request.stage != null && request.stage != flashcard.stage) {
            flashcard.stage = request.stage
            changed = true
        }
        if (request.timesReviewed != null && request.timesReviewed != flashcard.timesReviewed) {
            flashcard.timesReviewed = request.timesReviewed
            changed = true
        }
        if (request.reviewHistory != null && request.reviewHistory.history.size != flashcard.reviewHistory.history.size) {
            flashcard.reviewHistory = ReviewHistory(
                history = request.reviewHistory.history.map {
                    ReviewInfo(
                        stage = it.stage,
                        reviewDate = it.reviewDate
                    )
                }.toMutableList()
            )
            changed = true
        }
        if (request.lastReviewDate != null &&
            (flashcard.lastReviewDate == null || !request.lastReviewDate.isEqual(flashcard.lastReviewDate))
        ) {
            flashcard.lastReviewDate = request.lastReviewDate
            changed = true
        }

        return changed
    }

    @Transactional
    fun remove(user: User, setId: Long, id: Long) {
        log.info("User ${user.id}: removing flashcard $id from set $setId")
        flashcardSetService.verifyUserHasAccess(user, setId)
        verifyUserOperation(user, setId, id)
        val flashcardSet = flashcardSetService.getEntity(setId)
        val flashcard = getEntity(id)
        flashcardSet.flashcards.remove(flashcard)
        flashcardRepository.delete(flashcard)
    }

    @Transactional
    fun getEntity(id: Long): Flashcard =
        flashcardRepository.findById(id).orElseThrow {
            FlashcardNotFoundException("Flashcard with id $id not found")
        }

    @Transactional
    fun verifyUserOperation(user: User, setId: Long, id: Long) {
        if (getEntity(id).flashcardSet.id != setId) {
            throw UnmatchedFlashcardSetIdException(
                """
                    User ${user.id} requested flashcard $id 
                    doesn't belong to the requested set $setId
                    """.trimIndent()
            )
        }
    }

}
