package com.github.elimxim.flashcardsinspace.service

import com.github.elimxim.flashcardsinspace.entity.*
import com.github.elimxim.flashcardsinspace.entity.repository.ReviewSessionRepository
import com.github.elimxim.flashcardsinspace.service.validation.RequestValidator
import com.github.elimxim.flashcardsinspace.util.addQuizMetadata
import com.github.elimxim.flashcardsinspace.util.updateQuizMetadata
import com.github.elimxim.flashcardsinspace.web.dto.*
import com.github.elimxim.flashcardsinspace.web.exception.ApiErrorCode
import com.github.elimxim.flashcardsinspace.web.exception.HttpBadRequestException
import com.github.elimxim.flashcardsinspace.web.exception.HttpNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZonedDateTime

private val log = LoggerFactory.getLogger(ReviewSessionService::class.java)

@Service
class ReviewSessionService(
    private val requestValidator: RequestValidator,
    private val flashcardSetService: FlashcardSetService,
    private val chronoService: ChronoService,
    private val reviewSessionRepository: ReviewSessionRepository,
) {

    @Transactional
    fun createReviewSession(user: User, setId: Long, request: ReviewSessionCreateRequest): ReviewSessionDto {
        log.info("Creating a new review session for set $setId")
        user.checkVerified()
        val flashcardSet = flashcardSetService.getEntity(setId)
        flashcardSetService.verifyUserHasAccess(user, flashcardSet)
        flashcardSetService.verifyInitialized(flashcardSet)

        val validRequest = requestValidator.validate(request)
        val session = createNewReviewSession(flashcardSet, validRequest)
        if (session.type == ReviewSessionType.QUIZ) {
            addQuizMetadata(session, validRequest)
        }
        val savedSession = reviewSessionRepository.save(session)
        return savedSession.toDto()
    }

    @Transactional
    fun updateReviewSession(user: User, setId: Long, id: Long, request: ReviewSessionUpdateRequest) {
        log.info("Updating review session $id for set $setId")
        user.checkVerified()
        val session = getEntity(id)
        flashcardSetService.verifyUserHasAccess(user, session.flashcardSet)
        flashcardSetService.verifyInitialized(session.flashcardSet)

        val validRequest = requestValidator.validate(request)
        val changed = mergeReviewSession(session, validRequest)
        if (changed) {
            session.lastUpdatedAt = ZonedDateTime.now()
            reviewSessionRepository.save(session)
        }
    }

    private fun mergeReviewSession(session: ReviewSession, request: ValidReviewSessionUpdateRequest): Boolean {
        var changed = false
        if (request.elapsedTime > session.elapsedTime) {
            session.elapsedTime = request.elapsedTime
            changed = true
        }

        if (request.flashcardIds.isNotEmpty()) {
            val sessionFlashcardIds = session.flashcardIds?.toSet() ?: emptySet()
            val totalFlashcardIds = sessionFlashcardIds + request.flashcardIds
            session.flashcardIds = totalFlashcardIds.toLongArray()
            changed = sessionFlashcardIds.size != totalFlashcardIds.size
        }

        if (request.finished) {
            if (session.finishedAt != null) {
                throw HttpBadRequestException(ApiErrorCode.SAF400, "Review session ${session.id} is already finished")
            }
            session.finishedAt = ZonedDateTime.now()
            changed = true
        }

        if (session.type == ReviewSessionType.QUIZ) {
            if (updateQuizMetadata(session, request.metadata)) {
                changed = true
            }
        }

        return changed
    }

    @Transactional
    fun createChildReviewSession(
        user: User, setId: Long, parentId: Long,
        request: ReviewSessionCreateRequest
    ): ReviewSessionDto {
        log.info("Creating a child review session of $parentId for set $setId")
        user.checkVerified()
        val parentSession = getEntity(parentId)
        flashcardSetService.verifyUserHasAccess(user, parentSession.flashcardSet)
        flashcardSetService.verifyInitialized(parentSession.flashcardSet)

        val validRequest = requestValidator.validate(request)
        if (validRequest.type != parentSession.type) {
            throw HttpBadRequestException(
                ApiErrorCode.STM400,
                "Child session type ${validRequest.type} is not the same as parent session type ${parentSession.type}"
            )
        }

        val session = createNewReviewSession(parentSession.flashcardSet, validRequest).apply {
            parentSessionId = parentSession.id
        }
        if (session.type == ReviewSessionType.QUIZ) {
            if (parentSession.type != ReviewSessionType.QUIZ) {
                throw HttpBadRequestException(
                    ApiErrorCode.STM400,
                    "Parent session ${parentSession.id} is not ${ReviewSessionType.QUIZ}"
                )
            }
            addQuizMetadata(session, validRequest, parentSession)
        }
        val savedSession = reviewSessionRepository.save(session)
        log.info("Created child review session ${savedSession.id} of $parentId for set $setId")
        return savedSession.toDto()
    }

    @Transactional(readOnly = true)
    fun getReviewSession(user: User, setId: Long, id: Long): ReviewSessionDto {
        log.info("Getting review session $id for set $setId")
        user.checkVerified()
        val session = getEntity(id)
        flashcardSetService.verifyUserHasAccess(user, session.flashcardSet)
        return session.toDto()
    }

    @Transactional(readOnly = true)
    fun getLatestUncompletedReviewSession(user: User, setId: Long, type: ReviewSessionType): ReviewSessionDto? {
        log.info("Getting latest uncompleted review session for set $setId")
        user.checkVerified()
        val flashcardSet = flashcardSetService.getEntity(setId)
        flashcardSetService.verifyUserHasAccess(user, flashcardSet)
        val topUncompletedSessions = reviewSessionRepository
            .findLatestUncompletedReviewSessions(setId, type, 30)
            .filter {
                val metadata = it.metadata
                if (metadata != null && metadata is QuizMetadata) {
                    metadata.overallTotalCount != metadata.overallCorrectCount
                } else {
                    false
                }
            }

        val latestUncompletedSession = topUncompletedSessions.maxByOrNull { it.startedAt }
        return latestUncompletedSession?.toDto()
    }

    @Transactional(readOnly = true)
    fun getEntity(id: Long): ReviewSession {
        return reviewSessionRepository.findById(id).orElseThrow {
            HttpNotFoundException(ApiErrorCode.RES404, "Review session $id not found")
        }
    }

    private fun createNewReviewSession(
        flashcardSet: FlashcardSet,
        request: ValidReviewSessionCreateRequest
    ): ReviewSession {
        return ReviewSession(
            type = request.type,
            flashcardIds = null,
            elapsedTime = 0,
            startedAt = ZonedDateTime.now(),
            flashcardSet = flashcardSet,
            reviewDay = chronoService.getEntity(request.chronodayId),
        )
    }

}
