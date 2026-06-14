package com.github.elimxim.flashcardsinspace.entity

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.ZonedDateTime
import kotlin.math.roundToInt

@Entity
@Table(name = "flashcard_picture")
open class FlashcardPicture(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open val id: Long = 0,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    open var side: FlashcardSide,

    @Column(nullable = false)
    open var mimeType: String,

    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(nullable = false)
    open var pictureData: ByteArray,

    @Column(nullable = false)
    open var pictureSize: Long,

    @Column(nullable = false)
    open var width: Int,

    @Column(nullable = false)
    open var height: Int,

    @Column(nullable = false)
    open var uploadedAt: ZonedDateTime,

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "flashcard_id", referencedColumnName = "id")
    open var flashcard: Flashcard,
)

fun FlashcardPicture.sizeKB(): Int {
    return (pictureSize / 1024.0).roundToInt()
}

interface FlashcardPictureMetadata {
    fun getPictureId(): Long
    fun getSide(): FlashcardSide
    fun getFlashcardId(): Long
    fun getWidth(): Int
    fun getHeight(): Int
}
