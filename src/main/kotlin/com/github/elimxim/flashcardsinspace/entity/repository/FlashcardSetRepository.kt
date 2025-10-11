package com.github.elimxim.flashcardsinspace.entity.repository

import com.github.elimxim.flashcardsinspace.entity.FlashcardSet
import com.github.elimxim.flashcardsinspace.entity.FlashcardSetStatus
import com.github.elimxim.flashcardsinspace.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FlashcardSetRepository : JpaRepository<FlashcardSet, Long> {
    fun findAllByUser(user: User): List<FlashcardSet>
    fun findAllByUserAndStatusIn(user: User, status: List<FlashcardSetStatus>): List<FlashcardSet>
}
