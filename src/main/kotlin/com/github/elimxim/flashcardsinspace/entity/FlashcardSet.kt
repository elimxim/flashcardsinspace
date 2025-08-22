package com.github.elimxim.flashcardsinspace.entity

import jakarta.persistence.*
import java.time.ZonedDateTime

@Entity
@Table(name = "flashcard_set")
class FlashcardSet(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    var name: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: FlashcardSetStatus = FlashcardSetStatus.ACTIVE,

    @Column(nullable = false)
    var first: Boolean = false,

    @Column(nullable = false)
    var createdAt: ZonedDateTime,

    @Column(nullable = false)
    var startedAt: ZonedDateTime? = null,

    @Column(nullable = true)
    var lastUpdatedAt: ZonedDateTime? = null,

    @ManyToOne(optional = false)
    @JoinColumn(name = "language_id", referencedColumnName = "id")
    var language: Language,

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    var user: User,

    @OneToMany(
        mappedBy = "flashcardSet",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
    )
    var flashcards: MutableList<Flashcard> = arrayListOf(),

    @OneToMany(
        mappedBy = "flashcardSet",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
    )
    @OrderBy("chronodate ASC")
    var chronodays: MutableList<Chronoday> = arrayListOf(),
)

enum class FlashcardSetStatus {
    ACTIVE, DELETED, SUSPENDED
}

fun FlashcardSet.lastChronoday(): Chronoday? =
    chronodays.maxByOrNull { it.chronodate } // fixme can't be null