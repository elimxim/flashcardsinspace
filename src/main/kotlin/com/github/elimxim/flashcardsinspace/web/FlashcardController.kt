package com.github.elimxim.flashcardsinspace.web

import com.github.elimxim.flashcardsinspace.entity.User
import com.github.elimxim.flashcardsinspace.service.FlashcardService
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardPutRequest
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardPutResponse
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardSetPutRequest
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardSetPutResponse
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardSetsPostRequest
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardSetsPostResponse
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardSetsGetResponse
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardsGetResponse
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardsPostRequest
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardsPostResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/api/flashcard-sets")
class FlashcardController(
    private val flashcardService: FlashcardService,
) {
    @GetMapping()
    fun getFlashcardSets(
        @AuthenticationPrincipal user: User,
    ): ResponseEntity<FlashcardSetsGetResponse> {
        val flashcardSets = flashcardService.getFlashcardSets(user)
        return ResponseEntity.ok(FlashcardSetsGetResponse(flashcardSets))
    }

    @PostMapping()
    fun addFlashcardSet(
        @AuthenticationPrincipal user: User,
        @RequestBody request: FlashcardSetsPostRequest,
    ): ResponseEntity<FlashcardSetsPostResponse> {
        val savedFlashcardSet = flashcardService.addFlashcardSet(user, request.flashcardSet)
        return ResponseEntity.ok(FlashcardSetsPostResponse(savedFlashcardSet))
    }

    @PutMapping("/{flashcardSetId}")
    fun updateFlashcardSet(
        @AuthenticationPrincipal user: User,
        @PathVariable flashcardSetId: Long,
        @RequestBody request: FlashcardSetPutRequest,
    ): ResponseEntity<FlashcardSetPutResponse> {
        val updatedFlashcardSet = flashcardService.updateFlashcardSet(flashcardSetId, request.flashcardSet)
        return ResponseEntity.ok(
            FlashcardSetPutResponse(updatedFlashcardSet)
        )
    }

    @DeleteMapping("/{flashcardSetId}")
    fun removeFlashcardSet(
        @AuthenticationPrincipal user: User,
        @PathVariable flashcardSetId: Long,
    ): ResponseEntity<Unit> {
        flashcardService.removeFlashcardSet(flashcardSetId)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/{flashcardSetId}/flashcards")
    fun getFlashcards(@PathVariable flashcardSetId: Long): ResponseEntity<FlashcardsGetResponse> {
        val flashcards = flashcardService.getFlashcards(flashcardSetId)
        return ResponseEntity.ok(FlashcardsGetResponse(flashcards))
    }

    @PostMapping("/{flashcardSetId}/flashcards")
    fun addFlashcard(
        @PathVariable flashcardSetId: Long,
        @RequestBody request: FlashcardsPostRequest,
    ): ResponseEntity<FlashcardsPostResponse> {
        val savedFlashcard = flashcardService.addFlashcard(flashcardSetId, request.flashcard)
        return ResponseEntity.ok(FlashcardsPostResponse(savedFlashcard))
    }

    @PutMapping("/{flashcardSetId}/flashcards/{flashcardId}")
    fun updateFlashcard(
        @PathVariable flashcardSetId: Long,
        @PathVariable flashcardId: Long,
        @RequestBody request: FlashcardPutRequest,
    ): ResponseEntity<FlashcardPutResponse> {
        val updatedFlashcard = flashcardService.updateFlashcard(flashcardSetId, flashcardId, request.flashcard)
        return ResponseEntity.ok(FlashcardPutResponse(updatedFlashcard))
    }

    @DeleteMapping("/{flashcardSetId}/flashcards/{flashcardId}")
    fun removeFlashcard(
        @PathVariable flashcardSetId: Long,
        @PathVariable flashcardId: Long,
    ): ResponseEntity<Unit> {
        flashcardService.removeFlashcard(flashcardSetId, flashcardId)
        return ResponseEntity.ok().build()
    }

}