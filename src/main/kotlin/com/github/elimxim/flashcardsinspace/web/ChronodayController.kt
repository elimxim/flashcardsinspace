package com.github.elimxim.flashcardsinspace.web

import com.github.elimxim.flashcardsinspace.service.ChronodayService
import com.github.elimxim.flashcardsinspace.web.dto.ChronodayPutRequest
import com.github.elimxim.flashcardsinspace.web.dto.ChronodayResponse
import com.github.elimxim.flashcardsinspace.web.dto.ChronodaysGetResponse
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import java.time.ZonedDateTime

@Controller
@RequestMapping("/api/flashcard-sets/{setId}/chronodays")
class ChronodayController(
    private val chronodayService: ChronodayService,
) {
    @GetMapping
    fun getChronodays(
        @PathVariable setId: Long,
        @RequestParam clientDatetime: ZonedDateTime,
    ): ResponseEntity<ChronodaysGetResponse> {
        val (currDay, chronodays) = chronodayService.getChronodays(setId, clientDatetime)
        return ResponseEntity.ok(ChronodaysGetResponse(currDay, chronodays))
    }

    @PostMapping
    fun addChronoday(
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

    @PutMapping("/{id}")
    fun updateChronoday(
        @PathVariable setId: Long,
        @PathVariable id: Long,
        @RequestBody request: ChronodayPutRequest
    ): ResponseEntity<ChronodayResponse> {
        val chronoday = chronodayService.updateChronodayStatus(setId, id, request.chronodayStatus)
        return ResponseEntity.ok(ChronodayResponse(chronoday))
    }

    @DeleteMapping("/{id}")
    fun removeChronoday(@PathVariable setId: Long, @PathVariable id: Long): ResponseEntity<Unit> {
        chronodayService.removeChronoday(setId, id)
        return ResponseEntity.ok().build()
    }

}