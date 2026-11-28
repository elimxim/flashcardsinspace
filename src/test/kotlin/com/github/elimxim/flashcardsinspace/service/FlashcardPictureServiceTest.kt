package com.github.elimxim.flashcardsinspace.service

import com.github.elimxim.flashcardsinspace.TestImageSamples
import com.github.elimxim.flashcardsinspace.entity.Flashcard
import com.github.elimxim.flashcardsinspace.entity.FlashcardPicture
import com.github.elimxim.flashcardsinspace.entity.FlashcardPictureThumbnail
import com.github.elimxim.flashcardsinspace.entity.FlashcardSet
import com.github.elimxim.flashcardsinspace.entity.FlashcardSide
import com.github.elimxim.flashcardsinspace.entity.FlashcardStage
import com.github.elimxim.flashcardsinspace.entity.User
import com.github.elimxim.flashcardsinspace.entity.repository.FlashcardPictureRepository
import com.github.elimxim.flashcardsinspace.entity.repository.FlashcardPictureThumbnailRepository
import com.github.elimxim.flashcardsinspace.service.validation.RequestValidator
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardPictureThumbnailsRequest
import com.github.elimxim.flashcardsinspace.web.dto.ValidFlashcardPictureThumbnailsRequest
import com.github.elimxim.flashcardsinspace.web.exception.HttpBadRequestException
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.mock.web.MockMultipartFile
import java.io.ByteArrayInputStream
import java.time.LocalDate
import java.time.ZonedDateTime
import javax.imageio.ImageIO

private const val SET_ID = 1L
private const val FLASHCARD_ID = 10L

class FlashcardPictureServiceTest {
    private val flashcardPictureRepository = mockk<FlashcardPictureRepository>(relaxed = true)
    private val flashcardPictureThumbnailRepository = mockk<FlashcardPictureThumbnailRepository>(relaxed = true)
    private val flashcardService = mockk<FlashcardService>(relaxed = true)
    private val flashcardSetService = mockk<FlashcardSetService>(relaxed = true)
    private val requestValidator = mockk<RequestValidator>()

    private val service = FlashcardPictureService(
        flashcardPictureRepository = flashcardPictureRepository,
        flashcardPictureThumbnailRepository = flashcardPictureThumbnailRepository,
        flashcardService = flashcardService,
        flashcardSetService = flashcardSetService,
        imageValidator = ImageValidator(),
        requestValidator = requestValidator,
    )

    private val user = mockk<User>(relaxed = true) {
        every { emailVerified } returns true
    }
    private val flashcardSet = mockk<FlashcardSet>(relaxed = true) {
        every { id } returns SET_ID
    }
    private val flashcard = Flashcard(
        id = FLASHCARD_ID,
        frontSide = "front",
        backSide = "back",
        stage = FlashcardStage.S1,
        timesReviewed = 0,
        creationDate = LocalDate.of(2025, 1, 1),
        flashcardSet = flashcardSet,
    )

    @BeforeEach
    fun setUp() {
        every { flashcardService.getEntity(FLASHCARD_ID) } returns flashcard
        every { flashcardPictureRepository.save(any()) } answers { firstArg() }
        every { flashcardPictureThumbnailRepository.save(any()) } answers { firstArg() }
    }

