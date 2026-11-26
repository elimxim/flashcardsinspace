package com.github.elimxim.flashcardsinspace.entity.repository

import com.github.elimxim.flashcardsinspace.entity.FlashcardPictureThumbnail
import com.github.elimxim.flashcardsinspace.entity.FlashcardSide
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface FlashcardPictureThumbnailRepository : JpaRepository<FlashcardPictureThumbnail, Long> {
    fun findByFlashcardIdAndSide(flashcardId: Long, side: FlashcardSide): FlashcardPictureThumbnail?

    @Query(
        """
        SELECT t FROM FlashcardPictureThumbnail t
        WHERE t.flashcard.flashcardSet.id = :setId
          AND t.flashcard.id IN :flashcardIds
          AND t.side = :side
    """
    )
    fun findAllInSet(
        @Param("setId") setId: Long,
        @Param("flashcardIds") flashcardIds: Collection<Long>,
        @Param("side") side: FlashcardSide,
    ): List<FlashcardPictureThumbnail>

    @Modifying
    @Query(
        """
        DELETE FROM flashcard_picture_thumbnail t
        WHERE t.flashcard_id = :flashcardId AND t.side = :side
    """, nativeQuery = true
    )
    fun deleteByFlashcardIdAndSide(@Param("flashcardId") flashcardId: Long, @Param("side") side: String)

    @Modifying
    @Query(
        """
        DELETE FROM flashcard_picture_thumbnail t
        WHERE t.flashcard_id IN (
            SELECT f.id FROM flashcard f WHERE f.flashcard_set_id = :setId
        )
    """, nativeQuery = true
    )
    fun deleteByFlashcardSetId(@Param("setId") setId: Long)

    @Modifying
    @Query(
        """
        DELETE FROM flashcard_picture_thumbnail t
        WHERE t.flashcard_id IN (:flashcardIds)
    """, nativeQuery = true
    )
    fun deleteByFlashcardIdIn(@Param("flashcardIds") flashcardIds: Collection<Long>)
}
