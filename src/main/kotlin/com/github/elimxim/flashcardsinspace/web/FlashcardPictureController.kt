package com.github.elimxim.flashcardsinspace.web

import com.github.elimxim.flashcardsinspace.entity.FlashcardPicture
import com.github.elimxim.flashcardsinspace.entity.User
import com.github.elimxim.flashcardsinspace.security.escapeJava
import com.github.elimxim.flashcardsinspace.security.normalize
import com.github.elimxim.flashcardsinspace.service.FlashcardPictureService
import com.github.elimxim.flashcardsinspace.util.withLoggingContext
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardPictureDto
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardPictureMetadataDto
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@Controller
@RequestMapping("/api/flashcard-sets/{setId:\\d+}/flashcards")
class FlashcardPictureController(
    private val flashcardPictureService: FlashcardPictureService,
) {
    @GetMapping("/pictures/metadata")
    fun getMetadata(
        @AuthenticationPrincipal user: User,
        @PathVariable setId: Long,
    ): ResponseEntity<List<FlashcardPictureMetadataDto>> = withLoggingContext(user) {
        val result = flashcardPictureService.getMetadata(user, setId)
        return ResponseEntity.ok(result)
    }

    @PostMapping("/{flashcardId:\\d+}/picture", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadFlashcardPicture(
        @AuthenticationPrincipal user: User,
        @PathVariable setId: Long,
        @PathVariable flashcardId: Long,
        @RequestParam side: String,
        @RequestParam file: MultipartFile,
    ): ResponseEntity<FlashcardPictureDto> = withLoggingContext(user) {
        val dto = flashcardPictureService.saveOrUpdatePicture(user, setId, flashcardId, side.normalize().escapeJava(), file)
        return ResponseEntity.ok(dto)
    }

    @GetMapping("/{flashcardId:\\d+}/picture")
    fun fetchFlashcardPicture(
        @AuthenticationPrincipal user: User,
        @PathVariable setId: Long,
        @PathVariable flashcardId: Long,
        @RequestParam side: String,
    ): ResponseEntity<ByteArray> = withLoggingContext(user) {
        val picture = flashcardPictureService.fetchPicture(user, setId, flashcardId, side.normalize().escapeJava())
        return if (picture != null) {
            fetchPictureResponseEntity(picture)
        } else {
            ResponseEntity.noContent().build()
        }
    }

    @GetMapping("/{flashcardId:\\d+}/picture/{pictureId:\\d+}")
    fun fetchFlashcardPictureById(
        @AuthenticationPrincipal user: User,
        @PathVariable setId: Long,
        @PathVariable flashcardId: Long,
        @PathVariable pictureId: Long,
    ): ResponseEntity<ByteArray> = withLoggingContext(user) {
        val picture = flashcardPictureService.fetchPicture(user, setId, flashcardId, pictureId)
        return fetchPictureResponseEntity(picture)
    }

    @DeleteMapping("/{flashcardId:\\d+}/picture/{pictureId:\\d+}")
    fun deleteFlashcardPicture(
        @AuthenticationPrincipal user: User,
        @PathVariable setId: Long,
        @PathVariable flashcardId: Long,
        @PathVariable pictureId: Long,
    ): ResponseEntity<Unit> = withLoggingContext(user) {
        flashcardPictureService.removePicture(user, setId, flashcardId, pictureId)
        return ResponseEntity.ok().build()
    }
}

private fun fetchPictureResponseEntity(picture: FlashcardPicture) = ResponseEntity.ok().run {
    header("X-Picture-Id", picture.id.toString())
    val mimeType = picture.mimeType
    if (mimeType != null) {
        contentType(MediaType.parseMediaType(mimeType))
    } else {
        contentType(MediaType.parseMediaType("image/webp"))
    }
    contentLength(picture.pictureSize)
    body(picture.pictureData)
}
