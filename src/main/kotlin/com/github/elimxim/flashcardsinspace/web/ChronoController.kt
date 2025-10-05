package com.github.elimxim.flashcardsinspace.web

import com.github.elimxim.flashcardsinspace.entity.User
import com.github.elimxim.flashcardsinspace.security.normalize
import com.github.elimxim.flashcardsinspace.service.ChronoService
import com.github.elimxim.flashcardsinspace.service.ChronoSyncDay
import com.github.elimxim.flashcardsinspace.web.dto.ChronoBulkUpdateRequest
import com.github.elimxim.flashcardsinspace.web.dto.ChronoSyncRequest
import com.github.elimxim.flashcardsinspace.web.dto.ChronoSyncResponse
import com.github.elimxim.flashcardsinspace.web.dto.ChronodayDto
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/api/flashcard-sets/{setId:\\d+}/chrono")
class ChronoController(
    private val chronoService: ChronoService,
) {
    @PostMapping("/sync")
    fun synchronizeChronodays(
        @AuthenticationPrincipal user: User,
        @PathVariable setId: Long,
        @RequestBody request: ChronoSyncRequest,
    ): ResponseEntity<ChronoSyncResponse> {
        val (currDay, chronodays) = chronoService.sync(user, setId, request.normalize())
        return ResponseEntity.ok(ChronoSyncResponse(currDay, chronodays))
    }

    @PostMapping("/sync/next")
    fun synchronizeNextDay(
        @AuthenticationPrincipal user: User,
        @PathVariable setId: Long,
    ): ResponseEntity<ChronoSyncResponse> {
        val (currDay, chronodays) = chronoService.syncDay(user, setId, day = ChronoSyncDay.NEXT)
        return ResponseEntity.ok(ChronoSyncResponse(currDay, chronodays))
    }

    @PostMapping("/sync/prev")
    fun synchronizePrevDay(
        @AuthenticationPrincipal user: User,
        @PathVariable setId: Long,
    ): ResponseEntity<ChronoSyncResponse> {
        val (currDay, chronodays) = chronoService.syncDay(user, setId, day = ChronoSyncDay.PREV)
        return ResponseEntity.ok(ChronoSyncResponse(currDay, chronodays))
    }

    @PutMapping("/bulk")
    fun bulkUpdateChronodays(
        @AuthenticationPrincipal user: User,
        @PathVariable setId: Long,
        @RequestBody request: ChronoBulkUpdateRequest,
    ): ResponseEntity<List<ChronodayDto>> {
        val chronodays = chronoService.bulkUpdate(user, setId, request.normalize())
        return ResponseEntity.ok(chronodays)
    }

}
