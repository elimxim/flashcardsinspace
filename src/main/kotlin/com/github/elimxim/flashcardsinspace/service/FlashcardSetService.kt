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
import java.time.LocalDate
import java.time.ZonedDateTime
import kotlin.jvm.optionals.getOrNull

@Service
class FlashcardSetService(
    private val flashcardSetRepository: FlashcardSetRepository,
    private val languageService: LanguageService,
) {
    @Transactional
    fun getFlashcardSets(user: User): List<FlashcardSetDto> {
        val result = flashcardSetRepository.findAllByUserAndStatus(
            user = user,
            status = FlashcardSetStatus.ACTIVE
        )
        return result.map { it.toDto() }
    }

    @Transactional
    fun findFlashcardSetInt(id: Long): FlashcardSet? {
        return flashcardSetRepository.findById(id).getOrNull()
    }

    @Transactional
    fun getFlashcardSetInt(id: Long): FlashcardSet {
        return findFlashcardSetInt(id) ?: throw FlashcardSetNotFoundException(id)
    }

    @Transactional
    fun addFlashcardSet(user: User, dto: FlashcardSetDto): FlashcardSetDto {
        // todo check duplicates
        // todo check language
        val language = languageService.getLanguage(dto.languageId)
        // todo validate
        val flashcardSet = FlashcardSet(
            name = dto.name,
            language = language,
            first = dto.default,
            creationDate = LocalDate.parse(dto.createdAt),
            lastUpdatedAt = dto.lastUpdatedAt?.let { ZonedDateTime.parse(it) },
            user = user,
            flashcards = arrayListOf(),
        )

        return flashcardSetRepository.save(flashcardSet).toDto()
        // todo handle errors (using controller error interceptor?)
    }

    @Transactional
    fun updateFlashcardSet(id: Long, dto: FlashcardSetDto): FlashcardSetDto {
        // todo check for existence
        // todo validate
        val flashcardSet = flashcardSetRepository.getReferenceById(id) // fixme
        // todo move merge to a separate method
        var changed = false
        if (flashcardSet.name != dto.name) {
            flashcardSet.name = dto.name
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
    fun removeFlashcardSet(id: Long) {
        // todo check for existence
        // todo check if it's possible
        val flashcardSet = getFlashcardSetInt(id)
        if (flashcardSet.flashcards.size >= 10) {
            flashcardSet.status = FlashcardSetStatus.DELETED
            flashcardSetRepository.save(flashcardSet)
        } else {
            flashcardSetRepository.deleteById(id)
        }
    }

}