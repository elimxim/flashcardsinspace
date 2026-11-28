package com.github.elimxim.flashcardsinspace.service

import com.github.elimxim.flashcardsinspace.entity.*
import com.github.elimxim.flashcardsinspace.entity.repository.FlashcardAudioRepository
import com.github.elimxim.flashcardsinspace.entity.repository.FlashcardPictureRepository
import com.github.elimxim.flashcardsinspace.entity.repository.FlashcardPictureThumbnailRepository
import com.github.elimxim.flashcardsinspace.entity.repository.FlashcardRepository
import com.github.elimxim.flashcardsinspace.service.validation.RequestValidator
import com.github.elimxim.flashcardsinspace.util.trimOneLine
import com.github.elimxim.flashcardsinspace.util.withLock
import com.github.elimxim.flashcardsinspace.web.dto.*
import com.github.elimxim.flashcardsinspace.web.exception.ApiErrorCode
import com.github.elimxim.flashcardsinspace.web.exception.HttpConflictException
import com.github.elimxim.flashcardsinspace.web.exception.HttpNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZonedDateTime

private val log = LoggerFactory.getLogger(FlashcardService::class.java)

@Service
class FlashcardService(
    private val flashcardSetService: FlashcardSetService,
    private val flashcardRepository: FlashcardRepository,
    private val flashcardAudioRepository: FlashcardAudioRepository,
    private val flashcardPictureRepository: FlashcardPictureRepository,
    private val flashcardPictureThumbnailRepository: FlashcardPictureThumbnailRepository,
    private val requestValidator: RequestValidator,
    private val flashcardAddService: FlashcardAddService,
    private val reviewSessionService: ReviewSessionService,
) {
    @Transactional(readOnly = true)
    fun getAll(user: User, setId: Long, pageable: Pageable): Page<FlashcardDto> {
        log.info("Retrieving flashcards from set $setId")
        user.checkVerified()
        val flashcardSet = flashcardSetService.getEntity(setId)
        flashcardSetService.verifyUserHasAccess(user, flashcardSet)
        return flashcardRepository.findAllByFlashcardSetId(setId, pageable).map { it.toDto() }
    }

    fun add(user: User, setId: Long, request: FlashcardCreationRequest): FlashcardCreationResponse {
        log.info("Adding a new flashcard to set $setId")
        return addInternal(user, setId) {
            flashcardAddService.add(setId, request)
        }
    }

    fun addBulk(user: User, setId: Long, request: FlashcardBulkCreationRequest): FlashcardCreationResponse {
        log.info("Adding flashcards in bulk to set $setId")
        return addInternal(user, setId) {
            flashcardAddService.add(setId, request)
        }
    }

    private fun addInternal(
        user: User,
        setId: Long,
        action: () -> FlashcardCreationResponse
    ): FlashcardCreationResponse {
        user.checkVerified()
        val flashcardSet = flashcardSetService.getEntity(setId)
        flashcardSetService.verifyUserHasAccess(user, flashcardSet)
        return if (flashcardSet.isNotStarted()) {
            withLock(flashcardSet) { action() }
        } else {
            action()
        }
    }

    @Transactional
    fun update(
        user: User,
        setId: Long,
        id: Long,
        request: FlashcardUpdateRequest,
        sessionId: Long?
    ): FlashcardUpdateResponse {
        log.info("Updating flashcard $id in set $setId")
        user.checkVerified()
        val flashcard = getEntity(id)
        flashcardSetService.verifyUserHasAccess(user, flashcard.flashcardSet)
        verifyUserOperation(user, setId, flashcard)
        val validRequest = requestValidator.validate(request)
        if (isReviewOperation(flashcard, validRequest)) {
            flashcardSetService.verifyNotSuspended(flashcard.flashcardSet)
        }
        val updatedFlashcard = saveMerged(flashcard, validRequest)

        val sessionRequest = request.sessionRequest
        val sessionDto = if (sessionId != null && sessionRequest != null) {
            reviewSessionService.updateReviewSession(user, setId, sessionId, sessionRequest)
        } else null

        return FlashcardUpdateResponse(updatedFlashcard.toDto(), sessionDto)
    }

    @Transactional
    fun updateBulk(user: User, setId: Long, request: FlashcardBulkUpdateRequest): FlashcardBulkUpdateResponse {
        log.info("Updating ${request.requests?.size} flashcards in bulk in set $setId")
        user.checkVerified()
        val flashcardSet = flashcardSetService.getEntity(setId)
        flashcardSetService.verifyUserHasAccess(user, flashcardSet)
        val validRequest = requestValidator.validate(request)
        val flashcards = getEntities(user, setId, validRequest.requests.map { it.id })
        val updates = validRequest.requests.map { flashcards.getValue(it.id) to it.flashcard }
        if (updates.any { (flashcard, update) -> isReviewOperation(flashcard, update) }) {
            flashcardSetService.verifyNotSuspended(flashcardSet)
        }
        val updatedFlashcards = updates.map { (flashcard, update) -> saveMerged(flashcard, update) }
        return FlashcardBulkUpdateResponse(updatedFlashcards.map { it.toDto() })
    }

    private fun saveMerged(flashcard: Flashcard, request: ValidFlashcardUpdateRequest): Flashcard {
        return if (mergeFlashcard(flashcard, request)) {
            flashcard.lastUpdatedAt = ZonedDateTime.now()
            flashcardRepository.save(flashcard)
        } else {
            flashcard
        }
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
        user.checkVerified()
        val flashcard = getEntity(id)
        verifyUserOperation(user, setId, flashcard)
        flashcardSetService.verifyUserHasAccess(user, flashcard.flashcardSet)
        removeEntities(listOf(flashcard))
    }

    @Transactional
    fun removeBulk(user: User, setId: Long, request: FlashcardBulkRemovalRequest) {
        log.info("Removing ${request.ids?.size} flashcards in bulk from set $setId")
        user.checkVerified()
        val flashcardSet = flashcardSetService.getEntity(setId)
        flashcardSetService.verifyUserHasAccess(user, flashcardSet)
        val validRequest = requestValidator.validate(request)
        removeEntities(getEntities(user, setId, validRequest.ids).values)
    }

    private fun removeEntities(flashcards: Collection<Flashcard>) {
        val ids = flashcards.map { it.id }
        flashcardAudioRepository.deleteByFlashcardIdIn(ids)
        flashcardPictureThumbnailRepository.deleteByFlashcardIdIn(ids)
        flashcardPictureRepository.deleteByFlashcardIdIn(ids)
        flashcardRepository.deleteAll(flashcards)
    }

    @Transactional(readOnly = true)
    fun getEntity(id: Long): Flashcard =
        flashcardRepository.findById(id).orElseThrow {
            HttpNotFoundException(ApiErrorCode.FLA404, "Flashcard with id $id not found")
        }

    private fun getEntities(user: User, setId: Long, ids: List<Long>): Map<Long, Flashcard> {
        val flashcards = flashcardRepository.findAllById(ids).associateBy { it.id }
        val missingIds = ids.filter { it !in flashcards }
        if (missingIds.isNotEmpty()) {
            throw HttpNotFoundException(ApiErrorCode.FLA404, "Flashcards with ids $missingIds not found")
        }
        flashcards.values.forEach { this.verifyUserOperation(user, setId, it) }
        return flashcards
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
