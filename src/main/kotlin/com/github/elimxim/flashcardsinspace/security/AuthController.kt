package com.github.elimxim.flashcardsinspace.security

import com.github.elimxim.flashcardsinspace.web.dto.LoginRequest
import com.github.elimxim.flashcardsinspace.web.dto.SignUpRequest
import com.github.elimxim.flashcardsinspace.web.dto.UserDto
import com.github.elimxim.flashcardsinspace.web.dto.toDto
import jakarta.servlet.http.HttpServletResponse
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
        @CookieValue(name = "refreshToken") refreshToken: String,
        response: HttpServletResponse,
    ): ResponseEntity<UserDto> {
        val user = authService.refreshToken(refreshToken)
        jwtService.setCookies(user, response)
        return ResponseEntity.ok(user.toDto())
    }
}
