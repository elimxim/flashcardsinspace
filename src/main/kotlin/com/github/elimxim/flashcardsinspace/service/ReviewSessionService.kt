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
        flashcardSetService.verifyUserHasAccess(user, setId)
        flashcardSetService.verifyInitialized(setId)
        return createReviewSession(setId, requestValidator.validate(request))
    }

    @Transactional
    fun createReviewSession(setId: Long, request: ValidReviewSessionCreateRequest): ReviewSessionDto {
        val session = createNewReviewSession(setId, request)
        if (session.type == ReviewSessionType.QUIZ) {
            addQuizMetadata(session, request)
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
        flashcardSetService.verifyUserHasAccess(user, setId)
        flashcardSetService.verifyInitialized(setId)
        return createChildReviewSession(setId, parentId, requestValidator.validate(request))
    }

    @Transactional
    fun createChildReviewSession(
        setId: Long,
        parentId: Long,
        request: ValidReviewSessionCreateRequest
    ): ReviewSessionDto {
        val parentSession = getEntity(parentId)
        if (request.type != parentSession.type) {
            throw HttpBadRequestException(
                ApiErrorCode.STM400,
                "Child session type ${request.type} is not the same as parent session type ${parentSession.type}"
            )
        }

        val session = createNewReviewSession(setId, request).apply {
            parentSessionId = parentSession.id
        }
        if (session.type == ReviewSessionType.QUIZ) {
            if (parentSession.type != ReviewSessionType.QUIZ) {
                throw HttpBadRequestException(
                    ApiErrorCode.STM400,
                    "Parent session ${parentSession.id} is not ${ReviewSessionType.QUIZ}"
                )
            }
            addQuizMetadata(session, request, parentSession)
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
    fun getLatestReviewSession(user: User, setId: Long, type: ReviewSessionType): ReviewSessionDto? {
        log.info("Getting latest review session for set $setId")
        flashcardSetService.verifyUserHasAccess(user, setId)
        return reviewSessionRepository
            .findTopByFlashcardSetIdAndTypeOrderByStartedAtDesc(setId, type)
            .map { it.toDto() }
            .orElse(null)
    }

    @Transactional
    fun getEntity(id: Long): ReviewSession {
        return reviewSessionRepository.findById(id).orElseThrow {
            HttpNotFoundException(ApiErrorCode.RES404, "Review session $id not found")
        }
    }

    private fun createNewReviewSession(setId: Long, request: ValidReviewSessionCreateRequest): ReviewSession {
        return ReviewSession(
            type = request.type,
            flashcardIds = null,
            elapsedTime = 0,
            startedAt = ZonedDateTime.now(),
            flashcardSet = flashcardSetService.getEntity(setId),
            reviewDay = chronoService.getEntity(request.chronodayId),
        )
    }

    private fun addQuizMetadata(
        session: ReviewSession,
        request: ValidReviewSessionCreateRequest,
        parentSession: ReviewSession? = null
    ) {
        val metadata = if (parentSession != null && parentSession.metadata != null) {
            val parentMetadata = parentSession.metadata
            if (parentMetadata !is QuizMetadata) {
                throw HttpBadRequestException(
                    ApiErrorCode.SMM400,
                    "Parent session ${parentSession.id} metadata is not ${ReviewSessionType.QUIZ}"
                )
            }

            QuizMetadata(
                round = parentMetadata.round + 1,
                currRoundFlashcardIds = parentMetadata.nextRoundFlashcardIds,
                nextRoundFlashcardIds = mutableSetOf(),
                overallCorrectCount = parentMetadata.overallCorrectCount,
                overallTotalCount = parentMetadata.overallTotalCount,
            )
        } else {
            val currRoundFlashcardIds = getMetadataFieldName(QuizMetadata::currRoundFlashcardIds)?.let { fieldName ->
                request.metadata[fieldName]?.let { parseFlashcardIds(it) }?.toMutableSet()
            } ?: mutableSetOf()

            val overallTotalCount = getMetadataFieldName(QuizMetadata::overallTotalCount)?.let { fieldName ->
                request.metadata[fieldName]?.let { it as? Int ?: 0 }
            } ?: 0

            QuizMetadata(
                round = 1,
                currRoundFlashcardIds = currRoundFlashcardIds,
                nextRoundFlashcardIds = mutableSetOf(),
                overallCorrectCount = 0,
                overallTotalCount = overallTotalCount,
            )
        }

        session.metadata = metadata
    }

    private fun updateQuizMetadata(session: ReviewSession, metadata: Map<String, Any>): Boolean {
        val sessionMetadata = session.metadata
            ?: throw HttpBadRequestException(
                ApiErrorCode.SNM400,
                "Review session ${session.id} has no metadata"
            )

        if (sessionMetadata !is QuizMetadata) {
            throw HttpBadRequestException(
                ApiErrorCode.NQM400,
                "Review session ${session.id} metadata is not ${ReviewSessionType.QUIZ}"
            )
        }

        var changed = false
        getMetadataFieldName(QuizMetadata::nextRoundFlashcardIds)?.let { fieldName ->
            val nextRoundFlashcardIds = metadata[fieldName]
            if (nextRoundFlashcardIds != null) {
                val ids = parseFlashcardIds(nextRoundFlashcardIds)
                sessionMetadata.nextRoundFlashcardIds.addAll(ids)
                changed = ids.isNotEmpty()
            }
        }

        getMetadataFieldName(QuizMetadata::overallCorrectCount)?.let { fieldName ->
            val overallCorrectCount = metadata[fieldName]
            if (overallCorrectCount != null && overallCorrectCount is Int && overallCorrectCount > 0) {
                sessionMetadata.overallCorrectCount = overallCorrectCount
                changed = true
            }
        }

        return changed
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
