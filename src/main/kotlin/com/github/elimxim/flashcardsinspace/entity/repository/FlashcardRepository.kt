package com.github.elimxim.flashcardsinspace.entity.repository

import com.github.elimxim.flashcardsinspace.entity.Flashcard
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface FlashcardRepository : JpaRepository<Flashcard, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT f FROM Flashcard f WHERE f.id = :id")
    fun findByIdWithLock(id: Long): Optional<Flashcard>

    @Modifying
    @Query("UPDATE Flashcard f SET f.frontSideAudioId = :audioId WHERE f.id = :flashcardId")
    fun updateFrontSideAudioId(flashcardId: Long, audioId: Long): Int

    @Modifying
    @Query("UPDATE Flashcard f SET f.backSideAudioId = :audioId WHERE f.id = :flashcardId")
    fun updateBackSideAudioId(flashcardId: Long, audioId: Long): Int
}
