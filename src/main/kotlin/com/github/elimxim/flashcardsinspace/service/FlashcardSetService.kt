package com.github.elimxim.flashcardsinspace.service

import com.github.elimxim.flashcardsinspace.entity.FlashcardSet
import com.github.elimxim.flashcardsinspace.entity.FlashcardSetStatus
import com.github.elimxim.flashcardsinspace.entity.User
import com.github.elimxim.flashcardsinspace.entity.repository.FlashcardSetRepository
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardSetDto
import com.github.elimxim.flashcardsinspace.web.dto.toDto
import com.github.elimxim.flashcardsinspace.web.exception.FlashcardSetNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZonedDateTime

@Service
class FlashcardSetService(
    private val flashcardSetRepository: FlashcardSetRepository,
    private val languageService: LanguageService,
) {
    @Transactional
    fun findAll(user: User): List<FlashcardSetDto> {
        val result = flashcardSetRepository.findAllByUserAndStatus(
            user = user,
            status = FlashcardSetStatus.ACTIVE
        )
        return result.map { it.toDto() }
    }

    @Transactional
    fun createNew(user: User, dto: FlashcardSetDto): FlashcardSetDto {
        // todo check duplicates
        // todo check language
        val language = languageService.getLanguage(dto.languageId)
        // todo validate
        val flashcardSet = FlashcardSet(
            name = dto.name,
            language = language,
            first = dto.default,
            createdAt = ZonedDateTime.parse(dto.createdAt),
            lastUpdatedAt = dto.lastUpdatedAt?.let { ZonedDateTime.parse(it) },
            user = user,
            flashcards = arrayListOf(),
        )

        return flashcardSetRepository.save(flashcardSet).toDto()
        // todo handle errors (using controller error interceptor?)
    }

    @Transactional
    fun update(id: Long, dto: FlashcardSetDto): FlashcardSetDto {
        // todo check for existence
        // todo validate
        val flashcardSet = flashcardSetRepository.getReferenceById(id) // fixme
        // todo move merge to a separate method
        var changed = false
        if (flashcardSet.name != dto.name) {
            flashcardSet.name = dto.name
            changed = true
        }
        if (flashcardSet.status.name != dto.status) {
            val newStatus = FlashcardSetStatus.valueOf(dto.status) // todo validate
            flashcardSet.status = newStatus
            changed = true
        }
        if (flashcardSet.first != dto.default) {
            flashcardSet.first = dto.default
            changed = true
        }
        if (flashcardSet.language.id != dto.languageId) {
            // todo check for existence
            val language = languageService.getLanguage(dto.languageId)
            flashcardSet.language = language
            changed = true
        }

        if (changed) {
            flashcardSet.lastUpdatedAt = ZonedDateTime.now()
            return flashcardSetRepository.save(flashcardSet).toDto()
        } else {
            // todo log
            return dto
        }
    }

    @Transactional
    fun remove(id: Long) {
        // todo check for existence
        // todo check if it's possible
        val flashcardSet = getEntity(id)
        if (flashcardSet.flashcards.size >= 10) {
            flashcardSet.status = FlashcardSetStatus.DELETED
            flashcardSetRepository.save(flashcardSet)
        } else {
            flashcardSetRepository.deleteById(id)
        }
    }

    @Transactional
    fun get(id: Long): FlashcardSetDto = getEntity(id).toDto()

    @Transactional
    fun getEntity(id: Long): FlashcardSet =
        flashcardSetRepository.findById(id).orElseThrow {
            FlashcardSetNotFoundException("Flashcard set with id $id not found")
        }

}