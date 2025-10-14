package com.github.elimxim.flashcardsinspace.web

import com.github.elimxim.flashcardsinspace.entity.User
import com.github.elimxim.flashcardsinspace.security.normalize
import com.github.elimxim.flashcardsinspace.service.FlashcardSetService
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardSetCreationRequest
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardSetDto
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardSetExtraDto
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardSetUpdateRequest
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/api/flashcard-sets")
class FlashcardSetController(
    private val flashcardSetService: FlashcardSetService,
) {
    @GetMapping
    fun getFlashcardSets(
        @AuthenticationPrincipal user: User,
    ): ResponseEntity<List<FlashcardSetDto>> {
        val result = flashcardSetService.getAll(user)
        return ResponseEntity.ok(result)
    }

    @GetMapping("/extra")
    fun getFlashcardSetsExtra(
        @AuthenticationPrincipal user: User,
    ): ResponseEntity<List<FlashcardSetExtraDto>> {
        val result = flashcardSetService.getAllExtra(user)
        return ResponseEntity.ok(result)
    }

    @GetMapping("/{id:\\d+}")
    fun getFlashcardSet(
        @AuthenticationPrincipal user: User,
        @PathVariable id: Long,
    ): ResponseEntity<FlashcardSetDto> {
        val dto = flashcardSetService.get(user, id)
        return ResponseEntity.ok(dto)
    }

    @PostMapping
    fun addFlashcardSet(
        @AuthenticationPrincipal user: User,
        @RequestBody request: FlashcardSetCreationRequest,
    ): ResponseEntity<FlashcardSetDto> {
        val dto = flashcardSetService.create(user, request.normalize())
        return ResponseEntity.ok(dto)
    }

    @PutMapping("/{id:\\d+}")
    fun updateFlashcardSet(
        @AuthenticationPrincipal user: User,
        @PathVariable id: Long,
        @RequestBody request: FlashcardSetUpdateRequest,
    ): ResponseEntity<FlashcardSetDto> {
        val dto = flashcardSetService.update(user, id, request.normalize())
        return ResponseEntity.ok(dto)
    }

    @DeleteMapping("/{id:\\d+}")
    fun removeFlashcardSet(
        @AuthenticationPrincipal user: User,
        @PathVariable id: Long,
    ): ResponseEntity<Unit> {
        flashcardSetService.remove(user, id)
        return ResponseEntity.ok().build()
    }

}
