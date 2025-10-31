package com.github.elimxim.flashcardsinspace.entity

import jakarta.persistence.*
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.time.ZonedDateTime

@Entity
@Table(name = "astro_user")
open class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open val id: Long = 0,

    @Column(nullable = false, unique = true)
    open var email: String,

    @Column(nullable = false)
    open var name: String,

    @Column(nullable = false)
    open var secret: String,

    @Column(nullable = false)
    open var roles: String,

    @Column(nullable = false)
    open var registeredAt: ZonedDateTime,

    @Column(nullable = true)
    open var lastLoginAt: ZonedDateTime? = null,

    @Column(nullable = true)
    open var lastUpdatedAt: ZonedDateTime? = null,

    @Column(nullable = false)
    open var timezone: String = "UTC",

    @ManyToOne(optional = false)
    @JoinColumn(name = "language_id", referencedColumnName = "id")
    open var language: Language,

    @OneToMany(
        mappedBy = "user",
        cascade = [CascadeType.ALL],
        fetch = FetchType.LAZY,
    )
    open var flashcardSets: MutableList<FlashcardSet> = mutableListOf(),
) : UserDetails {
    override fun getAuthorities(): Collection<GrantedAuthority> =
        roles.split(",").map { SimpleGrantedAuthority(it.trim()) }

    override fun getPassword(): String = secret
    override fun getUsername(): String = email
    override fun isAccountNonExpired(): Boolean = true
    override fun isAccountNonLocked(): Boolean = true
    override fun isCredentialsNonExpired(): Boolean = true
    override fun isEnabled(): Boolean = true
}
