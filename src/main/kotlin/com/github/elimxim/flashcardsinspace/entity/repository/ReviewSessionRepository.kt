package com.github.elimxim.flashcardsinspace.entity.repository

import com.github.elimxim.flashcardsinspace.entity.ReviewSession
import com.github.elimxim.flashcardsinspace.entity.ReviewSessionType
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface ReviewSessionRepository : JpaRepository<ReviewSession, Long> {
    fun findTopByFlashcardSetIdAndTypeOrderByStartedAtDesc(flashcardSetId: Long, type: ReviewSessionType): Optional<ReviewSession>
}