    @Nested
    inner class SaveOrUpdatePicture {

        @Test
        fun `should save a thumbnail along with a new picture`() {
            every { flashcardPictureRepository.findByFlashcardIdAndSide(FLASHCARD_ID, FlashcardSide.FRONT) } returns null
            every { flashcardPictureThumbnailRepository.findByFlashcardIdAndSide(FLASHCARD_ID, FlashcardSide.FRONT) } returns null
            val saved = slot<FlashcardPictureThumbnail>()
            every { flashcardPictureThumbnailRepository.save(capture(saved)) } answers { firstArg() }

            service.saveOrUpdatePicture(user, SET_ID, FLASHCARD_ID, "FRONT", webpFile(1600, 1200))

            assertThat(saved.captured.side).isEqualTo(FlashcardSide.FRONT)
            assertThat(saved.captured.flashcard).isSameAs(flashcard)
            assertThat(saved.captured.flashcardPicture.flashcard).isSameAs(flashcard)
            assertThat(saved.captured.flashcardPicture.side).isEqualTo(FlashcardSide.FRONT)
            val thumbnail = ImageIO.read(ByteArrayInputStream(saved.captured.thumbnailData))
            assertThat(thumbnail.width).isEqualTo(THUMBNAIL_MAX_DIMENSION)
            assertThat(thumbnail.height).isEqualTo(300)
            assertThat(saved.captured.thumbnailSize).isEqualTo(saved.captured.thumbnailData.size.toLong())
        }

        @Test
        fun `should replace the thumbnail when the picture is replaced`() {
            val existing = FlashcardPictureThumbnail(
                side = FlashcardSide.FRONT,
                thumbnailData = byteArrayOf(1),
                thumbnailSize = 1,
                createdAt = ZonedDateTime.now().minusDays(1),
                flashcard = flashcard,
                flashcardPicture = picture(FlashcardSide.FRONT),
            )
            every { flashcardPictureThumbnailRepository.findByFlashcardIdAndSide(FLASHCARD_ID, FlashcardSide.FRONT) } returns existing

            service.saveOrUpdatePicture(user, SET_ID, FLASHCARD_ID, "FRONT", webpFile(800, 600))

            verify { flashcardPictureThumbnailRepository.save(existing) }
            assertThat(ImageIO.read(ByteArrayInputStream(existing.thumbnailData)).width).isEqualTo(THUMBNAIL_MAX_DIMENSION)
            assertThat(existing.thumbnailSize).isEqualTo(existing.thumbnailData.size.toLong())
            assertThat(existing.lastUpdatedAt).isNotNull()
        }
    }

    @Nested
    inner class RemovePicture {

        @Test
        fun `should remove the thumbnail of the picture's side`() {
            every { flashcardPictureRepository.findById(5L) } returns java.util.Optional.of(picture(FlashcardSide.BACK))

            service.removePicture(user, SET_ID, FLASHCARD_ID, 5L)

            verify { flashcardPictureThumbnailRepository.deleteByFlashcardIdAndSide(FLASHCARD_ID, "BACK") }
        }
    }

    @Nested
    inner class GetThumbnails {

        @Test
        fun `should return the thumbnails by flashcard id`() {
            every { flashcardSetService.getEntity(SET_ID) } returns flashcardSet
            every { requestValidator.validate(any<FlashcardPictureThumbnailsRequest>()) } returns
                ValidFlashcardPictureThumbnailsRequest(ids = listOf(FLASHCARD_ID, 11L))
            val data = byteArrayOf(1, 2, 3)
            every {
                flashcardPictureThumbnailRepository.findAllInSet(SET_ID, listOf(FLASHCARD_ID, 11L), FlashcardSide.FRONT)
            } returns listOf(
                FlashcardPictureThumbnail(side = FlashcardSide.FRONT, thumbnailData = data, thumbnailSize = data.size.toLong(), createdAt = ZonedDateTime.now(), flashcard = flashcard, flashcardPicture = picture(FlashcardSide.FRONT))
            )

            val thumbnails = service.getThumbnails(user, SET_ID, "FRONT", FlashcardPictureThumbnailsRequest())

            assertThat(thumbnails).containsOnlyKeys(FLASHCARD_ID)
            assertThat(thumbnails[FLASHCARD_ID]).isEqualTo(data)
        }

        @Test
        fun `should reject an unknown side`() {
            every { flashcardSetService.getEntity(SET_ID) } returns flashcardSet

            assertThrows<HttpBadRequestException> {
                service.getThumbnails(user, SET_ID, "MIDDLE", FlashcardPictureThumbnailsRequest())
            }
        }
    }

    private fun webpFile(width: Int, height: Int) =
        MockMultipartFile("file", "picture.webp", "image/webp", TestImageSamples.webp(width, height))

    private fun picture(side: FlashcardSide) = FlashcardPicture(
        id = 5L,
        side = side,
        mimeType = "image/webp",
        pictureData = byteArrayOf(1),
        pictureSize = 1,
        width = 1,
        height = 1,
        uploadedAt = ZonedDateTime.now(),
        flashcard = flashcard,
    )
}
