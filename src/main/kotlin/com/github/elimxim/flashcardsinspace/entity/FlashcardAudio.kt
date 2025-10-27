package com.github.elimxim.flashcardsinspace.entity

import jakarta.persistence.*
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

    @Column(nullable = false)
    var mimeType: String,

    @Lob
    @Column(nullable = false)
    var audioData: ByteArray,

    @Column(nullable = false)
    var audioSize: Long,

    @Column(nullable = false)
    var uploadedAt: ZonedDateTime? = null,
)

enum class FlashcardSide {
    FRONT, BACK
}

fun FlashcardAudio.sizeKB(): Int {
    return (audioSize / 1024.0).roundToInt()
}
