package com.github.elimxim.flashcardsinspace.entity

import jakarta.persistence.*
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.time.ZonedDateTime

@Entity
@Table(name = "astro_user")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    var email: String,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false)
    var secret: String,

    @Column(nullable = false)
    var roles: String,

    @Column(nullable = false)
    var registeredAt: ZonedDateTime,
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