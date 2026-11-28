package com.github.elimxim.flashcardsinspace.service

import com.github.elimxim.flashcardsinspace.entity.FlashcardPicture
import com.github.elimxim.flashcardsinspace.entity.FlashcardPictureThumbnail
import com.github.elimxim.flashcardsinspace.entity.User
import com.github.elimxim.flashcardsinspace.entity.checkVerified
import com.github.elimxim.flashcardsinspace.entity.repository.FlashcardPictureRepository
import com.github.elimxim.flashcardsinspace.entity.repository.FlashcardPictureThumbnailRepository
import com.github.elimxim.flashcardsinspace.entity.sizeKB
import com.github.elimxim.flashcardsinspace.service.validation.RequestValidator
import com.github.elimxim.flashcardsinspace.util.parseSideOrThrow
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardPictureDto
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardPictureMetadataDto
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardPictureThumbnailsRequest
import com.github.elimxim.flashcardsinspace.web.dto.toDto
import com.github.elimxim.flashcardsinspace.web.exception.ApiErrorCode
import com.github.elimxim.flashcardsinspace.web.exception.HttpBadRequestException
import com.github.elimxim.flashcardsinspace.web.exception.HttpNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.ZonedDateTime

private val log = LoggerFactory.getLogger(FlashcardPictureService::class.java)

private const val WEBP_MIME_TYPE = "image/webp"

