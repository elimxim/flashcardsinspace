package com.github.elimxim.flashcardsinspace.entity

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.ZonedDateTime

@Entity
@Table(name = "review_session")
open class ReviewSession(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open val id: Long = 0,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    open var type: ReviewSessionType,

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "BIGINT[]", nullable = true)
    open var flashcardIds: LongArray? = null,

    @Column(nullable = false)
    open var elapsedTime: Long = 0,

    @Column(nullable = false)
    open var startedAt: ZonedDateTime,

    @Column(nullable = true)
    open var finishedAt: ZonedDateTime? = null,

    @Column(nullable = true)
    open var lastUpdatedAt: ZonedDateTime? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSONB", nullable = true)
    open var metadata: ReviewSessionMetadata? = null,

    @Column(nullable = true)
    open var parentSessionId: Long? = null,

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "flashcard_set_id", referencedColumnName = "id")
    open var flashcardSet: FlashcardSet,

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "chronoday_id", referencedColumnName = "id")
    open var reviewDay: Chronoday,
)

enum class ReviewSessionType {
    LIGHTSPEED, UNKNOWN, ATTEMPTED, SPACE, QUIZ
}

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = QuizMetadata::class, name = "QUIZ"),
)
sealed class ReviewSessionMetadata

data class QuizMetadata(
    @field:MetadataField("round")
    var round: Int,
    @field:MetadataField("nextRoundFlashcardIds")
    var nextRoundFlashcardIds: MutableSet<Long> = mutableSetOf(),
    @field:MetadataField("currRoundFlashcardIds")
    var currRoundFlashcardIds: MutableSet<Long> = mutableSetOf(),
    @field:MetadataField("overallCorrectCount")
    var overallCorrectCount: Int,
    @field:MetadataField("overallTotalCount")
    var overallTotalCount: Int,
) : ReviewSessionMetadata()

@Retention(AnnotationRetention.RUNTIME)
annotation class MetadataField(val name: String)
