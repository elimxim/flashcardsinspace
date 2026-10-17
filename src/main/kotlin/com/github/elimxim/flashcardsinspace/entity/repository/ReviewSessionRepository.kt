package com.github.elimxim.flashcardsinspace.entity.repository

import com.github.elimxim.flashcardsinspace.entity.FlashcardSet
import com.github.elimxim.flashcardsinspace.entity.ReviewSession
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.*

interface ReviewSessionRepository : JpaRepository<ReviewSession, Long> {
    @Query("""
        SELECT parent
        FROM ReviewSession parent
        WHERE parent.flashcardSet.id = :flashcardSetId
          AND parent.type = ReviewSessionType.QUIZ
          AND parent.startedAt >= CURRENT_TIMESTAMP - :depthDays DAY
          AND NOT EXISTS (
              SELECT 1
              FROM ReviewSession child
              WHERE child.flashcardSet.id = :flashcardSetId
                AND child.type = ReviewSessionType.QUIZ
                AND child.startedAt > parent.startedAt
                AND child.parentSessionId = parent.id
          )
        ORDER BY parent.startedAt DESC
        LIMIT 1
    """)
    fun findLatestQuizSessionWithoutChildren(flashcardSetId: Long, depthDays: Int): Optional<ReviewSession>

    @EntityGraph(attributePaths = ["flashcardSet", "reviewDay"])
    override fun findById(id: Long): Optional<ReviewSession>

    fun deleteByFlashcardSet(flashcardSet: FlashcardSet)
}
