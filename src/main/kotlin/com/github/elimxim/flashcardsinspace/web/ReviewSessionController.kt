package com.github.elimxim.flashcardsinspace.web

import com.github.elimxim.flashcardsinspace.entity.User
import com.github.elimxim.flashcardsinspace.security.normalize
import com.github.elimxim.flashcardsinspace.service.ReviewSessionService
import com.github.elimxim.flashcardsinspace.web.dto.ReviewSessionCreateRequest
import com.github.elimxim.flashcardsinspace.web.dto.ReviewSessionResponse
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
    ): ResponseEntity<ReviewSessionResponse> {
        val response = reviewSessionService.create(user, setId, request.normalize())
        return ResponseEntity.ok(response)
    }

    @PutMapping("/{id:\\d+}")
    fun updateReviewSession(
        @AuthenticationPrincipal user: User,
        @PathVariable setId: Long,
        @PathVariable id: Long,
        @RequestBody request: ReviewSessionUpdateRequest,
    ): ResponseEntity<Unit> {
        reviewSessionService.update(user, setId, id, request.normalize())
        return ResponseEntity.ok().build()
    }

    @PostMapping("/{parentId:\\d+}/childs")
    fun createChildReviewSession(
        @AuthenticationPrincipal user: User,
        @PathVariable setId: Long,
        @PathVariable parentId: Long,
        @RequestBody request: ReviewSessionCreateRequest,
    ): ResponseEntity<ReviewSessionResponse> {
        val response = reviewSessionService.createChild(user, setId, parentId, request.normalize())
        return ResponseEntity.ok(response)
    }
}
