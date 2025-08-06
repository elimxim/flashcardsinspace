package com.github.elimxim.flashcardsinspace.entity

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDate
import java.time.ZonedDateTime

@Entity
@Table(name = "flashcard")
class Flashcard(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    var frontSide: String,

    @Column(nullable = false)
    var backSide: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var stage: FlashcardStage,

    @Column(nullable = false)
    var reviewedTimes: Int,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = true)
    var reviewHistory: ReviewHistory = ReviewHistory(),

    @Column(nullable = false)
    var creationDate: LocalDate,

    @Column(nullable = true)
    var reviewDate: LocalDate? = null,

    @Column(nullable = true)
    var lastUpdatedAt: ZonedDateTime? = null,

    @ManyToOne(optional = false)
    @JoinColumn(name = "flashcard_set_id", referencedColumnName = "id")
    var flashcardSet: FlashcardSet,
)

enum class FlashcardStage {
    S1, S2, S3, S4, S5, S6, S7
}

data class ReviewHistory(
    val history: MutableList<ReviewInfo> = arrayListOf()
)

data class ReviewInfo(
    val stage: FlashcardStage,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val reviewDate: LocalDate
)