package com.github.elimxim.flashcardsinspace.web

import com.github.elimxim.flashcardsinspace.service.FlashcardService
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
@RequestMapping("/api/flashcard-sets/{setId}/flashcards")
class FlashcardController(
    private val flashcardService: FlashcardService,
) {
    @GetMapping
    fun getFlashcards(@PathVariable setId: Long): ResponseEntity<FlashcardsGetResponse> {
        val flashcards = flashcardService.getFlashcards(setId)
        return ResponseEntity.ok(FlashcardsGetResponse(flashcards))
    }

    @PostMapping
    fun addFlashcard(
        @PathVariable setId: Long,
        @RequestBody request: FlashcardsPostRequest,
    ): ResponseEntity<FlashcardsPostResponse> {
        val savedFlashcard = flashcardService.addFlashcard(setId, request.flashcard)
        return ResponseEntity.ok(FlashcardsPostResponse(savedFlashcard))
    }

    @PutMapping("/{id}")
    fun updateFlashcard(
        @PathVariable setId: Long,
        @PathVariable id: Long,
        @RequestBody request: FlashcardPutRequest,
    ): ResponseEntity<FlashcardPutResponse> {
        val updatedFlashcard = flashcardService.updateFlashcard(setId, id, request.flashcard)
        return ResponseEntity.ok(FlashcardPutResponse(updatedFlashcard))
    }

    @DeleteMapping("/{id}")
    fun removeFlashcard(
        @PathVariable setId: Long,
        @PathVariable id: Long,
    ): ResponseEntity<Unit> {
        flashcardService.removeFlashcard(setId, id)
        return ResponseEntity.ok().build()
    }
}