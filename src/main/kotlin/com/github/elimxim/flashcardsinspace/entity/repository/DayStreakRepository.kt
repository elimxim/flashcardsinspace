package com.github.elimxim.flashcardsinspace.entity.repository

import com.github.elimxim.flashcardsinspace.entity.DayStreak
import com.github.elimxim.flashcardsinspace.entity.FlashcardSet
import org.springframework.data.jpa.repository.JpaRepository

interface DayStreakRepository : JpaRepository<DayStreak, Long> {
    fun deleteByFlashcardSet(flashcardSet: FlashcardSet)
}
