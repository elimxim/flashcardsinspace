package com.github.elimxim.flashcardsinspace.web

import com.github.elimxim.flashcardsinspace.entity.User
import com.github.elimxim.flashcardsinspace.security.normalize
import com.github.elimxim.flashcardsinspace.service.FlashcardService
import com.github.elimxim.flashcardsinspace.util.withLoggingContext
import com.github.elimxim.flashcardsinspace.web.dto.*
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
    ): ResponseEntity<PageDto<FlashcardDto>> = withLoggingContext(user) {
        return ResponseEntity.ok(flashcardService.getAll(user, setId, pageable).toDto())
    }

    @PostMapping
    fun addFlashcard(
        @AuthenticationPrincipal user: User,
        @PathVariable setId: Long,
        @RequestBody request: FlashcardCreationRequest,
    ): ResponseEntity<FlashcardCreationResponse> = withLoggingContext(user) {
        val dto = flashcardService.add(user, setId, request.normalize())
        return ResponseEntity.ok(dto)
    }

    @PostMapping("/bulk")
    fun addFlashcardsBulk(
        @AuthenticationPrincipal user: User,
        @PathVariable setId: Long,
        @RequestBody request: FlashcardBulkCreationRequest,
    ): ResponseEntity<FlashcardCreationResponse> = withLoggingContext(user) {
        val result = flashcardService.addBulk(user, setId, request.normalize())
        return ResponseEntity.ok(result)
    }

    @PutMapping("/bulk")
    fun updateFlashcardsBulk(
        @AuthenticationPrincipal user: User,
        @PathVariable setId: Long,
        @RequestBody request: FlashcardBulkUpdateRequest,
    ): ResponseEntity<FlashcardBulkUpdateResponse> = withLoggingContext(user) {
        val response = flashcardService.updateBulk(user, setId, request.normalize())
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/bulk")
    fun removeFlashcardsBulk(
        @AuthenticationPrincipal user: User,
        @PathVariable setId: Long,
        @RequestParam(name = "ids", required = false) ids: List<String>?,
    ): ResponseEntity<Unit> = withLoggingContext(user) {
        val request = FlashcardBulkRemovalRequest(ids = ids?.map { FlashcardBulkRemovalRequest.FlashcardId(it) })
        flashcardService.removeBulk(user, setId, request.normalize())
        return ResponseEntity.ok().build()
    }

    @PutMapping("/{id:\\d+}")
    fun updateFlashcard(
        @AuthenticationPrincipal user: User,
        @PathVariable setId: Long,
        @PathVariable id: Long,
        @RequestParam(name = "sessionId", required = false) sessionId: Long?,
        @RequestBody request: FlashcardUpdateRequest,
    ): ResponseEntity<FlashcardUpdateResponse> = withLoggingContext(user) {
        val response = flashcardService.update(user, setId, id, request.normalize(), sessionId)
        return ResponseEntity.ok(response)
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
