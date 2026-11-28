package com.github.elimxim.flashcardsinspace.entity

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.ZonedDateTime

@Entity
@Table(name = "flashcard_picture_thumbnail")
open class FlashcardPictureThumbnail(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open val id: Long = 0,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    open var side: FlashcardSide,

    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(nullable = false)
    open var thumbnailData: ByteArray,

    @Column(nullable = false)
    open var thumbnailSize: Long,

    @Column(nullable = false)
    open var createdAt: ZonedDateTime,

    @Column(nullable = true)
    open var lastUpdatedAt: ZonedDateTime? = null,

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "flashcard_id", referencedColumnName = "id")
    open var flashcard: Flashcard,

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "flashcard_picture_id", referencedColumnName = "id")
    open var flashcardPicture: FlashcardPicture,
)
