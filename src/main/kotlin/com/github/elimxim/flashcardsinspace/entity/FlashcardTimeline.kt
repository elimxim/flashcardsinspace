package com.github.elimxim.flashcardsinspace.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.time.ZonedDateTime

@Entity
@Table(name = "flashcard_timeline")
class FlashcardTimeline(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    var startedAt: ZonedDateTime,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: TimelineStatus = TimelineStatus.ACTIVE,

    @Column(nullable = true)
    var lastUpdatedAt: ZonedDateTime? = null,

    @OneToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "flashcard_set_id", referencedColumnName = "id")
    var flashcardSet: FlashcardSet,

    @OneToMany(
        mappedBy = "timeline",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
    )
    var chronodays: MutableList<Chronoday> = arrayListOf(),
) {
    fun lastChronoday(): Chronoday? = chronodays.maxByOrNull { it.chronodate }
}

enum class TimelineStatus {
    ACTIVE, SUSPENDED
}