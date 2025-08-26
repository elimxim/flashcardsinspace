package com.github.elimxim.flashcardsinspace.web

import com.github.elimxim.flashcardsinspace.service.ChronodayService
import com.github.elimxim.flashcardsinspace.web.dto.*
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/api/flashcard-sets/{setId}/chronodays")
class ChronodayController(
    private val chronodayService: ChronodayService,
) {
    @PutMapping
    fun get(
        @PathVariable setId: Long,
        @RequestBody request: ChronodaysPutRequest,
    ): ResponseEntity<ChronodaysPutResponse> {
        val (currDay, chronodays) = chronodayService.getChronodays(setId, request.clientDatetime)
        return ResponseEntity.ok(ChronodaysPutResponse(currDay, chronodays))
    }

    @PostMapping
    fun add(
        @PathVariable setId: Long,
        @RequestParam initial: Boolean = false,
    ): ResponseEntity<ChronodayResponse> {
        val chronoday = if (initial) {
            chronodayService.addInitialChronoday(setId)
        } else {
            chronodayService.addChronoday(setId)
        }
        return ResponseEntity.ok(ChronodayResponse(chronoday))
    }

    @PutMapping("/bulk")
    fun bulkUpdate(
        @PathVariable setId: Long,
        @RequestParam ids: List<Long>,
        @RequestBody request: RequestBodyContainer.ChronodayStatus,
    ): ResponseEntity<List<ChronodayDto>> {
        val chronodays = chronodayService.updateChronodays(setId, ids, request.status)
        return ResponseEntity.ok(chronodays)
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable setId: Long, @PathVariable id: Long): ResponseEntity<Unit> {
        chronodayService.removeChronoday(setId, id)
        return ResponseEntity.ok().build()
    }

}