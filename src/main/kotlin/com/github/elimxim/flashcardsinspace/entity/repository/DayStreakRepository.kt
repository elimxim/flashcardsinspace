package com.github.elimxim.flashcardsinspace.entity.repository

import com.github.elimxim.flashcardsinspace.entity.DayStreak
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface DayStreakRepository : JpaRepository<DayStreak, Long> {
    @Modifying
    @Query("DELETE FROM DayStreak d WHERE d.flashcardSet.id = :flashcardSetId")
    fun deleteByFlashcardSetId(flashcardSetId: Long)
}
