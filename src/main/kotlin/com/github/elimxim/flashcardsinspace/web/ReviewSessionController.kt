package com.github.elimxim.flashcardsinspace.web

import com.github.elimxim.flashcardsinspace.entity.ReviewSessionType
import com.github.elimxim.flashcardsinspace.entity.User
import com.github.elimxim.flashcardsinspace.security.escapeJava
import com.github.elimxim.flashcardsinspace.security.normalize
import com.github.elimxim.flashcardsinspace.service.ReviewSessionService
import com.github.elimxim.flashcardsinspace.service.validation.ValidReviewSessionType
import com.github.elimxim.flashcardsinspace.web.dto.ReviewSessionCreateRequest
import com.github.elimxim.flashcardsinspace.web.dto.ReviewSessionDto
import com.github.elimxim.flashcardsinspace.web.dto.ReviewSessionUpdateRequest
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/api/flashcard-sets/{setId:\\d+}/review-sessions")
class ReviewSessionController(
    private val reviewSessionService: ReviewSessionService,
) {

    @PostMapping
    fun createReviewSession(
        @AuthenticationPrincipal user: User,
        @PathVariable setId: Long,
        @RequestBody request: ReviewSessionCreateRequest,
    ): ResponseEntity<ReviewSessionDto> {
        val dto = reviewSessionService.createReviewSession(user, setId, request.normalize())
        return ResponseEntity.ok(dto)
    }

    @PutMapping("/{id:\\d+}")
    fun updateReviewSession(
        @AuthenticationPrincipal user: User,
        @PathVariable setId: Long,
        @PathVariable id: Long,
        @RequestBody request: ReviewSessionUpdateRequest,
    ): ResponseEntity<Unit> {
        reviewSessionService.updateReviewSession(user, setId, id, request.normalize())
        return ResponseEntity.ok().build()
    }

    @PostMapping("/{parentId:\\d+}/children")
    fun createChildReviewSession(
        @AuthenticationPrincipal user: User,
        @PathVariable setId: Long,
        @PathVariable parentId: Long,
        @RequestBody request: ReviewSessionCreateRequest,
    ): ResponseEntity<ReviewSessionDto> {
        val dto = reviewSessionService.createChildReviewSession(user, setId, parentId, request.normalize())
        return ResponseEntity.ok(dto)
    }

    @GetMapping("/{id:\\d+}")
    fun getReviewSession(
        @AuthenticationPrincipal user: User,
        @PathVariable setId: Long,
        @PathVariable id: Long,
    ): ResponseEntity<ReviewSessionDto> {
        val dto = reviewSessionService.getReviewSession(user, setId, id)
        return ResponseEntity.ok(dto)
    }

    @GetMapping("/latest")
    fun getLatestReviewSession(
        @AuthenticationPrincipal user: User,
        @PathVariable setId: Long,
        @RequestParam @ValidReviewSessionType type: String,
    ): ResponseEntity<ReviewSessionDto> {
        val type = ReviewSessionType.valueOf(type.normalize().escapeJava())
        val dto = reviewSessionService.getLatestReviewSession(user, setId, type)
        return ResponseEntity.ok(dto)
    }
}
