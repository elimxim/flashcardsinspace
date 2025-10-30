package com.github.elimxim.flashcardsinspace.entity

import jakarta.persistence.*

@Entity
@Table(name = "language")
open class Language(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    val name: String,

    @Column(nullable = false, unique = true)
    val code: String,
)
