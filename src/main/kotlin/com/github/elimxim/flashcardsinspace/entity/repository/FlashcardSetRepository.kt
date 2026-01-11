package com.github.elimxim.flashcardsinspace.entity.repository

import com.github.elimxim.flashcardsinspace.entity.FlashcardCount
import com.github.elimxim.flashcardsinspace.entity.FlashcardSet
import com.github.elimxim.flashcardsinspace.entity.FlashcardSetStatus
import com.github.elimxim.flashcardsinspace.entity.User
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface FlashcardSetRepository : JpaRepository<FlashcardSet, Long> {
    @EntityGraph(attributePaths = ["language"])
    fun findAllByUserAndStatusIn(user: User, status: List<FlashcardSetStatus>): List<FlashcardSet>

    @EntityGraph(attributePaths = ["language"])
    fun findWithLanguageById(id: Long): FlashcardSet?

    @EntityGraph(attributePaths = ["user"])
    fun findWithUserById(id: Long): FlashcardSet?

    @EntityGraph(attributePaths = ["language", "flashcards"])
    fun findWithLanguageAndFlashcardsById(id: Long): FlashcardSet?

    @EntityGraph(attributePaths = ["language", "chronodays", "dayStreak"])
    fun findWithLanguageAndChronodaysAndDayStreakById(id: Long): FlashcardSet?

    @EntityGraph(attributePaths = ["user", "flashcards"])
    fun findWithUserAndFlashcardsById(id: Long): FlashcardSet?

    @Query("""
        SELECT fs.id AS id, COUNT(f) AS flashcardCount
        FROM FlashcardSet fs
        LEFT JOIN fs.flashcards f
        WHERE fs.user = :user AND fs.status IN :status
        GROUP BY fs.id
    """)
    fun countFlashcardsByUserAndStatusIn(user: User, status: List<FlashcardSetStatus>): List<FlashcardCount>
}
