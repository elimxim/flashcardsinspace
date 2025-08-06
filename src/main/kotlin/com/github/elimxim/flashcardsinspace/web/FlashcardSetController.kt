package com.github.elimxim.flashcardsinspace.web

import com.github.elimxim.flashcardsinspace.entity.User
import com.github.elimxim.flashcardsinspace.service.FlashcardSetService
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardPutRequest
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardPutResponse
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardSetPutRequest
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardSetPutResponse
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardSetsPostRequest
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardSetsPostResponse
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardSetsGetResponse
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
class FlashcardSetController(
    private val flashcardSetService: FlashcardSetService,
) {
    @GetMapping()
    fun getFlashcardSets(
        @AuthenticationPrincipal user: User,
    ): ResponseEntity<FlashcardSetsGetResponse> {
        val flashcardSets = flashcardSetService.getFlashcardSets(user)
        return ResponseEntity.ok(FlashcardSetsGetResponse(flashcardSets))
    }

    @PostMapping()
    fun addFlashcardSet(
        @AuthenticationPrincipal user: User,
        @RequestBody request: FlashcardSetsPostRequest,
    ): ResponseEntity<FlashcardSetsPostResponse> {
        val savedFlashcardSet = flashcardSetService.addFlashcardSet(user, request.flashcardSet)
        return ResponseEntity.ok(FlashcardSetsPostResponse(savedFlashcardSet))
    }

    @PutMapping("/{flashcardSetId}")
    fun updateFlashcardSet(
        @AuthenticationPrincipal user: User,
        @PathVariable flashcardSetId: Long,
        @RequestBody request: FlashcardSetPutRequest,
    ): ResponseEntity<FlashcardSetPutResponse> {
        val updatedFlashcardSet = flashcardSetService.updateFlashcardSet(flashcardSetId, request.flashcardSet)
        return ResponseEntity.ok(
            FlashcardSetPutResponse(updatedFlashcardSet)
        )
    }

    @DeleteMapping("/{flashcardSetId}")
    fun removeFlashcardSet(
        @AuthenticationPrincipal user: User,
        @PathVariable flashcardSetId: Long,
    ): ResponseEntity<Unit> {
        flashcardSetService.removeFlashcardSet(flashcardSetId)
        return ResponseEntity.ok().build()
    }



}