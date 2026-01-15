package com.github.elimxim.flashcardsinspace.security

import com.github.elimxim.flashcardsinspace.entity.User
import com.github.elimxim.flashcardsinspace.util.VERIFICATION_TOKEN_COOKIE
import com.github.elimxim.flashcardsinspace.util.withLoggingContext
import com.github.elimxim.flashcardsinspace.web.dto.VerificationCodeRequest
import com.github.elimxim.flashcardsinspace.web.dto.VerificationIntentRequest
import com.github.elimxim.flashcardsinspace.web.dto.VerificationIntentResponse
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth/verification")
class VerificationCodeController(
    private val verificationCodeService: VerificationCodeService,
) {
    @PostMapping("/request")
    fun requestCode(
        @AuthenticationPrincipal user: User?,
        @CookieValue(name = VERIFICATION_TOKEN_COOKIE, required = false) verificationToken: String?,
        @RequestBody request: VerificationIntentRequest?,
        response: HttpServletResponse,
    ): ResponseEntity<Unit> = withLoggingContext(user) {
        verificationCodeService.send(user, verificationToken, request?.normalize(), response)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/code")
    fun verifyCode(
        @RequestBody request: VerificationCodeRequest,
        @CookieValue(name = VERIFICATION_TOKEN_COOKIE, required = false) verificationToken: String?,
        response: HttpServletResponse,
    ): ResponseEntity<VerificationIntentResponse> = withLoggingContext {
        val response = verificationCodeService.verify(verificationToken, request.normalize(), response)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/context")
    fun getContext(
        @CookieValue(name = VERIFICATION_TOKEN_COOKIE, required = false) verificationToken: String?,
        response: HttpServletResponse,
    ): ResponseEntity<VerificationIntentResponse> = withLoggingContext {
        val response = verificationCodeService.context(verificationToken, response)
        return ResponseEntity.ok(response)
    }
}
