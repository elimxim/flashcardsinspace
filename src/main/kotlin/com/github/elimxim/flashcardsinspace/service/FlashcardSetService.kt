package com.github.elimxim.flashcardsinspace.service

import com.github.elimxim.flashcardsinspace.entity.FlashcardSet
import com.github.elimxim.flashcardsinspace.entity.FlashcardSetStatus
import com.github.elimxim.flashcardsinspace.entity.User
import com.github.elimxim.flashcardsinspace.entity.repository.FlashcardSetRepository
import com.github.elimxim.flashcardsinspace.service.validation.RequestValidator
import com.github.elimxim.flashcardsinspace.web.dto.*
import com.github.elimxim.flashcardsinspace.web.exception.FlashcardSetNotFoundException
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
) {
    @Transactional
    fun getAll(user: User): List<FlashcardSetDto> {
        log.info("User ${user.id}: retrieving all flashcard sets")
        val result = flashcardSetRepository.findAllByUserAndStatus(
            user = user,
            status = FlashcardSetStatus.ACTIVE
        )
        return result.map { it.toDto() }
    }

    @Transactional
    fun get(user: User, id: Long): FlashcardSetDto {
        log.info("User ${user.id}: retrieving flashcard set $id")
        verifyUserHasAccess(user, id)
        return getEntity(id).toDto()
    }

    @Transactional
    fun create(user: User, request: FlashcardSetCreationRequest): FlashcardSetDto {
        log.info("User ${user.id}: creating a new flashcard set")
        return create(user, requestValidator.validate(request))
    }

    @Transactional
    fun create(user: User, request: ValidFlashcardSetCreationRequest): FlashcardSetDto {
        val language = languageService.getEntity(request.languageId)

        val flashcardSet = FlashcardSet(
            name = request.name,
            language = language,
            first = false,
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

        if (changed) {
            flashcardSet.lastUpdatedAt = ZonedDateTime.now()
            return flashcardSetRepository.save(flashcardSet).toDto()
        } else {
            return flashcardSet.toDto()
        }
    }

    private fun mergeFlashcardSet(flashcardSet: FlashcardSet, request: ValidFlashcardSetUpdateRequest): Boolean {
        var changed = false
        if (flashcardSet.name != request.name) {
            flashcardSet.name = request.name
            changed = true
        }
        if (flashcardSet.status != request.status) {
            val newStatus = request.status
            flashcardSet.status = newStatus
            changed = true
        }
        if (request.first != null && request.first != flashcardSet.first) {
            flashcardSet.first = request.first
            changed = true
        }
        if (flashcardSet.language.id != request.languageId) {
            val language = languageService.getEntity(request.languageId)
            flashcardSet.language = language
            changed = true
        }

        return changed
    }

    @Transactional
    fun remove(user: User, id: Long) {
        log.info("User ${user.id}: removing flashcard set $id")
        verifyUserHasAccess(user, id)
        val flashcardSet = getEntity(id)
        if (flashcardSet.flashcards.size >= 40) {
            flashcardSet.status = FlashcardSetStatus.DELETED
            flashcardSetRepository.save(flashcardSet)
        } else {
            flashcardSetRepository.deleteById(id)
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
}
