package com.github.elimxim.flashcardsinspace.web

import com.github.elimxim.flashcardsinspace.entity.User
import com.github.elimxim.flashcardsinspace.security.normalize
import com.github.elimxim.flashcardsinspace.service.FlashcardSetSuspendService
import com.github.elimxim.flashcardsinspace.util.withLoggingContext
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardSetSuspendResponse
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardSetUpdateRequest
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/api/flashcard-sets/{id:\\d+}/suspend")
class FlashcardSetSuspendController(
    private val flashcardSetSuspendService: FlashcardSetSuspendService,
) {

    @PostMapping
    fun initFlashcardSet(
        @AuthenticationPrincipal user: User,
        @PathVariable id: Long,
        @RequestBody request: FlashcardSetUpdateRequest,
    ): ResponseEntity<FlashcardSetSuspendResponse> = withLoggingContext(user) {
        val response = flashcardSetSuspendService.suspend(user, id, request.normalize())
        return ResponseEntity.ok(response)
    }
}
