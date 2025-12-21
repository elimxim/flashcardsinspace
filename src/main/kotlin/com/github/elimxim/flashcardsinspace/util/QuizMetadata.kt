package com.github.elimxim.flashcardsinspace.util

import com.github.elimxim.flashcardsinspace.entity.QuizMetadata
import com.github.elimxim.flashcardsinspace.entity.ReviewSession
import com.github.elimxim.flashcardsinspace.entity.ReviewSessionType
import com.github.elimxim.flashcardsinspace.web.dto.ValidReviewSessionCreateRequest
import com.github.elimxim.flashcardsinspace.web.exception.ApiErrorCode
import com.github.elimxim.flashcardsinspace.web.exception.HttpBadRequestException

fun addQuizMetadata(
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

fun updateQuizMetadata(session: ReviewSession, metadata: Map<String, Any>): Boolean {
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
