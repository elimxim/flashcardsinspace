package com.github.elimxim.flashcardsinspace.web

import com.github.elimxim.flashcardsinspace.entity.User
import com.github.elimxim.flashcardsinspace.security.escapeJava
import com.github.elimxim.flashcardsinspace.security.normalize
import com.github.elimxim.flashcardsinspace.service.FlashcardAudioService
import com.github.elimxim.flashcardsinspace.util.withLoggingContext
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardAudioDto
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@Controller
@RequestMapping("/api/flashcard-sets/{setId:\\d+}/flashcards/{flashcardId:\\d+}/audio")
class FlashcardAudioController(
    private val flashcardAudioService: FlashcardAudioService,
) {
    @GetMapping("/{audioId:\\d+}")
    fun downloadAudio(
        @AuthenticationPrincipal user: User,
        @PathVariable setId: Long,
        @PathVariable flashcardId: Long,
        @PathVariable audioId: Long,
    ): ResponseEntity<ByteArray> = withLoggingContext(user) {
        val audio = flashcardAudioService.fetchAudio(user, setId, flashcardId, audioId)
        return ResponseEntity.ok().run {
            val mimeType = audio.mimeType
            if (mimeType != null) {
                contentType(MediaType.parseMediaType(mimeType))
            }
            contentLength(audio.audioSize)
            body(audio.audioData)
        }
    }

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadAudio(
        @AuthenticationPrincipal user: User,
        @PathVariable setId: Long,
        @PathVariable flashcardId: Long,
        @RequestParam side: String,
        @RequestParam file: MultipartFile,
    ): ResponseEntity<FlashcardAudioDto> = withLoggingContext(user) {
        val dto = flashcardAudioService.saveOrUpdateAudio(user, setId, flashcardId, side.escapeJava().normalize(), file)
        return ResponseEntity.ok(dto)
    }

    @DeleteMapping("/{audioId:\\d+}")
    fun deleteAudio(
        @AuthenticationPrincipal user: User,
        @PathVariable setId: Long,
        @PathVariable flashcardId: Long,
        @PathVariable audioId: Long,
    ): ResponseEntity<Unit> = withLoggingContext(user) {
        flashcardAudioService.removeAudio(user, setId, flashcardId, audioId)
        return ResponseEntity.ok().build()
    }
}
