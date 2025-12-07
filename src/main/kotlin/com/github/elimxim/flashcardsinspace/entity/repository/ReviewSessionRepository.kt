package com.github.elimxim.flashcardsinspace.entity.repository

import com.github.elimxim.flashcardsinspace.entity.ReviewSession
import org.springframework.data.jpa.repository.JpaRepository

interface ReviewSessionRepository : JpaRepository<ReviewSession, Long> {
}
