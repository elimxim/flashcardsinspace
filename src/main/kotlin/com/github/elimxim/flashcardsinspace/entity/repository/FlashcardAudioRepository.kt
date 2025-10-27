package com.github.elimxim.flashcardsinspace.entity.repository

import com.github.elimxim.flashcardsinspace.entity.FlashcardAudio
import org.springframework.data.jpa.repository.JpaRepository

interface FlashcardAudioRepository : JpaRepository<FlashcardAudio, Long> {
}
