package com.github.elimxim.flashcardsinspace.web

import com.github.elimxim.flashcardsinspace.entity.User
import com.github.elimxim.flashcardsinspace.service.ConfirmationCodeService
import com.github.elimxim.flashcardsinspace.util.withLoggingContext
import com.github.elimxim.flashcardsinspace.web.dto.SendConfirmationCodeRequest
import com.github.elimxim.flashcardsinspace.web.dto.VerifyConfirmationCodeRequest
import com.github.elimxim.flashcardsinspace.web.dto.VerifyConfirmationCodeResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/confirmation-codes")
class ConfirmationController(
    private val confirmationCodeService: ConfirmationCodeService,
) {
    @PostMapping
    fun sendConfirmationCode(
        @AuthenticationPrincipal user: User,
        @RequestBody request: SendConfirmationCodeRequest,
    ): ResponseEntity<Unit> = withLoggingContext(user) {
        confirmationCodeService.generateAndSend(user, request)
        return ResponseEntity.ok().build()
    }

    @PutMapping
    fun verifyConfirmationCode(
        @AuthenticationPrincipal user: User,
        @RequestBody request: VerifyConfirmationCodeRequest,
    ): ResponseEntity<VerifyConfirmationCodeResponse> = withLoggingContext(user) {
        val response = confirmationCodeService.verify(user, request)
        return ResponseEntity.ok(response)
    }
}
