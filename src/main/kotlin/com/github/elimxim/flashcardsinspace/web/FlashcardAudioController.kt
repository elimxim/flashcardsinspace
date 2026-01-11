package com.github.elimxim.flashcardsinspace.web

import com.github.elimxim.flashcardsinspace.entity.FlashcardAudio
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardAudioMetadataDto
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
@RequestMapping("/api/flashcard-sets/{setId:\\d+}/flashcards")
class FlashcardAudioController(
    private val flashcardAudioService: FlashcardAudioService,
) {
    @GetMapping("/audio/metadata")
    fun getMetadata(
        @AuthenticationPrincipal user: User,
        @PathVariable setId: Long,
    ): ResponseEntity<List<FlashcardAudioMetadataDto>> = withLoggingContext(user) {
        val result = flashcardAudioService.getMetadata(user, setId)
        return ResponseEntity.ok(result)
    }

    @PostMapping("/{flashcardId:\\d+}/audio", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadFlashcardAudio(
        @AuthenticationPrincipal user: User,
        @PathVariable setId: Long,
        @PathVariable flashcardId: Long,
        @RequestParam side: String,
        @RequestParam file: MultipartFile,
    ): ResponseEntity<FlashcardAudioDto> = withLoggingContext(user) {
        val dto = flashcardAudioService.saveOrUpdateAudio(user, setId, flashcardId, side.normalize().escapeJava(), file)
        return ResponseEntity.ok(dto)
    }

    @GetMapping("/{flashcardId:\\d+}/audio")
    fun fetchFlashcardAudio(
        @AuthenticationPrincipal user: User,
        @PathVariable setId: Long,
        @PathVariable flashcardId: Long,
        @RequestParam side: String,
    ): ResponseEntity<ByteArray> = withLoggingContext(user) {
        val audio = flashcardAudioService.fetchAudio(user, setId, flashcardId, side.normalize().escapeJava())
        return if (audio != null) {
            fetchAudioResponseEntity(audio)
        } else {
            ResponseEntity.noContent().build()
        }
    }


    @GetMapping("/{flashcardId:\\d+}/audio/{audioId:\\d+}")
    fun fetchFlashcardAudioById(
        @AuthenticationPrincipal user: User,
        @PathVariable setId: Long,
        @PathVariable flashcardId: Long,
        @PathVariable audioId: Long,
    ): ResponseEntity<ByteArray> = withLoggingContext(user) {
        val audio = flashcardAudioService.fetchAudio(user, setId, flashcardId, audioId)
        return fetchAudioResponseEntity(audio)
    }

    @DeleteMapping("/{flashcardId:\\d+}/audio/{audioId:\\d+}")
    fun deleteFlashcardAudio(
        @AuthenticationPrincipal user: User,
        @PathVariable setId: Long,
        @PathVariable flashcardId: Long,
        @PathVariable audioId: Long,
    ): ResponseEntity<Unit> = withLoggingContext(user) {
        flashcardAudioService.removeAudio(user, setId, flashcardId, audioId)
        return ResponseEntity.ok().build()
    }
}

private fun fetchAudioResponseEntity(audio: FlashcardAudio) = ResponseEntity.ok().run {
    header("X-Audio-Id", audio.id.toString())
    val mimeType = audio.mimeType
    if (mimeType != null) {
        contentType(MediaType.parseMediaType(mimeType))
    }
    contentLength(audio.audioSize)
    body(audio.audioData)
}
