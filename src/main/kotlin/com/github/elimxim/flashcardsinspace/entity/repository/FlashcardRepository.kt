package com.github.elimxim.flashcardsinspace.entity.repository

import com.github.elimxim.flashcardsinspace.entity.Flashcard
import com.github.elimxim.flashcardsinspace.entity.FlashcardSet
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface FlashcardRepository : JpaRepository<Flashcard, Long> {
    @EntityGraph(attributePaths = ["flashcardSet"])
    override fun findById(id: Long): Optional<Flashcard>

    fun countByFlashcardSet(flashcardSet: FlashcardSet): Int
}
