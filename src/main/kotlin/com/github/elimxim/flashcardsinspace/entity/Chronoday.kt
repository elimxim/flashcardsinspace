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
import java.time.LocalDate

@Entity
@Table(name = "chronoday")
class Chronoday(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Column(nullable = false)
    var chronodate: LocalDate,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: DayStatus,

    @ManyToOne(optional = false)
    @JoinColumn(name = "flashcard_timeline_id", referencedColumnName = "id")
    var timeline: FlashcardTimeline,
)

enum class DayStatus {
    INITIAL, COMPLETED, IN_PROGRESS, NOT_STARTED, OFF
}