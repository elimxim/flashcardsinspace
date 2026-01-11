package com.github.elimxim.flashcardsinspace.service

import com.github.elimxim.flashcardsinspace.entity.FlashcardSet
import com.github.elimxim.flashcardsinspace.entity.repository.ChronodayRepository
import com.github.elimxim.flashcardsinspace.entity.repository.FlashcardRepository
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

}
