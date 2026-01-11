package com.github.elimxim.flashcardsinspace.service

import com.github.elimxim.flashcardsinspace.entity.Flashcard
import com.github.elimxim.flashcardsinspace.entity.ReviewHistory
import com.github.elimxim.flashcardsinspace.entity.ReviewInfo
import com.github.elimxim.flashcardsinspace.entity.User
import com.github.elimxim.flashcardsinspace.entity.repository.FlashcardRepository
import com.github.elimxim.flashcardsinspace.service.validation.RequestValidator
import com.github.elimxim.flashcardsinspace.util.trimOneLine
import com.github.elimxim.flashcardsinspace.web.dto.*
import com.github.elimxim.flashcardsinspace.web.exception.ApiErrorCode
import com.github.elimxim.flashcardsinspace.web.exception.HttpConflictException
import com.github.elimxim.flashcardsinspace.web.exception.HttpNotFoundException
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
    @Transactional(readOnly = true)
    fun getAll(user: User, setId: Long): List<FlashcardDto> {
        log.info("Retrieving flashcards from set $setId")
        val flashcardSet = flashcardSetService.getEntity(setId)
        flashcardSetService.verifyUserHasAccess(user, flashcardSet)
        return flashcardRepository.findAllByFlashcardSetId(setId).map { it.toDto() }
    }

    @Transactional
    fun add(user: User, setId: Long, request: FlashcardCreationRequest): FlashcardDto {
        log.info("Adding a new flashcard to set $setId")
        val flashcardSet = flashcardSetService.getEntity(setId)
        flashcardSetService.verifyUserHasAccess(user, flashcardSet)
        val validRequest = requestValidator.validate(request)
        val flashcard = Flashcard(
            frontSide = validRequest.frontSide,
            backSide = validRequest.backSide,
            stage = validRequest.stage,
            timesReviewed = 0,
            creationDate = validRequest.creationDate,
            flashcardSet = flashcardSet,
        )

        return flashcardRepository.save(flashcard).toDto()
    }

    @Transactional
    fun update(user: User, setId: Long, id: Long, request: FlashcardUpdateRequest): FlashcardDto {
        log.info("Updating flashcard $id in set $setId")
        val flashcard = getEntity(id)
        flashcardSetService.verifyUserHasAccess(user, flashcard.flashcardSet)
        verifyUserOperation(user, setId, flashcard)
        val validRequest = requestValidator.validate(request)

        if (isReviewOperation(flashcard, validRequest)) {
            flashcardSetService.verifyNotSuspended(flashcard.flashcardSet)
        }

        val updatedFlashcard = if (mergeFlashcard(flashcard, validRequest)) {
            flashcard.lastUpdatedAt = ZonedDateTime.now()
            flashcardRepository.save(flashcard)
        } else {
            flashcard
        }

        return updatedFlashcard.toDto()
    }

    private fun isReviewOperation(flashcard: Flashcard, request: ValidFlashcardUpdateRequest): Boolean {
        return isStageChanged(flashcard, request)
                || isTimesReviewedChanged(flashcard, request)
                || isReviewHistoryChanged(flashcard, request)
                || isLastReviewDateChanged(flashcard, request)
    }

    private fun mergeFlashcard(flashcard: Flashcard, request: ValidFlashcardUpdateRequest): Boolean {
        var changed = false
        if (isFrontSideChanged(flashcard, request)) {
            request.frontSide?.let { flashcard.frontSide = it }
            changed = true
        }
        if (isBackSideChanged(flashcard, request)) {
            request.backSide?.let { flashcard.backSide = it }
            changed = true
        }
        if (isStageChanged(flashcard, request)) {
            request.stage?.let { flashcard.stage = it }
            changed = true
        }
        if (isTimesReviewedChanged(flashcard, request)) {
            request.timesReviewed?.let { flashcard.timesReviewed = it }
            changed = true
        }
        if (isReviewHistoryChanged(flashcard, request)) {
            request.reviewHistory?.let {
                flashcard.reviewHistory = ReviewHistory(
                    history = it.history.map { info ->
                        ReviewInfo(
                            stage = info.stage,
                            reviewDate = info.reviewDate
                        )
                    }.toMutableList()
                )
            }
            changed = true
        }
        if (isLastReviewDateChanged(flashcard, request)) {
            flashcard.lastReviewDate = request.lastReviewDate
            changed = true
        }

        return changed
    }

    @Transactional
    fun remove(user: User, setId: Long, id: Long) {
        log.info("Removing flashcard $id from set $setId")
        val flashcard = getEntity(id)
        verifyUserOperation(user, setId, flashcard)
        flashcardSetService.verifyUserHasAccess(user, flashcard.flashcardSet)
        flashcardRepository.delete(flashcard)
    }

    @Transactional(readOnly = true)
    fun getEntity(id: Long): Flashcard =
        flashcardRepository.findById(id).orElseThrow {
            HttpNotFoundException(ApiErrorCode.FLA404, "Flashcard with id $id not found")
        }

    @Transactional(readOnly = true)
    fun verifyUserOperation(user: User, setId: Long, flashcard: Flashcard) {
        if (flashcard.flashcardSet.id != setId) {
            throw HttpConflictException(
                ApiErrorCode.FSF409,
                """
                User ${user.id} requested flashcard ${flashcard.id}
                doesn't belong to the requested set $setId
                """.trimOneLine()
            )
        }
    }

}

private fun isFrontSideChanged(flashcard: Flashcard, request: ValidFlashcardUpdateRequest): Boolean {
    return request.frontSide != null && request.frontSide != flashcard.frontSide
}

private fun isBackSideChanged(flashcard: Flashcard, request: ValidFlashcardUpdateRequest): Boolean {
    return request.backSide != null && request.backSide != flashcard.backSide
}

private fun isStageChanged(flashcard: Flashcard, request: ValidFlashcardUpdateRequest): Boolean {
    return request.stage != null && request.stage != flashcard.stage
}

private fun isTimesReviewedChanged(flashcard: Flashcard, request: ValidFlashcardUpdateRequest): Boolean {
    return request.timesReviewed != null && request.timesReviewed != flashcard.timesReviewed
}

private fun isReviewHistoryChanged(flashcard: Flashcard, request: ValidFlashcardUpdateRequest): Boolean {
    return request.reviewHistory != null && request.reviewHistory.history.size != flashcard.reviewHistory.history.size
}

private fun isLastReviewDateChanged(flashcard: Flashcard, request: ValidFlashcardUpdateRequest): Boolean {
    return request.lastReviewDate != null &&
            (flashcard.lastReviewDate == null || !request.lastReviewDate.isEqual(flashcard.lastReviewDate))
}
