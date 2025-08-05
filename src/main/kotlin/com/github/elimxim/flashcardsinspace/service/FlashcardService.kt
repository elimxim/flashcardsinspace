package com.github.elimxim.flashcardsinspace.service

import com.github.elimxim.flashcardsinspace.entity.Flashcard
import com.github.elimxim.flashcardsinspace.entity.FlashcardSet
import com.github.elimxim.flashcardsinspace.entity.FlashcardSetStatus
import com.github.elimxim.flashcardsinspace.entity.ReviewHistory
import com.github.elimxim.flashcardsinspace.entity.ReviewInfo
import com.github.elimxim.flashcardsinspace.entity.FlashcardStage
import com.github.elimxim.flashcardsinspace.entity.User
import com.github.elimxim.flashcardsinspace.entity.repository.FlashcardRepository
import com.github.elimxim.flashcardsinspace.entity.repository.FlashcardSetRepository
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardDto
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardSetDto
import com.github.elimxim.flashcardsinspace.web.dto.toDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.ZonedDateTime

@Service
class FlashcardService(
    private val flashcardSetRepository: FlashcardSetRepository,
    private val flashcardRepository: FlashcardRepository,
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
        val flashcardSet = getFlashcardSet(id)
        if (flashcardSet.flashcards.size >= 10) {
            flashcardSet.status = FlashcardSetStatus.DELETED
            flashcardSetRepository.save(flashcardSet)
        } else {
            flashcardSetRepository.deleteById(id)
        }
    }

    @Transactional
    fun getFlashcards(flashcardSetId: Long): List<FlashcardDto> {
        // todo check for existence
        return getFlashcardSet(flashcardSetId).flashcards.map { it.toDto() }
    }

    @Transactional
    fun addFlashcard(flashcardSetId: Long, dto: FlashcardDto): FlashcardDto {
        // todo validate
        val flashcardSet = getFlashcardSet(flashcardSetId)
        // todo check for existence
        val flashcard = Flashcard(
            frontSide = dto.frontSide,
            backSide = dto.backSide,
            stage = FlashcardStage.valueOf(dto.stage),
            reviewedTimes = dto.reviewedTimes,
            creationDate = LocalDate.parse(dto.createdAt),
            flashcardSet = flashcardSet,
        )

        flashcardSet.flashcards.add(flashcard)
        return flashcardRepository.save(flashcard).toDto()
    }

    @Transactional
    fun updateFlashcard(flashcardSetId: Long, id: Long, dto: FlashcardDto): FlashcardDto {
        // todo validate
        // todo check for existence
        val flashcardSet = getFlashcardSet(flashcardSetId)
        // todo check for existence
        val flashcard = getFlashcard(id)
        // todo move merge to a separate method
        var changed = false
        if (flashcard.frontSide != dto.frontSide) {
            flashcard.frontSide = dto.frontSide
            changed = true
        }
        if (flashcard.backSide != dto.backSide) {
            flashcard.backSide = dto.backSide
            changed = true
        }
        val stage = FlashcardStage.valueOf(dto.stage)
        if (flashcard.stage != stage) {
            flashcard.stage = stage
            changed = true
        }
        if (flashcard.reviewedTimes != dto.reviewedTimes) {
            flashcard.reviewedTimes = dto.reviewedTimes
            changed = true
        }
        if (flashcard.reviewHistory.history.size != dto.reviewHistory.history.size) {
            flashcard.reviewHistory = ReviewHistory(
                history = dto.reviewHistory.history.map {
                    ReviewInfo(
                        stage = FlashcardStage.valueOf(it.stage),
                        reviewDate = LocalDate.parse(it.reviewedAt)
                    )
                }.toMutableList()
            )
            changed = true
        }
        if (flashcard.reviewDate?.toString() != dto.reviewedAt) {
            flashcard.reviewDate = dto.reviewedAt?.let { LocalDate.parse(it) }
        }

        if (changed) {
            flashcard.lastUpdatedAt = ZonedDateTime.now()
            return flashcardRepository.save(flashcard).toDto()
        } else {
            // todo log
            return dto
        }
    }

    @Transactional
    fun removeFlashcard(flashcardSetId: Long, id: Long) {
        // check for existence
        val flashcardSet = getFlashcardSet(flashcardSetId)
        // check for existence
        val flashcard = getFlashcard(id)
        flashcardSet.flashcards.remove(flashcard)
        flashcardRepository.delete(flashcard)
    }

    private fun getFlashcardSet(id: Long): FlashcardSet {
        return flashcardSetRepository.getReferenceById(id) // fixme
    }

    private fun getFlashcard(id: Long): Flashcard {
        return flashcardRepository.getReferenceById(id) // fixme
    }
}