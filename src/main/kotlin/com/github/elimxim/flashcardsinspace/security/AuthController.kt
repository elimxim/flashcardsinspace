package com.github.elimxim.flashcardsinspace.security

import com.github.elimxim.flashcardsinspace.web.dto.LoginRequest
import com.github.elimxim.flashcardsinspace.web.dto.SignUpRequest
import com.github.elimxim.flashcardsinspace.web.dto.UserResponse
import com.github.elimxim.flashcardsinspace.web.dto.toDto
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService,
    private val jwtService: JwtService,
) {
    @PostMapping("/signup")
    fun signup(
        @RequestBody request: SignUpRequest,
        response: HttpServletResponse,
    ): ResponseEntity<UserResponse> {
        val user = authService.signUp(request)
        jwtService.setCookies(user, response)
        return ResponseEntity.ok(UserResponse(user.toDto()))
    }

    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginRequest,
        response: HttpServletResponse,
    ): ResponseEntity<UserResponse> {
        val user = authService.login(request)
        jwtService.setCookies(user, response)
        return ResponseEntity.ok(UserResponse(user.toDto()))
    }

    // todo password reset

    @PostMapping("/logout")
    fun logout(response: HttpServletResponse): ResponseEntity<Unit> {
        authService.logout()
        jwtService.clearCookies(response)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/refresh")
    fun refresh(
        @CookieValue(name = "refreshToken") refreshToken: String,
        response: HttpServletResponse,
    ): ResponseEntity<UserResponse> {
        val user = authService.refreshToken(refreshToken)
        jwtService.setCookies(user, response)
        return ResponseEntity.ok(UserResponse(user.toDto()))
    }
}