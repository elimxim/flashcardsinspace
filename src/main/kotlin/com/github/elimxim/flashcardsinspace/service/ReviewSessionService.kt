package com.github.elimxim.flashcardsinspace.service

import com.github.elimxim.flashcardsinspace.entity.QuizMetadata
import com.github.elimxim.flashcardsinspace.entity.ReviewSession
import com.github.elimxim.flashcardsinspace.entity.ReviewSessionType
import com.github.elimxim.flashcardsinspace.entity.User
import com.github.elimxim.flashcardsinspace.entity.repository.ReviewSessionRepository
import com.github.elimxim.flashcardsinspace.service.validation.RequestValidator
import com.github.elimxim.flashcardsinspace.util.getMetadataFieldName
import com.github.elimxim.flashcardsinspace.util.numbersOnlyPattern
import com.github.elimxim.flashcardsinspace.web.dto.*
import com.github.elimxim.flashcardsinspace.web.exception.ParentReviewSessionIsNotQuizException
import com.github.elimxim.flashcardsinspace.web.exception.ReviewSessionNotFoundException
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
        flashcardSetService.verifyUserHasAccess(user, setId)
        flashcardSetService.verifyInitialized(setId)
        return createReviewSession(setId, requestValidator.validate(request))
    }

    @Transactional
    fun createReviewSession(setId: Long, request: ValidReviewSessionCreateRequest): ReviewSessionDto {
        val session = createNewReviewSession(setId, request)
        if (session.type == ReviewSessionType.QUIZ) {
            addQuizMetadata(session)
        }
        val savedSession = reviewSessionRepository.save(session)
        return savedSession.toDto()
    }

    @Transactional
    fun updateReviewSession(user: User, setId: Long, id: Long, request: ReviewSessionUpdateRequest) {
        log.info("Updating review session $id for set $setId")
        flashcardSetService.verifyUserHasAccess(user, setId)
        flashcardSetService.verifyInitialized(setId)
        updateReviewSession(id, requestValidator.validate(request))
    }

    @Transactional
    fun updateReviewSession(id: Long, request: ValidReviewSessionUpdateRequest) {
        val session = getEntity(id)
        val changed = mergeReviewSession(session, request)
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
            val flashcardIds = (session.flashcardIds?.toSet() ?: emptySet()) + request.flashcardIds
            session.flashcardIds = flashcardIds.toLongArray()
            changed = true
        }

        if (session.type == ReviewSessionType.QUIZ) {
            changed = updateQuizMetadata(session, request.metadata)
        }

        return changed
    }

    @Transactional
    fun createChildReviewSession(
        user: User, setId: Long, parentId: Long,
        request: ReviewSessionCreateRequest
    ): ReviewSessionDto {
        log.info("Creating a child review session of $parentId for set $setId")
        flashcardSetService.verifyUserHasAccess(user, setId)
        flashcardSetService.verifyInitialized(setId)
        return createChildReviewSession(setId, parentId, requestValidator.validate(request))
    }

    @Transactional
    fun createChildReviewSession(setId: Long, parentId: Long, request: ValidReviewSessionCreateRequest): ReviewSessionDto {
        val parentSession = getEntity(parentId)
        val session = createNewReviewSession(setId, request).apply {
            parentSessionId = parentSession.id
        }
        if (session.type == ReviewSessionType.QUIZ) {
            if (parentSession.type != ReviewSessionType.QUIZ) {
                throw ParentReviewSessionIsNotQuizException(
                    "Parent session ${parentSession.id} type is not ${ReviewSessionType.QUIZ}"
                )
            }
            addQuizMetadata(session)
        }
        val savedSession = reviewSessionRepository.save(session)
        return savedSession.toDto()
    }

    @Transactional
    fun getReviewSession(user: User, setId: Long, id: Long): ReviewSessionDto {
        log.info("Getting review session $id for set $setId")
        flashcardSetService.verifyUserHasAccess(user, setId)
        return getEntity(id).toDto()
    }

    @Transactional
    fun getEntity(id: Long): ReviewSession {
        return reviewSessionRepository.findById(id).orElseThrow {
            ReviewSessionNotFoundException("Review session $id not found")
        }
    }

    private fun createNewReviewSession(setId: Long, request: ValidReviewSessionCreateRequest): ReviewSession {
        return ReviewSession(
            type = request.type,
            flashcardIds = request.flashcardIds.toLongArray(),
            elapsedTime = 0,
            startedAt = ZonedDateTime.now(),
            flashcardSet = flashcardSetService.getEntity(setId),
            reviewDay = chronoService.getEntity(request.chronodayId),
        )
    }

    private fun addQuizMetadata(session: ReviewSession, parentSession: ReviewSession? = null) {
        val metadata = if (parentSession != null && parentSession.metadata != null) {
            val parentMetadata = parentSession.metadata
            if (parentMetadata !is QuizMetadata) {
                throw ParentReviewSessionIsNotQuizException(
                    "Parent session ${parentSession.id} metadata is not ${ReviewSessionType.QUIZ}"
                )
            }

            QuizMetadata(
                round = parentMetadata.round + 1,
                overallCorrectCount = parentMetadata.overallCorrectCount,
                overallTotalCount = parentMetadata.overallTotalCount,
            )
        } else {
            QuizMetadata(
                round = 1,
                overallCorrectCount = 0,
                overallTotalCount = session.flashcardIds!!.size,
            )
        }

        session.metadata = metadata
    }

    private fun updateQuizMetadata(session: ReviewSession, metadata: Map<String, Any>): Boolean {
        val sessionMetadata = session.metadata
        if (sessionMetadata == null) {
            log.warn("Review session ${session.id} has no metadata")
            return false
        }

        if (sessionMetadata !is QuizMetadata) {
            log.warn("Review session ${session.id} metadata is not ${ReviewSessionType.QUIZ}")
            return false
        }

        val nextRoundFlashcardIdsFieldName = getMetadataFieldName(QuizMetadata::nextRoundFlashcardIds)
        if (nextRoundFlashcardIdsFieldName != null) {
            val nextRoundFlashcardIds = metadata[nextRoundFlashcardIdsFieldName]
            if (nextRoundFlashcardIds != null) {
                val ids = parseFlashcardIds(nextRoundFlashcardIds)
                sessionMetadata.nextRoundFlashcardIds.addAll(ids)
                return ids.isNotEmpty()
            }
        }

        return false
    }

    private fun parseFlashcardIds(value: Any): List<Long> {
        return when (value) {
            is String -> value.split(",")
                .filter { it.matches(numbersOnlyPattern) }
                .map { it.toLong() }
            is List<*> -> value.mapNotNull { item ->
                when (item) {
                    is Number -> item.toLong()
                    is String -> if (item.matches(numbersOnlyPattern)) item.toLong() else null
                    else -> null
                }
            }
            else -> emptyList()
        }
    }
}
