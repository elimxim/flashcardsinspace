package com.github.elimxim.flashcardsinspace.web

import com.github.elimxim.flashcardsinspace.entity.User
import com.github.elimxim.flashcardsinspace.security.normalize
import com.github.elimxim.flashcardsinspace.service.FlashcardService
import com.github.elimxim.flashcardsinspace.util.withLoggingContext
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardCreationRequest
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardDto
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardUpdateRequest
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/api/flashcard-sets/{setId:\\d+}/flashcards")
class FlashcardController(
    private val flashcardService: FlashcardService,
) {
    @GetMapping
    fun getFlashcards(
        @AuthenticationPrincipal user: User,
        @PathVariable setId: Long,
    ): ResponseEntity<List<FlashcardDto>> = withLoggingContext(user) {
        val result = flashcardService.getAll(user, setId)
        return ResponseEntity.ok(result)
    }

    @PostMapping
    fun addFlashcard(
        @AuthenticationPrincipal user: User,
        @PathVariable setId: Long,
        @RequestBody request: FlashcardCreationRequest,
    ): ResponseEntity<FlashcardDto> = withLoggingContext(user) {
        val dto = flashcardService.add(user, setId, request.normalize())
        return ResponseEntity.ok(dto)
    }

    @PutMapping("/{id:\\d+}")
    fun updateFlashcard(
        @AuthenticationPrincipal user: User,
        @PathVariable setId: Long,
        @PathVariable id: Long,
        @RequestBody request: FlashcardUpdateRequest,
    ): ResponseEntity<FlashcardDto> = withLoggingContext(user) {
        val dto = flashcardService.update(user, setId, id, request.normalize())
        return ResponseEntity.ok(dto)
    }

    @DeleteMapping("/{id:\\d+}")
    fun removeFlashcard(
        @AuthenticationPrincipal user: User,
        @PathVariable setId: Long,
        @PathVariable id: Long,
    ): ResponseEntity<Unit> = withLoggingContext(user) {
        flashcardService.remove(user, setId, id)
        return ResponseEntity.ok().build()
    }
}
