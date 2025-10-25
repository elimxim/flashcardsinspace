package com.github.elimxim.flashcardsinspace.web

import com.github.elimxim.flashcardsinspace.entity.User
import com.github.elimxim.flashcardsinspace.security.normalize
import com.github.elimxim.flashcardsinspace.service.FlashcardSetInitService
import com.github.elimxim.flashcardsinspace.util.withLoggingContext
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardCreationRequest
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardSetInitResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/api/flashcard-sets/{id:\\d+}/init")
class FlashcardSetInitController(
    private val flashcardSetInitService: FlashcardSetInitService,
) {

    @PostMapping
    fun initFlashcardSet(
        @AuthenticationPrincipal user: User,
        @PathVariable id: Long,
        @RequestBody request: FlashcardCreationRequest,
    ): ResponseEntity<FlashcardSetInitResponse> = withLoggingContext(user) {
        val response = flashcardSetInitService.init(user, id, request.normalize())
        return ResponseEntity.ok(response)
    }
}
