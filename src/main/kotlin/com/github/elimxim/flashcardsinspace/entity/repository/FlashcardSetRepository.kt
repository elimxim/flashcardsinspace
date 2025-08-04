package com.github.elimxim.flashcardsinspace.entity.repository

import com.github.elimxim.flashcardsinspace.entity.FlashcardSet
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FlashcardSetRepository : JpaRepository<FlashcardSet, Long> {
    fun findAllByUserId(userId: Long): List<FlashcardSet>
}