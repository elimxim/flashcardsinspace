package com.github.elimxim.flashcardsinspace.security

import com.github.elimxim.flashcardsinspace.util.PASSWORD_RESET_TOKEN_COOKIE
import com.github.elimxim.flashcardsinspace.util.REFRESH_TOKEN_COOKIE
import com.github.elimxim.flashcardsinspace.util.withLoggingContext
import com.github.elimxim.flashcardsinspace.web.dto.*
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
class AuthController(private val authService: AuthService) {
    @PostMapping("/signup")
    fun signup(
        @RequestBody request: SignUpRequest,
        response: HttpServletResponse,
    ): ResponseEntity<UserDto> {
        val user = authService.signUp(request.normalize(), response)
        return ResponseEntity.ok(user.toDto())
    }

    @PostMapping("/login")
    fun login(
        @RequestBody request: LoginRequest,
        response: HttpServletResponse,
    ): ResponseEntity<UserDto> {
        val user = authService.login(request.normalize(), response)
        return ResponseEntity.ok(user.toDto())
    }

    @PostMapping("/logout")
    fun logout(response: HttpServletResponse): ResponseEntity<Unit> = withLoggingContext {
        authService.logout(response)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/refresh")
    fun refresh(
        @CookieValue(name = REFRESH_TOKEN_COOKIE, required = false) refreshToken: String?,
        response: HttpServletResponse,
    ): ResponseEntity<UserDto> {
        val user = authService.refreshToken(refreshToken, response)
        return if (user != null) {
            ResponseEntity.ok(user.toDto())
        } else {
            // consider it as expired - 401
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
    }

    @PostMapping("/password-reset")
    fun resetPassword(
        @CookieValue(name = PASSWORD_RESET_TOKEN_COOKIE, required = false) resetToken: String?,
        @RequestBody request: PasswordResetRequest,
        response: HttpServletResponse,
    ): ResponseEntity<Unit> = withLoggingContext {
        authService.resetPassword(resetToken, request, response)
        return ResponseEntity.ok().build()
    }
}
