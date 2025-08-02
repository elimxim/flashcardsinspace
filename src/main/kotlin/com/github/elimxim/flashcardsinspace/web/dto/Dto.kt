package com.github.elimxim.flashcardsinspace.web.dto

data class UserDto(
    val id: Long,
    val email: String,
    val name: String,
    val roles: List<String>,
    val registeredAt: String,
)