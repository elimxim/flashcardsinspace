package com.github.elimxim.flashcardsinspace.web

import com.github.elimxim.flashcardsinspace.entity.User
import com.github.elimxim.flashcardsinspace.service.FlashcardSetService
import com.github.elimxim.flashcardsinspace.web.dto.*
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
    ): ResponseEntity<FlashcardSetsGetResponse> {
        val flashcardSets = flashcardSetService.findAll(user)
        return ResponseEntity.ok(FlashcardSetsGetResponse(flashcardSets))
    }

    @PostMapping
    fun addFlashcardSet(
        @AuthenticationPrincipal user: User,
        @RequestBody request: FlashcardSetsPostRequest,
    ): ResponseEntity<FlashcardSetsPostResponse> {
        val savedFlashcardSet = flashcardSetService.createNew(user, request.flashcardSet)
        return ResponseEntity.ok(FlashcardSetsPostResponse(savedFlashcardSet))
    }

    @GetMapping("/{id}")
    fun getFlashcardSet(
        @AuthenticationPrincipal user: User,
        @PathVariable id: Long,
    ): ResponseEntity<FlashcardSetGetResponse> {
        val flashcardSet = flashcardSetService.get(id)
        return ResponseEntity.ok(
            FlashcardSetGetResponse(flashcardSet)
        )
    }

    @PutMapping("/{id}")
    fun updateFlashcardSet(
        @AuthenticationPrincipal user: User,
        @PathVariable id: Long,
        @RequestBody request: FlashcardSetPutRequest,
    ): ResponseEntity<FlashcardSetPutResponse> {
        val updatedFlashcardSet = flashcardSetService.update(id, request.flashcardSet)
        return ResponseEntity.ok(
            FlashcardSetPutResponse(updatedFlashcardSet)
        )
    }

    @DeleteMapping("/{id}")
    fun removeFlashcardSet(
        @AuthenticationPrincipal user: User,
        @PathVariable id: Long,
    ): ResponseEntity<Unit> {
        flashcardSetService.remove(id)
        return ResponseEntity.ok().build()
    }

}