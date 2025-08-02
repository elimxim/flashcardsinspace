package com.github.elimxim.flashcardsinspace.web.dto

import jakarta.validation.constraints.NotBlank

// todo kotlin way for jakarta annotations?
data class SignUpRequest(
    @field:NotBlank val email: String,
    @field:NotBlank val secret: String,
    @field:NotBlank val name: String,
)

data class LoginRequest(
    @field:NotBlank val email: String,
    @field:NotBlank val secret: String
)

data class UserResponse(
    val user: UserDto
)

