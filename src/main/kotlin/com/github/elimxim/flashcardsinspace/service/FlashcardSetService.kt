package com.github.elimxim.flashcardsinspace.service

import com.github.elimxim.flashcardsinspace.entity.*
import com.github.elimxim.flashcardsinspace.entity.repository.*
import com.github.elimxim.flashcardsinspace.service.validation.RequestValidator
import com.github.elimxim.flashcardsinspace.web.dto.*
import com.github.elimxim.flashcardsinspace.web.exception.ApiErrorCode
import com.github.elimxim.flashcardsinspace.web.exception.HttpBadRequestException
import com.github.elimxim.flashcardsinspace.web.exception.HttpForbiddenException
import com.github.elimxim.flashcardsinspace.web.exception.HttpNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZonedDateTime

private val log = LoggerFactory.getLogger(FlashcardSetService::class.java)

private const val SOFT_DELETE_THRESHOLD = 30

@Service
class FlashcardSetService(
    private val flashcardSetRepository: FlashcardSetRepository,
    private val chronodayRepository: ChronodayRepository,
    private val flashcardRepository: FlashcardRepository,
    private val flashcardAudioRepository: FlashcardAudioRepository,
    private val languageService: LanguageService,
    private val requestValidator: RequestValidator,
    private val reviewSessionRepository: ReviewSessionRepository,
) {
    @Transactional(readOnly = true)
    fun getAll(user: User): List<FlashcardSetDto> {
        log.info("Retrieving all flashcard sets")
        user.checkVerified()
        val result = flashcardSetRepository.findAllByUserAndStatusIn(
            user, listOf(FlashcardSetStatus.ACTIVE, FlashcardSetStatus.SUSPENDED)
        )
        return result.map { it.toDto() }
    }

    @Transactional(readOnly = true)
    fun getAllExtra(user: User): List<FlashcardSetExtraDto> {
        log.info("Retrieving all flashcard sets extra")
        user.checkVerified()
        val counts = flashcardSetRepository.countFlashcardsByUserAndStatusIn(
            user, status = listOf(FlashcardSetStatus.ACTIVE, FlashcardSetStatus.SUSPENDED)
        )
        return counts.map {
            FlashcardSetExtraDto(
                id = it.id,
                flashcardsNumber = it.flashcardCount,
            )
        }
    }

    @Transactional(readOnly = true)
    fun get(user: User, id: Long): FlashcardSetDto {
        log.info("Retrieving flashcard set $id")
        user.checkVerified()
        val flashcardSet = getEntity(id)
        verifyUserHasAccess(user, flashcardSet)
        return flashcardSet.toDto()
    }

    @Transactional
    fun create(user: User, request: FlashcardSetCreationRequest): FlashcardSetDto {
        log.info("Creating a new flashcard set")
        user.checkVerified()
        val validRequest = requestValidator.validate(request)
        val language = languageService.getEntity(validRequest.languageId)

        val flashcardSet = FlashcardSet(
            name = validRequest.name,
            language = language,
            createdAt = ZonedDateTime.now(),
            flashcards = arrayListOf(),
            user = user,
        )

        return flashcardSetRepository.save(flashcardSet).toDto()
    }

    @Transactional
    fun update(user: User, id: Long, request: FlashcardSetUpdateRequest): FlashcardSetDto {
        log.info("Updating flashcard set $id")
        user.checkVerified()
        val flashcardSet = getEntity(id)
        verifyUserHasAccess(user, flashcardSet)
        val validRequest = requestValidator.validate(request)
        val changed = mergeFlashcardSet(flashcardSet, validRequest)

        return if (changed) {
            flashcardSetRepository.save(flashcardSet).toDto()
        } else {
            flashcardSet.toDto()
        }
    }

    fun mergeFlashcardSet(flashcardSet: FlashcardSet, request: ValidFlashcardSetUpdateRequest): Boolean {
        var changed = false
        if (request.name != null && request.name != flashcardSet.name) {
            flashcardSet.name = request.name
            changed = true
        }
        if (request.status != null && request.status != flashcardSet.status) {
            flashcardSet.status = request.status
            changed = true
        }
        if (request.languageId != null && request.languageId != flashcardSet.language.id) {
            val language = languageService.getEntity(request.languageId)
            flashcardSet.language = language
            changed = true
        }

        if (changed) {
            flashcardSet.lastUpdatedAt = ZonedDateTime.now()
        }

        return changed
    }

    @Transactional
    fun remove(user: User, id: Long) {
        log.info("Removing flashcard set $id")
        user.checkVerified()
        val flashcardSet = getEntity(id)
        verifyUserHasAccess(user, flashcardSet)
        val flashcardCount = flashcardRepository.countByFlashcardSet(flashcardSet)
        if (flashcardCount >= SOFT_DELETE_THRESHOLD) {
            flashcardSet.status = FlashcardSetStatus.DELETED
            flashcardSetRepository.save(flashcardSet)
        } else {
            flashcardSet.dayStreak = null
            flashcardAudioRepository.deleteByFlashcardSetId(id)
            reviewSessionRepository.deleteByFlashcardSet(flashcardSet)
            flashcardSetRepository.delete(flashcardSet)
        }
    }

    @Transactional(readOnly = true)
    fun getEntity(id: Long): FlashcardSet {
        return flashcardSetRepository.findById(id).orElseThrow {
            HttpNotFoundException(ApiErrorCode.FSE404, "Flashcard set with id $id not found")
        }
    }

    fun verifyUserHasAccess(user: User, flashcardSet: FlashcardSet) {
        if (flashcardSet.user.id != user.id) {
            throw HttpForbiddenException(
                ApiErrorCode.FSU403,
                "User ${user.id} does not have access to flashcard set ${flashcardSet.id}"
            )
        }
    }

    fun verifyNotSuspended(flashcardSet: FlashcardSet) {
        if (flashcardSet.isSuspended()) {
            throw HttpBadRequestException(
                ApiErrorCode.FSS400,
                "Flashcard set $${flashcardSet.id} is suspended"
            )
        }
    }

    @Transactional(readOnly = true)
    fun verifyInitialized(flashcardSet: FlashcardSet) {
        val chronodayCount = chronodayRepository.countByFlashcardSetId(flashcardSet.id)
        if (chronodayCount == 0) {
            throw HttpBadRequestException(
                ApiErrorCode.FSI400,
                "Flashcard set $${flashcardSet.id} is not started"
            )
        }
    }

}
