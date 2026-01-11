package com.github.elimxim.flashcardsinspace.entity

import jakarta.persistence.*

@Entity
@Table(name = "language")
open class Language(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open val id: Long = 0,

    @Column(nullable = false, unique = true)
    open val name: String,

    @Column(nullable = false, unique = true)
    open val code: String,
)

interface ReadOnlyLanguage {
    fun getId(): Long
    fun getName(): String
    fun getCode(): String
}
