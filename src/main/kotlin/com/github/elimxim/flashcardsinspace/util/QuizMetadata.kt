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
    val metadata = if (parentSession?.metadata != null) {
        val parentMetadata = parentSession.metadata
        if (parentMetadata !is QuizMetadata) {
            throw HttpBadRequestException(
                ApiErrorCode.SMM400,
                "Parent session ${parentSession.id} metadata is not ${ReviewSessionType.QUIZ}"
            )
        }

        QuizMetadata(
            stages = parentMetadata.stages,
            round = parentMetadata.round + 1,
            currRoundFlashcardIds = parentMetadata.nextRoundFlashcardIds,
            nextRoundFlashcardIds = mutableSetOf(),
            overallCorrectCount = parentMetadata.overallCorrectCount,
            overallIncorrectCount = parentMetadata.overallIncorrectCount,
            elapsedTotalTime = parentMetadata.elapsedTotalTime,
        )
    } else {
        val stages = getMetadataFieldName(QuizMetadata::stages)?.let { fieldName ->
            request.metadata[fieldName]?.let { parseStringArray(it) }?.toMutableSet()
        } ?: mutableSetOf()

        val currRoundFlashcardIds = getMetadataFieldName(QuizMetadata::currRoundFlashcardIds)?.let { fieldName ->
            request.metadata[fieldName]?.let { parseNumericArray(it) }?.toMutableSet()
        } ?: mutableSetOf()

        QuizMetadata(
            stages = stages,
            round = 1,
            currRoundFlashcardIds = currRoundFlashcardIds,
            nextRoundFlashcardIds = mutableSetOf(),
            overallCorrectCount = 0, // a new session doesn't have any correct flashcards
            overallIncorrectCount = 0, // a new session doesn't have any incorrect flashcards
            elapsedTotalTime = 0,
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
            val ids = parseNumericArray(nextRoundFlashcardIds)
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

    getMetadataFieldName(QuizMetadata::overallIncorrectCount)?.let { fieldName ->
        val overallIncorrectCount = metadata[fieldName]
        if (overallIncorrectCount != null && overallIncorrectCount is Int && overallIncorrectCount > 0) {
            sessionMetadata.overallIncorrectCount = overallIncorrectCount
            changed = true
        }
    }

    getMetadataFieldName(QuizMetadata::elapsedTotalTime)?.let { fieldName ->
        val elapsedTotalTime = metadata[fieldName]
        if (elapsedTotalTime != null && (elapsedTotalTime is Int || elapsedTotalTime is Long) && elapsedTotalTime > 0) {
            sessionMetadata.elapsedTotalTime = elapsedTotalTime.toLong()
            changed = true
        }
    }

    return changed
}

private fun parseNumericArray(value: Any): List<Long> {
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

private fun parseStringArray(value: Any): List<String> {
    return when (value) {
        is String -> value.split(",").map { it.trim() }.filter { it.isNotEmpty() }

        is List<*> -> value.mapNotNull { item ->
            when (item) {
                is String -> if (item.trim().isNotEmpty()) item else null
                else -> null
            }
        }

        else -> emptyList()
    }
}
