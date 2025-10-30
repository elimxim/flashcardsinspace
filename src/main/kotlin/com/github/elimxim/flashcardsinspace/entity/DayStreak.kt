package com.github.elimxim.flashcardsinspace.entity

import jakarta.persistence.*

@Entity
@Table(name = "day_streak")
open class DayStreak(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    var streak: Int,

    @OneToOne
    @JoinColumn(name = "chronoday_id", referencedColumnName = "id")
    var lastDay: Chronoday,

    @OneToOne
    @JoinColumn(name = "flashcard_set_id", referencedColumnName = "id")
    var flashcardSet: FlashcardSet,
)
