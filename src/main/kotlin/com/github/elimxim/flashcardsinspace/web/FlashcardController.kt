package com.github.elimxim.flashcardsinspace.web

import com.github.elimxim.flashcardsinspace.entity.User
import com.github.elimxim.flashcardsinspace.security.normalize
import com.github.elimxim.flashcardsinspace.service.FlashcardService
import com.github.elimxim.flashcardsinspace.util.withLoggingContext
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardBulkCreationRequest
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardCreationRequest
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardDto
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardUpdateRequest
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
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
        @PageableDefault(size = 256) pageable: Pageable,
    ): ResponseEntity<Page<FlashcardDto>> = withLoggingContext(user) {
        return ResponseEntity.ok(flashcardService.getAll(user, setId, pageable))
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

    @PostMapping("/bulk")
    fun addFlashcardsBulk(
        @AuthenticationPrincipal user: User,
        @PathVariable setId: Long,
        @RequestBody request: FlashcardBulkCreationRequest,
    ): ResponseEntity<List<FlashcardDto>> = withLoggingContext(user) {
        val result = flashcardService.addBulk(user, setId, request.normalize())
        return ResponseEntity.ok(result)
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
