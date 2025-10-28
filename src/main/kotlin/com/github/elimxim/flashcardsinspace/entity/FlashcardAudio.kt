package com.github.elimxim.flashcardsinspace.entity

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.ZonedDateTime
import kotlin.math.roundToInt

@Entity
@Table(name = "flashcard_audio")
class FlashcardAudio(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var side: FlashcardSide,

    @Column(nullable = true)
    var mimeType: String? = null,

    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(nullable = false)
    var audioData: ByteArray,

    @Column(nullable = false)
    var audioSize: Long,

    @Column(nullable = false)
    var uploadedAt: ZonedDateTime,
)

enum class FlashcardSide {
    FRONT, BACK
}

fun FlashcardAudio.sizeKB(): Int {
    return (audioSize / 1024.0).roundToInt()
}
