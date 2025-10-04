package com.github.elimxim.flashcardsinspace.web

import com.github.elimxim.flashcardsinspace.entity.User
import com.github.elimxim.flashcardsinspace.security.normalize
import com.github.elimxim.flashcardsinspace.service.ChronoService
import com.github.elimxim.flashcardsinspace.web.dto.ChronoBulkUpdateRequest
import com.github.elimxim.flashcardsinspace.web.dto.ChronoSyncRequest
import com.github.elimxim.flashcardsinspace.web.dto.ChronoSyncResponse
import com.github.elimxim.flashcardsinspace.web.dto.ChronodayDto
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/api/flashcard-sets/{setId:\\d+}/chronodays")
class ChronoController(
    private val chronoService: ChronoService,
) {
    @PutMapping
    fun synchronizeChronodays(
        @AuthenticationPrincipal user: User,
        @PathVariable setId: Long,
        @RequestBody request: ChronoSyncRequest,
    ): ResponseEntity<ChronoSyncResponse> {
        val (currDay, chronodays) = chronoService.sync(user, setId, request.normalize())
        return ResponseEntity.ok(ChronoSyncResponse(currDay, chronodays))
    }

    @PostMapping
    fun addChronoday(
        @AuthenticationPrincipal user: User,
        @PathVariable setId: Long,
    ): ResponseEntity<ChronodayDto> {
        val dto = chronoService.addNext(user, setId)
        return ResponseEntity.ok(dto)
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

    @DeleteMapping("/{id:\\d+}")
    fun removeChronoday(
        @AuthenticationPrincipal user: User,
        @PathVariable setId: Long,
        @PathVariable id: Long
    ): ResponseEntity<Unit> {
        chronoService.remove(user, setId, id)
        return ResponseEntity.ok().build()
    }

}
