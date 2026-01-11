package com.github.elimxim.flashcardsinspace.entity

import jakarta.persistence.*

@Entity
@Table(name = "day_streak")
open class DayStreak(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open val id: Long = 0,

    @Column(nullable = false)
    open var streak: Int,

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "chronoday_id", referencedColumnName = "id")
    open var lastDay: Chronoday,

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "flashcard_set_id", referencedColumnName = "id")
    open var flashcardSet: FlashcardSet,
)
