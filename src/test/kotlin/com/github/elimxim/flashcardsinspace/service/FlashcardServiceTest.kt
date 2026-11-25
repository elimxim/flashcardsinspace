package com.github.elimxim.flashcardsinspace.service

import com.github.elimxim.flashcardsinspace.entity.Flashcard
import com.github.elimxim.flashcardsinspace.entity.FlashcardSet
import com.github.elimxim.flashcardsinspace.entity.FlashcardStage
import com.github.elimxim.flashcardsinspace.entity.User
import com.github.elimxim.flashcardsinspace.entity.repository.FlashcardAudioRepository
import com.github.elimxim.flashcardsinspace.entity.repository.FlashcardPictureRepository
import com.github.elimxim.flashcardsinspace.entity.repository.FlashcardRepository
import com.github.elimxim.flashcardsinspace.service.validation.RequestValidator
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardBulkRemovalRequest
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardBulkUpdateRequest
import com.github.elimxim.flashcardsinspace.web.dto.ValidFlashcardBulkRemovalRequest
import com.github.elimxim.flashcardsinspace.web.dto.ValidFlashcardBulkUpdateRequest
import com.github.elimxim.flashcardsinspace.web.dto.ValidFlashcardUpdateRequest
import com.github.elimxim.flashcardsinspace.web.exception.ApiErrorCode
import com.github.elimxim.flashcardsinspace.web.exception.HttpBadRequestException
import com.github.elimxim.flashcardsinspace.web.exception.HttpConflictException
import com.github.elimxim.flashcardsinspace.web.exception.HttpNotFoundException
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

private const val SET_ID = 1L
private const val OTHER_SET_ID = 2L

class FlashcardServiceTest {
    private val flashcardSetService = mockk<FlashcardSetService>(relaxed = true)
    private val flashcardRepository = mockk<FlashcardRepository>(relaxed = true)
    private val flashcardAudioRepository = mockk<FlashcardAudioRepository>(relaxed = true)
    private val flashcardPictureRepository = mockk<FlashcardPictureRepository>(relaxed = true)
    private val requestValidator = mockk<RequestValidator>()

    private val service = FlashcardService(
        flashcardSetService = flashcardSetService,
        flashcardRepository = flashcardRepository,
        flashcardAudioRepository = flashcardAudioRepository,
        flashcardPictureRepository = flashcardPictureRepository,
        requestValidator = requestValidator,
        flashcardAddService = mockk(),
        reviewSessionService = mockk(),
    )

    private val user = mockk<User>(relaxed = true) {
        every { emailVerified } returns true
        every { id } returns 1L
    }
    private val flashcardSet = flashcardSet(SET_ID)

    @BeforeEach
    fun setUp() {
        every { flashcardSetService.getEntity(SET_ID) } returns flashcardSet
        every { flashcardRepository.save(any<Flashcard>()) } answers { firstArg() }
    }

    @Nested
    inner class UpdateBulk {

        @Test
        fun `should update every flashcard and return them in request order`() {
            givenFlashcards(flashcard(10L, FlashcardStage.OUTER_SPACE), flashcard(11L, FlashcardStage.OUTER_SPACE))
            givenUpdates(11L to stageUpdate(FlashcardStage.S1), 10L to stageUpdate(FlashcardStage.S1))

            val response = service.updateBulk(user, SET_ID, FlashcardBulkUpdateRequest())

            assertThat(response.flashcards.map { it.id }).containsExactly(11L, 10L)
            assertThat(response.flashcards.map { it.stage }).containsOnly(FlashcardStage.S1.name)
            verify(exactly = 2) { flashcardRepository.save(any<Flashcard>()) }
        }

        @Test
        fun `should change nothing when one flashcard is missing`() {
            givenFlashcards(flashcard(10L))
            givenUpdates(10L to stageUpdate(FlashcardStage.S2), 99L to stageUpdate(FlashcardStage.S2))

            val exception = assertThrows<HttpNotFoundException> {
                service.updateBulk(user, SET_ID, FlashcardBulkUpdateRequest())
            }

            assertThat(exception.apiErrorCode).isEqualTo(ApiErrorCode.FLA404)
            verify(exactly = 0) { flashcardRepository.save(any<Flashcard>()) }
        }

        @Test
        fun `should change nothing when one flashcard belongs to another set`() {
            givenFlashcards(flashcard(10L), flashcard(11L, flashcardSet = flashcardSet(OTHER_SET_ID)))
            givenUpdates(10L to stageUpdate(FlashcardStage.S2), 11L to stageUpdate(FlashcardStage.S2))

            assertThrows<HttpConflictException> {
                service.updateBulk(user, SET_ID, FlashcardBulkUpdateRequest())
            }

            verify(exactly = 0) { flashcardRepository.save(any<Flashcard>()) }
        }

        @Test
        fun `should change nothing when a review change hits a suspended set`() {
            every { flashcardSetService.verifyNotSuspended(flashcardSet) } throws
                    HttpBadRequestException(ApiErrorCode.FSS400, "suspended")
            givenFlashcards(flashcard(10L), flashcard(11L))
            givenUpdates(
                10L to ValidFlashcardUpdateRequest(frontSide = "new front"),
                11L to stageUpdate(FlashcardStage.S2),
            )

            assertThrows<HttpBadRequestException> {
                service.updateBulk(user, SET_ID, FlashcardBulkUpdateRequest())
            }

            verify(exactly = 0) { flashcardRepository.save(any<Flashcard>()) }
        }

        @Test
        fun `should allow text changes in a suspended set`() {
            every { flashcardSetService.verifyNotSuspended(flashcardSet) } throws
                    HttpBadRequestException(ApiErrorCode.FSS400, "suspended")
            givenFlashcards(flashcard(10L))
            givenUpdates(10L to ValidFlashcardUpdateRequest(frontSide = "new front"))

            val response = service.updateBulk(user, SET_ID, FlashcardBulkUpdateRequest())

            assertThat(response.flashcards.single().frontSide).isEqualTo("new front")
        }

        private fun givenUpdates(vararg updates: Pair<Long, ValidFlashcardUpdateRequest>) {
            every { requestValidator.validate(any<FlashcardBulkUpdateRequest>()) } returns
                    ValidFlashcardBulkUpdateRequest(
                        updates.map { (id, update) -> ValidFlashcardBulkUpdateRequest.Item(id, update) }
                    )
        }
    }

