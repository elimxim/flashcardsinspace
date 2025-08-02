package com.github.elimxim.flashcardsinspace.security

import com.github.elimxim.flashcardsinspace.entity.User
import com.github.elimxim.flashcardsinspace.web.dto.LoginRequest
import com.github.elimxim.flashcardsinspace.web.dto.SignUpRequest
import com.github.elimxim.flashcardsinspace.entity.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.ZonedDateTime

private val log = LoggerFactory.getLogger(AuthService::class.java)

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val authenticationManager: AuthenticationManager,
) {

    // todo SignUpResponse with errors instead of throwing exceptions
    // todo signup journal
    // todo more logs
    fun signUp(request: SignUpRequest): User {
        // todo validate request fields
        if (userRepository.findByEmail(request.email).isPresent) {
            log.error("Email {} is already taken", request.email)
            throw IllegalArgumentException("Email already exists")
        }

        val user = User(
            email = request.email,
            name = request.name,
            secret = passwordEncoder.encode(request.secret),
            roles = "ASTRONAUT", // todo role model
            registeredAt = ZonedDateTime.now(),
        )

        val savedUser = userRepository.save(user)
        // todo emailService.sendWelcomeEmail(savedUser)
        return savedUser
    }

    // todo LoginResponse with errors instead of throwing exceptions
    // todo login journal
    // todo more logs
    fun login(request: LoginRequest): User {
        val token = UsernamePasswordAuthenticationToken(request.email, request.secret)
        authenticationManager.authenticate(token)

        val user = userRepository.findByEmail(request.email)
            .orElseThrow { UsernameNotFoundException("User not found") }

        return user
    }

    fun logout() {
        // todo log
    }

    // todo RefreshTokenResponse with errors
    // todo more logs
    fun refreshToken(refreshToken: String): User {
        val email = jwtService.extractUsername(refreshToken)
        val user = userRepository.findByEmail(email)
            .orElseThrow { UsernameNotFoundException("User not found") }

        if (!jwtService.isTokenValid(refreshToken, user)) {
            log.error("Invalid refresh token for user {}", email)
            throw IllegalArgumentException("Invalid refresh token")
        }

        return user
    }
}