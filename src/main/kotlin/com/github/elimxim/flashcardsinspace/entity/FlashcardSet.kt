package com.github.elimxim.flashcardsinspace.entity

import jakarta.persistence.*
import java.time.ZonedDateTime

@Entity
@Table(name = "flashcard_set")
open class FlashcardSet(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open val id: Long = 0,

    @Column(nullable = false)
    open var name: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    open var status: FlashcardSetStatus = FlashcardSetStatus.ACTIVE,

    @Column(nullable = false)
    open var createdAt: ZonedDateTime,

    @Column(nullable = false)
    open var startedAt: ZonedDateTime? = null,

    @Column(nullable = true)
    open var lastUpdatedAt: ZonedDateTime? = null,

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "language_id", referencedColumnName = "id")
    open var language: Language,

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    open var user: User,

    @OneToMany(
        mappedBy = "flashcardSet",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.LAZY,
    )
    open var flashcards: MutableList<Flashcard> = mutableListOf(),

    @OneToMany(
        mappedBy = "flashcardSet",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.LAZY,
    )
    open var chronodays: MutableList<Chronoday> = mutableListOf(),

    @OneToOne(
        mappedBy = "flashcardSet",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.LAZY,
    )
    open var dayStreak: DayStreak? = null,
)

enum class FlashcardSetStatus {
    ACTIVE, DELETED, SUSPENDED
}

interface FlashcardCount {
    fun getId(): Long
    fun getFlashcardCount(): Int
}

fun FlashcardSet.lastChronoday(): Chronoday? =
    chronodays.maxByOrNull { it.chronodate }

fun FlashcardSet.isSuspended(): Boolean =
    status == FlashcardSetStatus.SUSPENDED
