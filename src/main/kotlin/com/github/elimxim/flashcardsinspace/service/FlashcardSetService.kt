package com.github.elimxim.flashcardsinspace.service

import com.github.elimxim.flashcardsinspace.entity.FlashcardSet
import com.github.elimxim.flashcardsinspace.entity.FlashcardSetStatus
import com.github.elimxim.flashcardsinspace.entity.User
import com.github.elimxim.flashcardsinspace.entity.isSuspended
import com.github.elimxim.flashcardsinspace.entity.repository.DayStreakRepository
import com.github.elimxim.flashcardsinspace.entity.repository.ReviewSessionRepository
import com.github.elimxim.flashcardsinspace.service.validation.RequestValidator
import com.github.elimxim.flashcardsinspace.web.dto.*
import com.github.elimxim.flashcardsinspace.web.exception.ApiErrorCode
import com.github.elimxim.flashcardsinspace.web.exception.HttpBadRequestException
import com.github.elimxim.flashcardsinspace.web.exception.HttpForbiddenException
import jakarta.persistence.EntityManager
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZonedDateTime

private val log = LoggerFactory.getLogger(FlashcardSetService::class.java)

@Service
class FlashcardSetService(
    private val flashcardSetDbService: FlashcardSetDbService,
    private val languageService: LanguageService,
    private val requestValidator: RequestValidator,
    private val dayStreakRepository: DayStreakRepository,
    private val reviewSessionRepository: ReviewSessionRepository,
    private val entityManager: EntityManager,
) {
    @Transactional(readOnly = true)
    fun getAll(user: User): List<FlashcardSetDto> {
        log.info("Retrieving all flashcard sets")
        val result = flashcardSetDbService.findAll(
            user, FlashcardSetStatus.ACTIVE, FlashcardSetStatus.SUSPENDED
        )
        return result.map { it.toDto() }
    }

    @Transactional(readOnly = true)
    fun getAllExtra(user: User): List<FlashcardSetExtraDto> {
        log.info("Retrieving all flashcard sets extra")
        val counts = flashcardSetDbService.countFlashcards(
            user, FlashcardSetStatus.ACTIVE, FlashcardSetStatus.SUSPENDED
        )
        return counts.map {
            FlashcardSetExtraDto(
                id = it.getId(),
                flashcardsNumber = it.getFlashcardCount(),
            )
        }
    }

    @Transactional(readOnly = true)
    fun get(user: User, id: Long): FlashcardSetDto {
        log.info("Retrieving flashcard set $id")
        verifyUserHasAccess(user, id)
        return flashcardSetDbService.findById(id).toDto()
    }

    @Transactional
    fun create(user: User, request: FlashcardSetCreationRequest): FlashcardSetDto {
        log.info("Creating a new flashcard set")
        return create(user, requestValidator.validate(request))
    }

    @Transactional
    fun create(user: User, request: ValidFlashcardSetCreationRequest): FlashcardSetDto {
        val language = languageService.getEntity(request.languageId)

        val flashcardSet = FlashcardSet(
            name = request.name,
            language = language,
            createdAt = ZonedDateTime.now(),
            flashcards = arrayListOf(),
            user = user,
        )

        return flashcardSetDbService.save(flashcardSet).toDto()
    }

    @Transactional
    fun update(user: User, id: Long, request: FlashcardSetUpdateRequest): FlashcardSetDto {
        log.info("User ${user.id}: updating flashcard set $id")
        verifyUserHasAccess(user, id)
        return update(id, requestValidator.validate(request))
    }

    @Transactional
    fun update(id: Long, request: ValidFlashcardSetUpdateRequest): FlashcardSetDto {
        val flashcardSet = flashcardSetDbService.findById(id)
        val changed = mergeFlashcardSet(flashcardSet, request)

        return if (changed) {
            flashcardSetDbService.save(flashcardSet).toDto()
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
        verifyUserHasAccess(user, id)
        val flashcardSet = flashcardSetDbService.findById(id)
        val flashcardCount = flashcardSetDbService.countFlashcards(flashcardSet)
        if (flashcardCount >= 40) {
            flashcardSet.status = FlashcardSetStatus.DELETED
            flashcardSetDbService.save(flashcardSet)
        } else {
            dayStreakRepository.deleteByFlashcardSetId(id)
            reviewSessionRepository.deleteByFlashcardSetId(id)
            entityManager.flush()
            entityManager.clear()
            flashcardSetDbService.delete(flashcardSet)
        }
    }

    @Transactional(readOnly = true)
    fun verifyUserHasAccess(user: User, id: Long) {
        if (flashcardSetDbService.findById(id, mode = FlashcardSetFetchWithMode.USER).user.id != user.id) {
            throw HttpForbiddenException(
                ApiErrorCode.FSU403,
                "User ${user.id} does not have access to flashcard set $id"
            )
        }
    }

    @Transactional(readOnly = true)
    fun verifyInitialized(id: Long) {
        val flashcardSet = flashcardSetDbService.findById(id)
        val chronodayCount = flashcardSetDbService.countChronodays(flashcardSet)
        if (chronodayCount == 0) {
            throw HttpBadRequestException(ApiErrorCode.FSI400, "Flashcard set $id is not started")
        }
    }

    @Transactional(readOnly = true)
    fun verifyNotSuspended(id: Long) {
        val flashcardSet = flashcardSetDbService.findById(id)
        if (flashcardSet.isSuspended()) {
            throw HttpBadRequestException(ApiErrorCode.FSS400, "Flashcard set $id is suspended")
        }
    }
}
