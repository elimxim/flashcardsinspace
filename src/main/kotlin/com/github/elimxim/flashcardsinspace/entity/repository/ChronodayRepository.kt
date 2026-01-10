package com.github.elimxim.flashcardsinspace.entity.repository

import com.github.elimxim.flashcardsinspace.entity.Chronoday
import com.github.elimxim.flashcardsinspace.entity.FlashcardSet
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ChronodayRepository : JpaRepository<Chronoday, Long> {
    fun countByFlashcardSet(flashcardSet: FlashcardSet): Int
}
