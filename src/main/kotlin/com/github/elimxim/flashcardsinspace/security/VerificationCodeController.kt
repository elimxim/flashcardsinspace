package com.github.elimxim.flashcardsinspace.security

import com.github.elimxim.flashcardsinspace.entity.User
import com.github.elimxim.flashcardsinspace.util.VERIFICATION_TOKEN_COOKIE
import com.github.elimxim.flashcardsinspace.util.withLoggingContext
import com.github.elimxim.flashcardsinspace.web.dto.ConfirmationCodeRequest
import com.github.elimxim.flashcardsinspace.web.dto.VerificationCodeRequest
import com.github.elimxim.flashcardsinspace.web.dto.VerificationCodeResponse
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth/verification-code")
class VerificationCodeController(
    private val verificationCodeService: VerificationCodeService,
) {
    @PostMapping
    fun sendConfirmationCode(
        @AuthenticationPrincipal user: User?,
        @RequestBody request: ConfirmationCodeRequest,
        response: HttpServletResponse,
    ): ResponseEntity<Unit> = withLoggingContext(user) {
        verificationCodeService.generateAndSend(user, request.normalize(), response)
        return ResponseEntity.ok().build()
    }

    @PutMapping
    fun verifyConfirmationCode(
        @AuthenticationPrincipal user: User?,
        @RequestBody request: VerificationCodeRequest,
        @CookieValue(name = VERIFICATION_TOKEN_COOKIE, required = false) verificationToken: String?,
        response: HttpServletResponse,
    ): ResponseEntity<VerificationCodeResponse> = withLoggingContext(user) {
        val response = verificationCodeService.verify(user, verificationToken, request.normalize(), response)
        return ResponseEntity.ok(response)
    }

    @GetMapping
    fun testConfirmationCode(
        @AuthenticationPrincipal user: User?,
        @CookieValue(name = VERIFICATION_TOKEN_COOKIE, required = false) verificationToken: String?,
        response: HttpServletResponse,
    ): ResponseEntity<VerificationCodeResponse> = withLoggingContext(user) {
        val response = verificationCodeService.context(verificationToken, response)
        return ResponseEntity.ok(response)
    }
}
