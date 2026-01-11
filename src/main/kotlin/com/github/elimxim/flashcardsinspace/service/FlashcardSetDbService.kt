package com.github.elimxim.flashcardsinspace.service

import com.github.elimxim.flashcardsinspace.entity.FlashcardSet
import com.github.elimxim.flashcardsinspace.entity.FlashcardSetStatus
import com.github.elimxim.flashcardsinspace.entity.User
import com.github.elimxim.flashcardsinspace.entity.repository.ChronodayRepository
import com.github.elimxim.flashcardsinspace.entity.repository.FlashcardRepository
import com.github.elimxim.flashcardsinspace.entity.FlashcardCount
import com.github.elimxim.flashcardsinspace.entity.repository.FlashcardSetRepository
import com.github.elimxim.flashcardsinspace.web.exception.ApiErrorCode
import com.github.elimxim.flashcardsinspace.web.exception.HttpNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

enum class FlashcardSetFetchWithMode {
    LANGUAGE,
    USER,
    FLASHCARDS,
    CHRONODAYS,
    ALL,
}

@Service
class FlashcardSetDbService(
    private val flashcardSetRepository: FlashcardSetRepository,
    private val flashcardRepository: FlashcardRepository,
    private val chronodayRepository: ChronodayRepository,
) {
    @Transactional(readOnly = true)
    fun findById(id: Long, mode: FlashcardSetFetchWithMode = FlashcardSetFetchWithMode.LANGUAGE): FlashcardSet {
        val flashcardSet = when (mode) {
            FlashcardSetFetchWithMode.LANGUAGE -> flashcardSetRepository.findWithLanguageById(id)
            FlashcardSetFetchWithMode.USER -> flashcardSetRepository.findWithUserById(id)
            FlashcardSetFetchWithMode.FLASHCARDS -> flashcardSetRepository.findWithLanguageAndFlashcardsById(id)
            FlashcardSetFetchWithMode.CHRONODAYS -> flashcardSetRepository.findWithLanguageAndChronodaysAndDayStreakById(id)
            FlashcardSetFetchWithMode.ALL -> {
                val flashcardSet = flashcardSetRepository.findWithUserAndFlashcardsById(id)
                if (flashcardSet != null) {
                    flashcardSetRepository.findWithUserAndFlashcardsById(id)
                }
                flashcardSet
            }
        }

        return flashcardSet
            ?: throw HttpNotFoundException(
                ApiErrorCode.FSE404,
                "Flashcard set with id $id not found"
            )
    }

    @Transactional
    fun findAll(user: User, vararg status: FlashcardSetStatus): List<FlashcardSet> {
        return flashcardSetRepository.findAllByUserAndStatusIn(
            user = user,
            status = status.toList()
        )
    }

    @Transactional(readOnly = true)
    fun countFlashcards(user: User, vararg status: FlashcardSetStatus): List<FlashcardCount> {
        return flashcardSetRepository.countFlashcardsByUserAndStatusIn(
            user = user,
            status = status.toList()
        )
    }

    @Transactional(readOnly = true)
    fun countFlashcards(flashcardSet: FlashcardSet): Int {
        return flashcardRepository.countByFlashcardSet(flashcardSet)
    }

    @Transactional(readOnly = true)
    fun countChronodays(flashcardSet: FlashcardSet): Int {
        return chronodayRepository.countByFlashcardSet(flashcardSet)
    }

    @Transactional
    fun save(flashcardSet: FlashcardSet): FlashcardSet {
        return flashcardSetRepository.save(flashcardSet)
    }

    @Transactional
    fun delete(flashcardSet: FlashcardSet) {
        flashcardSetRepository.delete(flashcardSet)
    }
}