@Service
class FlashcardPictureService(
    private val flashcardPictureRepository: FlashcardPictureRepository,
    private val flashcardPictureThumbnailRepository: FlashcardPictureThumbnailRepository,
    private val flashcardService: FlashcardService,
    private val flashcardSetService: FlashcardSetService,
    private val imageValidator: ImageValidator,
    private val requestValidator: RequestValidator,
) {
    @Transactional(readOnly = true)
    fun getMetadata(user: User, setId: Long): List<FlashcardPictureMetadataDto> {
        log.info("Getting picture metadata for flashcard set $setId")
        user.checkVerified()
        val flashcardSet = flashcardSetService.getEntity(setId)
        flashcardSetService.verifyUserHasAccess(user, flashcardSet)

        return flashcardPictureRepository.findAllMetadata(setId).map {
            FlashcardPictureMetadataDto(
                pictureId = it.getPictureId(),
                flashcardSide = it.getSide().name,
                flashcardId = it.getFlashcardId(),
                width = it.getWidth(),
                height = it.getHeight(),
            )
        }
    }

    @Transactional(readOnly = true)
    fun getThumbnails(
        user: User,
        setId: Long,
        side: String,
        request: FlashcardPictureThumbnailsRequest,
    ): Map<Long, ByteArray> {
        log.info("Getting ${request.ids?.size} picture thumbnails for flashcard set $setId, side: $side")
        user.checkVerified()
        val flashcardSet = flashcardSetService.getEntity(setId)
        flashcardSetService.verifyUserHasAccess(user, flashcardSet)
        val flashcardSide = side.parseSideOrThrow {
            throw HttpBadRequestException(ApiErrorCode.WFS400, "Invalid side $side for fetching thumbnails")
        }
        val validRequest = requestValidator.validate(request)

        return flashcardPictureThumbnailRepository.findAllInSet(setId, validRequest.ids, flashcardSide)
            .associate { it.flashcard.id to it.thumbnailData }
    }

    @Transactional(readOnly = true)
    fun fetchPicture(user: User, setId: Long, flashcardId: Long, side: String): FlashcardPicture? {
        log.info("Fetching picture for flashcard $flashcardId in set $setId, side: $side")
        user.checkVerified()
        val flashcard = flashcardService.getEntity(flashcardId)
        flashcardSetService.verifyUserHasAccess(user, flashcard.flashcardSet)
        flashcardService.verifyUserOperation(user, setId, flashcard)
        val flashcardSide = side.parseSideOrThrow {
            throw HttpBadRequestException(ApiErrorCode.WFS400, "Invalid side $side for uploading picture")
        }
        return flashcardPictureRepository.findByFlashcardIdAndSide(flashcardId, flashcardSide)
    }

    @Transactional(readOnly = true)
    fun fetchPicture(user: User, setId: Long, flashcardId: Long, pictureId: Long): FlashcardPicture {
        log.info("Fetching picture $pictureId for flashcard $flashcardId in set $setId")
        user.checkVerified()
        val flashcard = flashcardService.getEntity(flashcardId)
        flashcardSetService.verifyUserHasAccess(user, flashcard.flashcardSet)
        flashcardService.verifyUserOperation(user, setId, flashcard)
        return getEntity(pictureId)
    }

    @Transactional
    fun saveOrUpdatePicture(
        user: User,
        setId: Long,
        flashcardId: Long,
        side: String,
        file: MultipartFile,
    ): FlashcardPictureDto {
        log.info("Uploading picture file ${file.originalFilename} for flashcard $flashcardId in set $setId, size: ${file.size} bytes")
        user.checkVerified()
        val flashcard = flashcardService.getEntity(flashcardId)
        flashcardSetService.verifyUserHasAccess(user, flashcard.flashcardSet)
        flashcardService.verifyUserOperation(user, setId, flashcard)

        val flashcardSide = side.parseSideOrThrow {
            throw HttpBadRequestException(ApiErrorCode.WFS400, "Invalid side $side for uploading picture")
        }
        val validated = imageValidator.validateWebp(file.bytes)
        val pictureData = validated.bytes

        val picture = flashcardPictureRepository.findByFlashcardIdAndSide(flashcardId, flashcardSide)
            ?.apply {
                this.mimeType = WEBP_MIME_TYPE
                this.pictureData = pictureData
                this.pictureSize = pictureData.size.toLong()
                this.width = validated.width
                this.height = validated.height
                this.lastUpdatedAt = ZonedDateTime.now()
            }
            ?: FlashcardPicture(
                side = flashcardSide,
                mimeType = WEBP_MIME_TYPE,
                pictureData = pictureData,
                pictureSize = pictureData.size.toLong(),
                width = validated.width,
                height = validated.height,
                uploadedAt = ZonedDateTime.now(),
                flashcard = flashcard,
            )

        val savedPicture = flashcardPictureRepository.save(picture)

        val thumbnailData = PictureThumbnails.create(validated.image)
        val thumbnail = flashcardPictureThumbnailRepository.findByFlashcardIdAndSide(flashcardId, flashcardSide)
            ?.apply {
                this.thumbnailData = thumbnailData
                this.thumbnailSize = thumbnailData.size.toLong()
                this.lastUpdatedAt = ZonedDateTime.now()
                this.flashcardPicture = savedPicture
            }
            ?: FlashcardPictureThumbnail(
                side = flashcardSide,
                thumbnailData = thumbnailData,
                thumbnailSize = thumbnailData.size.toLong(),
                createdAt = ZonedDateTime.now(),
                flashcard = flashcard,
                flashcardPicture = savedPicture,
            )
        flashcardPictureThumbnailRepository.save(thumbnail)

        return savedPicture.run {
            log.info("Picture $id saved, side: $side, size: ${sizeKB()} KBs, ${width}x${height}, thumbnail: ${thumbnailData.size} bytes")
            toDto()
        }
    }

    @Transactional
    fun removePicture(user: User, setId: Long, flashcardId: Long, pictureId: Long) {
        log.info("Removing picture $pictureId for flashcard $flashcardId in set $setId")
        user.checkVerified()
        val flashcard = flashcardService.getEntity(flashcardId)
        flashcardSetService.verifyUserHasAccess(user, flashcard.flashcardSet)
        flashcardService.verifyUserOperation(user, setId, flashcard)

        val picture = getEntity(pictureId)
        flashcardPictureThumbnailRepository.deleteByFlashcardIdAndSide(flashcardId, picture.side.name)
        flashcardPictureRepository.delete(picture)
    }

    @Transactional(readOnly = true)
    fun getEntity(id: Long): FlashcardPicture =
        flashcardPictureRepository.findById(id).orElseThrow {
            HttpNotFoundException(ApiErrorCode.FPI404, "Picture with id $id not found")
        }

}
