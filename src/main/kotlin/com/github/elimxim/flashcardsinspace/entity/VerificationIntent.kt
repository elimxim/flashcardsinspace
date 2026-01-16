package com.github.elimxim.flashcardsinspace.entity

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.ZonedDateTime

@Entity
@Table(name = "verification_intent")
open class VerificationIntent(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open val id: Long = 0,

    @Column(nullable = false)
    open val email: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    open var type: VerificationType,

    @Column(nullable = false, columnDefinition = "CHAR(64)")
    @JdbcTypeCode(SqlTypes.CHAR)
    open val tokenHash: String,

    @Column(nullable = true)
    open val code: String? = null,

    @Column(nullable = false)
    open var attempts: Int = 0,

    @Column(nullable = false)
    open val createdAt: ZonedDateTime,

    @Column(nullable = false)
    open val expiresAt: ZonedDateTime,

    @Column(nullable = true)
    open var usedAt: ZonedDateTime? = null,

    @Column(nullable = true)
    open var lastUpdatedAt: ZonedDateTime? = null,

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    open var user: User,
)

enum class VerificationType {
    REGISTRATION_REQUEST,
    PASSWORD_RESET_REQUEST,
    PASSWORD_RESET_ACCESS,
}

