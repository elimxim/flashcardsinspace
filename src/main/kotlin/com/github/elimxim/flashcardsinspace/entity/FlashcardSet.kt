package com.github.elimxim.flashcardsinspace.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.LocalDate
import java.time.ZonedDateTime

@Entity
@Table(name = "flashcard_set")
data class FlashcardSet(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    var name: String,

    @ManyToOne(optional = false)
    @JoinColumn(name = "language_id", referencedColumnName = "id")
    var language: Language,

    @Column(nullable = false)
    var first: Boolean = false,

    @Column(nullable = false)
    var creationDate: LocalDate,

    @Column(nullable = true)
    var lastUpdatedAt: ZonedDateTime? = null,

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    var user: User,

    @OneToMany(
        mappedBy = "flashcardSet",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        )
    val flashcards: MutableList<Flashcard> = arrayListOf(),
)