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
@RequestMapping("/auth/code")
class VerificationCodeController(
    private val verificationCodeService: VerificationCodeService,
) {
    @PostMapping("/confirmation")
    fun confirmation(
        @AuthenticationPrincipal user: User?,
        @CookieValue(name = VERIFICATION_TOKEN_COOKIE, required = false) verificationToken: String?,
        @RequestBody request: ConfirmationCodeRequest?,
        response: HttpServletResponse,
    ): ResponseEntity<Unit> = withLoggingContext(user) {
        verificationCodeService.send(user, verificationToken, request?.normalize(), response)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/verification")
    fun verification(
        @AuthenticationPrincipal user: User?,
        @RequestBody request: VerificationCodeRequest,
        @CookieValue(name = VERIFICATION_TOKEN_COOKIE, required = false) verificationToken: String?,
        response: HttpServletResponse,
    ): ResponseEntity<VerificationCodeResponse> = withLoggingContext(user) {
        val response = verificationCodeService.verify(verificationToken, request.normalize(), response)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/context")
    fun codeContext(
        @AuthenticationPrincipal user: User?,
        @CookieValue(name = VERIFICATION_TOKEN_COOKIE, required = false) verificationToken: String?,
        response: HttpServletResponse,
    ): ResponseEntity<VerificationCodeResponse> = withLoggingContext(user) {
        val response = verificationCodeService.context(verificationToken, response)
        return ResponseEntity.ok(response)
    }
}