    @Nested
    inner class RemoveBulk {

        @Test
        fun `should remove every flashcard with its audio and pictures`() {
            val flashcards = listOf(flashcard(10L), flashcard(11L))
            givenFlashcards(*flashcards.toTypedArray())
            givenRemovals(10L, 11L)

            service.removeBulk(user, SET_ID, FlashcardBulkRemovalRequest())

            verify { flashcardAudioRepository.deleteByFlashcardIdIn(match { it.toSet() == setOf(10L, 11L) }) }
            verify { flashcardPictureRepository.deleteByFlashcardIdIn(match { it.toSet() == setOf(10L, 11L) }) }
            verify { flashcardRepository.deleteAll(match<Iterable<Flashcard>> { it.toSet() == flashcards.toSet() }) }
        }

        @Test
        fun `should remove nothing when one flashcard is missing`() {
            givenFlashcards(flashcard(10L))
            givenRemovals(10L, 99L)

            assertThrows<HttpNotFoundException> {
                service.removeBulk(user, SET_ID, FlashcardBulkRemovalRequest())
            }

            verifyNothingRemoved()
        }

        @Test
        fun `should remove nothing when one flashcard belongs to another set`() {
            givenFlashcards(flashcard(10L), flashcard(11L, flashcardSet = flashcardSet(OTHER_SET_ID)))
            givenRemovals(10L, 11L)

            assertThrows<HttpConflictException> {
                service.removeBulk(user, SET_ID, FlashcardBulkRemovalRequest())
            }

            verifyNothingRemoved()
        }

        private fun givenRemovals(vararg ids: Long) {
            every { requestValidator.validate(any<FlashcardBulkRemovalRequest>()) } returns
                    ValidFlashcardBulkRemovalRequest(ids.toList())
        }

        private fun verifyNothingRemoved() {
            verify(exactly = 0) { flashcardAudioRepository.deleteByFlashcardIdIn(any()) }
            verify(exactly = 0) { flashcardPictureRepository.deleteByFlashcardIdIn(any()) }
            verify(exactly = 0) { flashcardRepository.deleteAll(any<Iterable<Flashcard>>()) }
        }
    }

    private fun givenFlashcards(vararg flashcards: Flashcard) {
        every { flashcardRepository.findAllById(any()) } returns flashcards.toList()
    }

    private fun stageUpdate(stage: FlashcardStage) = ValidFlashcardUpdateRequest(stage = stage)

    private fun flashcard(
        id: Long,
        stage: FlashcardStage = FlashcardStage.S1,
        flashcardSet: FlashcardSet = this.flashcardSet,
    ) = Flashcard(
        id = id,
        frontSide = "front $id",
        backSide = "back $id",
        stage = stage,
        timesReviewed = 0,
        creationDate = LocalDate.of(2025, 1, 1),
        flashcardSet = flashcardSet,
    )
}

private fun flashcardSet(id: Long) = mockk<FlashcardSet>(relaxed = true) {
    every { this@mockk.id } returns id
}
