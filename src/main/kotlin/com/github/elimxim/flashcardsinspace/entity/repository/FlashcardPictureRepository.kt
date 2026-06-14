package com.github.elimxim.flashcardsinspace.entity.repository

import com.github.elimxim.flashcardsinspace.entity.FlashcardPicture
import com.github.elimxim.flashcardsinspace.entity.FlashcardPictureMetadata
import com.github.elimxim.flashcardsinspace.entity.FlashcardSide
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface FlashcardPictureRepository : JpaRepository<FlashcardPicture, Long> {
    fun findByFlashcardIdAndSide(flashcardId: Long, side: FlashcardSide): FlashcardPicture?

    @Query(
        """
        SELECT
            fp.id AS pictureId,
            fp.side AS side,
            fp.flashcard_id AS flashcardId,
            fp.width AS width,
            fp.height AS height
        FROM flashcard_picture fp
        INNER JOIN flashcard f ON fp.flashcard_id = f.id
        WHERE f.flashcard_set_id = :setId
    """, nativeQuery = true
    )
    fun findAllMetadata(@Param("setId") setId: Long): List<FlashcardPictureMetadata>

    @Modifying
    @Query(
        """
        DELETE FROM flashcard_picture fp
        WHERE fp.flashcard_id IN (
            SELECT f.id FROM flashcard f WHERE f.flashcard_set_id = :setId
        )
    """, nativeQuery = true
    )
    fun deleteByFlashcardSetId(@Param("setId") setId: Long)

    @Modifying
    @Query(
        """
        DELETE FROM flashcard_picture fp
        WHERE fp.flashcard_id = :flashcardId
    """, nativeQuery = true
    )
    fun deleteByFlashcardId(@Param("flashcardId") flashcardId: Long)
}
