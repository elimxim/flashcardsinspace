package com.github.elimxim.flashcardsinspace.entity

import jakarta.persistence.*
import java.time.ZonedDateTime

@Entity
@Table(name = "confirmation_code")
open class ConfirmationCode(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open val id: Long = 0,

    @Column(nullable = false)
    open val email: String,

    @Column(nullable = false)
    open val code: String,

    @Column(nullable = false)
    open var attempts: Int = 0,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    open var purpose: ConfirmationPurpose,

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
    open var user: User? = null,
)

enum class ConfirmationPurpose {
    EMAIL_VERIFICATION,
    EMAIL_CHANGE,
    PASSWORD_RESET,
    ACCOUNT_DELETION
}

