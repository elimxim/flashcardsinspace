package com.github.elimxim.flashcardsinspace.service

import com.github.elimxim.flashcardsinspace.entity.*
import com.github.elimxim.flashcardsinspace.entity.repository.DayStreakRepository
import com.github.elimxim.flashcardsinspace.entity.repository.FlashcardSetRepository
import com.github.elimxim.flashcardsinspace.service.validation.RequestValidator
import com.github.elimxim.flashcardsinspace.web.dto.*
import com.github.elimxim.flashcardsinspace.web.exception.FlashcardSetNotFoundException
import com.github.elimxim.flashcardsinspace.web.exception.FlashcardSetSuspendedException
import com.github.elimxim.flashcardsinspace.web.exception.UserOperationNotAllowedException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZonedDateTime

private val log = LoggerFactory.getLogger(FlashcardSetService::class.java)

@Service
class FlashcardSetService(
    private val flashcardSetRepository: FlashcardSetRepository,
    private val languageService: LanguageService,
    private val requestValidator: RequestValidator,
    private val dayStreakRepository: DayStreakRepository,
) {
    @Transactional
    fun getAll(user: User): List<FlashcardSetDto> {
        log.info("Retrieving all flashcard sets")
        val result = flashcardSetRepository.findAllByUserAndStatusIn(
            user = user,
            status = listOf(FlashcardSetStatus.ACTIVE, FlashcardSetStatus.SUSPENDED)
        )
        return result.map { it.toDto() }
    }

    @Transactional
    fun getAllExtra(user: User): List<FlashcardSetExtraDto> {
        log.info("Retrieving all flashcard sets extra")
        val result = flashcardSetRepository.findAllByUserAndStatusIn(
            user = user,
            status = listOf(FlashcardSetStatus.ACTIVE, FlashcardSetStatus.SUSPENDED)
        )
        return result.map {
            FlashcardSetExtraDto(
                id = it.id,
                flashcardsNumber = it.flashcardsNumber(),
            )
        }
    }

    @Transactional
    fun get(user: User, id: Long): FlashcardSetDto {
        log.info("Retrieving flashcard set $id")
        verifyUserHasAccess(user, id)
        return getEntity(id).toDto()
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

        return flashcardSetRepository.save(flashcardSet).toDto()
    }

    @Transactional
    fun update(user: User, id: Long, request: FlashcardSetUpdateRequest): FlashcardSetDto {
        log.info("User ${user.id}: updating flashcard set $id")
        verifyUserHasAccess(user, id)
        return update(id, requestValidator.validate(request))
    }

    @Transactional
    fun update(id: Long, request: ValidFlashcardSetUpdateRequest): FlashcardSetDto {
        val flashcardSet = getEntity(id)
        val changed = mergeFlashcardSet(flashcardSet, request)

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
        verifyUserHasAccess(user, id)
        val flashcardSet = getEntity(id)
        if (flashcardSet.flashcards.size >= 40) {
            flashcardSet.status = FlashcardSetStatus.DELETED
            flashcardSetRepository.save(flashcardSet)
        } else {
            flashcardSet.dayStreak = null
            dayStreakRepository.deleteByFlashcardSetId(id)
            flashcardSetRepository.delete(flashcardSet)
        }
    }

    @Transactional
    fun getEntity(id: Long): FlashcardSet =
        flashcardSetRepository.findById(id).orElseThrow {
            FlashcardSetNotFoundException("Flashcard set with id $id not found")
        }

    @Transactional
    fun verifyUserHasAccess(user: User, id: Long) {
        if (getEntity(id).user.id != user.id) {
            throw UserOperationNotAllowedException("User ${user.id} does not have access to flashcard set $id")
        }
    }

    @Transactional
    fun verifyNotSuspended(id: Long) {
        val flashcardSet = getEntity(id)
        if (flashcardSet.isSuspended()) {
            throw FlashcardSetSuspendedException("Flashcard set $id is suspended")
        }
    }
}
