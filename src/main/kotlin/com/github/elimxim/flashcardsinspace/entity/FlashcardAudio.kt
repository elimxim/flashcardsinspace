package com.github.elimxim.flashcardsinspace.entity

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.ZonedDateTime
import kotlin.math.roundToInt

@Entity
@Table(name = "flashcard_audio")
open class FlashcardAudio(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open val id: Long = 0,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    open var side: FlashcardSide,

    @Column(nullable = true)
    open var mimeType: String? = null,

    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(nullable = false)
    open var audioData: ByteArray,

    @Column(nullable = false)
    open var audioSize: Long,

    @Column(nullable = false)
    open var uploadedAt: ZonedDateTime,

    @ManyToOne(
        optional = false,
        fetch = FetchType.LAZY,
        cascade = [CascadeType.ALL]
    )
    @JoinColumn(name = "flashcard_id", referencedColumnName = "id")
    open var flashcard: Flashcard,
)

fun FlashcardAudio.sizeKB(): Int {
    return (audioSize / 1024.0).roundToInt()
}

data class FlashcardAudioMetadata(
    val audioId: Long,
    val flashcardSide: FlashcardSide,
    val flashcardId: Long,
)
