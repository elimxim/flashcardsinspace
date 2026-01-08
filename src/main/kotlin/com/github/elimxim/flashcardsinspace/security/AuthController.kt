package com.github.elimxim.flashcardsinspace.security

import com.github.elimxim.flashcardsinspace.entity.User
import com.github.elimxim.flashcardsinspace.util.withLoggingContext
import com.github.elimxim.flashcardsinspace.web.dto.*
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService,
    private val jwtService: JwtService,
    private val confirmationCodeService: ConfirmationCodeService,
) {
    @PostMapping("/signup")
    fun signup(
        @RequestBody request: SignUpRequest,
        response: HttpServletResponse,
    ): ResponseEntity<UserDto> {
        val user = authService.signUp(request.normalize())
        jwtService.setCookies(user, response)
        return ResponseEntity.ok(user.toDto())
    }

    @PostMapping("/login")
    fun login(
        @RequestBody request: LoginRequest,
        response: HttpServletResponse,
    ): ResponseEntity<UserDto> {
        val user = authService.login(request.normalize())
        jwtService.setCookies(user, response)
        return ResponseEntity.ok(user.toDto())
    }

    @PostMapping("/logout")
    fun logout(response: HttpServletResponse): ResponseEntity<Unit> {
        authService.logout()
        jwtService.clearCookies(response)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/refresh")
    fun refresh(
        @CookieValue(name = "refreshToken", required = false) refreshToken: String?,
        response: HttpServletResponse,
    ): ResponseEntity<UserDto> {
        val user = authService.refreshToken(refreshToken)
        return if (user != null) {
            jwtService.setCookies(user, response)
            ResponseEntity.ok(user.toDto())
        } else {
            // consider it as expired - 401
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
    }

    @PostMapping("/send-code")
    fun sendConfirmationCode(
        @AuthenticationPrincipal user: User?,
        @RequestBody request: ConfirmationCodeRequest,
    ): ResponseEntity<Unit> = withLoggingContext(user) {
        confirmationCodeService.generateAndSend(user, request.normalize())
        return ResponseEntity.ok().build()
    }

    @PostMapping("/verify-code")
    fun verifyConfirmationCode(
        @AuthenticationPrincipal user: User?,
        @RequestBody request: ConfirmationCodeVerificationRequest,
    ): ResponseEntity<ConfirmationCodeResponse> = withLoggingContext(user) {
        val response = confirmationCodeService.verify(user, request.normalize())
        return ResponseEntity.ok(response)
    }

    @PostMapping("/test-code")
    fun testConfirmationCode(
        @AuthenticationPrincipal user: User?,
        @RequestBody request: ConfirmationCodeTestRequest,
    ): ResponseEntity<ConfirmationCodeResponse> = withLoggingContext(user) {
        val response = confirmationCodeService.test(user, request.normalize())
        return ResponseEntity.ok(response)
    }
}
