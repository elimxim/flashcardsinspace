package com.github.elimxim.flashcardsinspace.web

import com.github.elimxim.flashcardsinspace.service.FlashcardTimelineService
import com.github.elimxim.flashcardsinspace.web.dto.ChronodayPutRequest
import com.github.elimxim.flashcardsinspace.web.dto.ChronodayResponse
import com.github.elimxim.flashcardsinspace.web.dto.ChronodaysGetResponse
import com.github.elimxim.flashcardsinspace.web.dto.TimelinePutRequest
import com.github.elimxim.flashcardsinspace.web.dto.TimelinePostRequest
import com.github.elimxim.flashcardsinspace.web.dto.TimelineResponse
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import java.time.ZonedDateTime

@Controller
@RequestMapping("/api/flashcard-sets/{setId}/timeline")
class FlashcardTimelineController(
    private val flashcardTimelineService: FlashcardTimelineService,
) {
    @GetMapping
    fun getTimeline(@PathVariable setId: Long): ResponseEntity<TimelineResponse> {
        val timeline = flashcardTimelineService.getTimeline(setId)
        return ResponseEntity.ok(TimelineResponse(timeline))
    }

    @PostMapping
    fun createTimeline(
        @PathVariable setId: Long,
        @RequestBody request: TimelinePostRequest,
    ): ResponseEntity<TimelineResponse> {
        val timeline = flashcardTimelineService.createTimeline(setId, request.clientDatetime)
        return ResponseEntity.ok(TimelineResponse(timeline))
    }

    @PutMapping
    fun updateTimeline(
        @PathVariable setId: Long,
        @RequestBody request: TimelinePutRequest,
    ): ResponseEntity<TimelineResponse> {
        val timeline = flashcardTimelineService.updateTimelineStatus(setId, request.timelineStatus)
        return ResponseEntity.ok(TimelineResponse(timeline))
    }

    @GetMapping("/chronodays")
    fun getChronodays(
        @PathVariable setId: Long,
        @RequestParam clientDatetime: ZonedDateTime,
    ): ResponseEntity<ChronodaysGetResponse> {
        val (currDay, chronodays) = flashcardTimelineService.getChronodays(setId, clientDatetime)
        return ResponseEntity.ok(ChronodaysGetResponse(currDay, chronodays))
    }

    @PostMapping("/chronodays")
    fun addChronoday(@PathVariable setId: Long): ResponseEntity<ChronodayResponse> {
        val chronoday = flashcardTimelineService.addChronoday(setId)
        return ResponseEntity.ok(ChronodayResponse(chronoday))
    }

    @PutMapping("/chronodays/{id}")
    fun updateChronoday(
        @PathVariable setId: Long,
        @PathVariable id: Long,
        @RequestBody request: ChronodayPutRequest
    ): ResponseEntity<ChronodayResponse> {
        val chronoday = flashcardTimelineService.updateChronodayStatus(setId, id, request.chronodayStatus)
        return ResponseEntity.ok(ChronodayResponse(chronoday))
    }

    @DeleteMapping("/chronodays/{id}")
    fun removeChronoday(@PathVariable setId: Long, @PathVariable id: Long): ResponseEntity<Unit> {
        flashcardTimelineService.removeChronoday(setId, id)
        return ResponseEntity.ok().build()
    }

}