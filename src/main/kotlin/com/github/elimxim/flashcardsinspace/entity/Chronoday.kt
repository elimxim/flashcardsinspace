package com.github.elimxim.flashcardsinspace.entity

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "chronoday")
open class Chronoday(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @field:JsonFormat(pattern = "yyyy-MM-dd")
    @Column(nullable = false)
    var chronodate: LocalDate,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: ChronodayStatus,

    @ManyToOne(optional = false)
    @JoinColumn(name = "flashcard_set_id", referencedColumnName = "id")
    var flashcardSet: FlashcardSet,
)

enum class ChronodayStatus {
    INITIAL, COMPLETED, IN_PROGRESS, NOT_STARTED, OFF
}

fun Chronoday.isInitial() = status == ChronodayStatus.INITIAL
fun Chronoday.isInProgress() = status == ChronodayStatus.IN_PROGRESS
fun Chronoday.isCompleted() = status == ChronodayStatus.COMPLETED
fun Chronoday.isNotStarted() = status == ChronodayStatus.NOT_STARTED
fun Chronoday.isOff() = status == ChronodayStatus.OFF
