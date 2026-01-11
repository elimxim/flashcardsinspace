package com.github.elimxim.flashcardsinspace.entity

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDate
import java.time.ZonedDateTime

@Entity
@Table(name = "flashcard")
open class Flashcard(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open val id: Long = 0,

    @Column(nullable = false)
    open var frontSide: String,

    @Column(nullable = false)
    open var backSide: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    open var stage: FlashcardStage,

    @Column(nullable = false)
    open var timesReviewed: Int,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = true)
    open var reviewHistory: ReviewHistory = ReviewHistory(),

    @Column(nullable = false)
    open var creationDate: LocalDate,

    @Column(nullable = true)
    open var lastReviewDate: LocalDate? = null,

    @Column(nullable = true)
    open var lastUpdatedAt: ZonedDateTime? = null,

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "flashcard_set_id", referencedColumnName = "id")
    open var flashcardSet: FlashcardSet,
)

const val TERMINAL_STAGE_NAME = "OUTER_SPACE"
enum class FlashcardStage {
    S1, S2, S3, S4, S5, S6, S7, OUTER_SPACE
}

enum class FlashcardSide {
    FRONT, BACK
}

data class ReviewHistory(
    val history: MutableList<ReviewInfo> = arrayListOf()
)

data class ReviewInfo(
    val stage: FlashcardStage,
    @field:JsonFormat(pattern = "yyyy-MM-dd")
    val reviewDate: LocalDate
)
