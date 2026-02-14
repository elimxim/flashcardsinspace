package com.github.elimxim.flashcardsinspace.entity.repository

import com.github.elimxim.flashcardsinspace.entity.FlashcardAudio
import com.github.elimxim.flashcardsinspace.entity.FlashcardAudioMetadata
import com.github.elimxim.flashcardsinspace.entity.FlashcardSide
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface FlashcardAudioRepository : JpaRepository<FlashcardAudio, Long> {
    fun findByFlashcardIdAndSide(flashcardId: Long, side: FlashcardSide): FlashcardAudio?

    @Query(
        """
        SELECT
            fa.id AS audioId,
            fa.side AS side,
            fa.flashcard_id AS flashcardId
        FROM flashcard_audio fa
        INNER JOIN flashcard f ON fa.flashcard_id = f.id
        WHERE f.flashcard_set_id = :setId
    """, nativeQuery = true
    )
    fun findAllMetadata(@Param("setId") setId: Long): List<FlashcardAudioMetadata>

    @Modifying
    @Query(
        """
        DELETE FROM flashcard_audio fa
        WHERE fa.flashcard_id IN (
            SELECT f.id FROM flashcard f WHERE f.flashcard_set_id = :setId
        )
    """, nativeQuery = true
    )
    fun deleteByFlashcardSetId(@Param("setId") setId: Long)

    @Modifying
    @Query(
        """
        DELETE FROM flashcard_audio fa
        WHERE fa.flashcard_id = :flashcardId
    """, nativeQuery = true
    )
    fun deleteByFlashcardId(@Param("flashcardId") flashcardId: Long)
}
