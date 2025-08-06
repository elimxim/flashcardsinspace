package com.github.elimxim.flashcardsinspace.web

import com.github.elimxim.flashcardsinspace.service.FlashcardService
import com.github.elimxim.flashcardsinspace.service.FlashcardSetService
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardPutRequest
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardPutResponse
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardsGetResponse
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardsPostRequest
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardsPostResponse
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/api/flashcard-sets/{flashcardSetId}/flashcards")
class FlashcardController(
    private val flashcardService: FlashcardService,
) {
    @GetMapping()
    fun getFlashcards(@PathVariable flashcardSetId: Long): ResponseEntity<FlashcardsGetResponse> {
        val flashcards = flashcardService.getFlashcards(flashcardSetId)
        return ResponseEntity.ok(FlashcardsGetResponse(flashcards))
    }

    @PostMapping()
    fun addFlashcard(
        @PathVariable flashcardSetId: Long,
        @RequestBody request: FlashcardsPostRequest,
    ): ResponseEntity<FlashcardsPostResponse> {
        val savedFlashcard = flashcardService.addFlashcard(flashcardSetId, request.flashcard)
        return ResponseEntity.ok(FlashcardsPostResponse(savedFlashcard))
    }

    @PutMapping("/{flashcardId}")
    fun updateFlashcard(
        @PathVariable flashcardSetId: Long,
        @PathVariable flashcardId: Long,
        @RequestBody request: FlashcardPutRequest,
    ): ResponseEntity<FlashcardPutResponse> {
        val updatedFlashcard = flashcardService.updateFlashcard(flashcardSetId, flashcardId, request.flashcard)
        return ResponseEntity.ok(FlashcardPutResponse(updatedFlashcard))
    }

    @DeleteMapping("/{flashcardId}")
    fun removeFlashcard(
        @PathVariable flashcardSetId: Long,
        @PathVariable flashcardId: Long,
    ): ResponseEntity<Unit> {
        flashcardService.removeFlashcard(flashcardSetId, flashcardId)
        return ResponseEntity.ok().build()
    }
}