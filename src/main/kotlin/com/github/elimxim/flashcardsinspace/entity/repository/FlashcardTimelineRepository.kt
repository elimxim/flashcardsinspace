package com.github.elimxim.flashcardsinspace.entity.repository

import com.github.elimxim.flashcardsinspace.entity.FlashcardTimeline
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FlashcardTimelineRepository : JpaRepository<FlashcardTimeline, Long> {
}