package com.github.elimxim.flashcardsinspace.web

import com.github.elimxim.flashcardsinspace.entity.User
import com.github.elimxim.flashcardsinspace.entity.repository.UserRepository
import com.github.elimxim.flashcardsinspace.security.JwtService
import com.github.elimxim.flashcardsinspace.service.FlashcardService
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardDto
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardUpdateResponse
import com.github.elimxim.flashcardsinspace.web.dto.ReviewHistoryDto
import com.github.elimxim.flashcardsinspace.web.dto.ReviewSessionDto
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.BDDMockito.given
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.put
import java.time.LocalDate
import java.time.ZonedDateTime
import kotlin.test.Test

@WebMvcTest(FlashcardController::class)
class FlashcardControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var flashcardService: FlashcardService

    @MockitoBean
    private lateinit var jwtService: JwtService

    @MockitoBean
    private lateinit var userRepository: UserRepository

    private val mockUser: User = mockk(relaxed = true) {
        every { emailVerified } returns true
        every { id } returns 1L
    }

    @Nested
    @DisplayName("GET /api/flashcard-sets/{setId}/flashcards")
    inner class GetFlashcards {

        @Test
        fun `should return 200 with page content when total fits in one page`() {
            val flashcards = listOf(flashcardDto(1L), flashcardDto(2L))
            val page = PageImpl(flashcards, PageRequest.of(0, 256), flashcards.size.toLong())
            given(flashcardService.getAll(any(), anyLong(), any())).willReturn(page)

            val result = mockMvc.get("/api/flashcard-sets/1/flashcards") {
                with(user(mockUser))
            }.andExpect {
                status { isOk() }
            }.andReturn()

            assertThat(result.response.contentAsString)
                .contains("\"last\":true")
                .contains("\"totalElements\":2")
        }

        @Test
        fun `should return last=false when total exceeds page size`() {
            val flashcards = (1..256).map { flashcardDto(it.toLong()) }
            val page = PageImpl(flashcards, PageRequest.of(0, 256), 512L)
            given(flashcardService.getAll(any(), anyLong(), any())).willReturn(page)

            val result = mockMvc.get("/api/flashcard-sets/1/flashcards") {
                with(user(mockUser))
            }.andExpect {
                status { isOk() }
            }.andReturn()

            assertThat(result.response.contentAsString)
                .contains("\"last\":false")
                .contains("\"totalPages\":2")
        }

        @Test
        fun `should use default page size of 256 when no params given`() {
            val page = PageImpl<FlashcardDto>(emptyList(), PageRequest.of(0, 256), 0L)
            given(flashcardService.getAll(any(), anyLong(), any())).willReturn(page)

            val result = mockMvc.get("/api/flashcard-sets/1/flashcards") {
                with(user(mockUser))
            }.andExpect {
                status { isOk() }
            }.andReturn()

            assertThat(result.response.contentAsString)
                .contains("\"size\":256")
        }
    }

    @Nested
    @DisplayName("PUT /api/flashcard-sets/{setId}/flashcards/{id}")
    inner class UpdateFlashcard {

        @Test
        fun `should return flashcard fields at the top level when no session is attached`() {
            given(flashcardService.update(any(), anyLong(), anyLong(), any(), anyOrNull()))
                .willReturn(FlashcardUpdateResponse(flashcardDto(1L)))

            val result = mockMvc.put("/api/flashcard-sets/1/flashcards/1") {
                with(user(mockUser))
                with(csrf())
                contentType = MediaType.APPLICATION_JSON
                content = "{\"stage\":\"S2\"}"
            }.andExpect {
                status { isOk() }
            }.andReturn()

            assertThat(result.response.contentAsString)
                .contains("\"id\":1")
                .contains("\"stage\":\"S1\"")
                .doesNotContain("\"flashcard\"")
                .doesNotContain("\"session\"")
        }

        @Test
        fun `should return the session alongside the flashcard fields when a session is attached`() {
            given(flashcardService.update(any(), anyLong(), anyLong(), any(), anyOrNull()))
                .willReturn(FlashcardUpdateResponse(flashcardDto(1L), reviewSessionDto(7L)))

            val result = mockMvc.put("/api/flashcard-sets/1/flashcards/1?sessionId=7") {
                with(user(mockUser))
                with(csrf())
                contentType = MediaType.APPLICATION_JSON
                content = "{\"stage\":\"S2\",\"sessionRequest\":{\"elapsedTime\":\"42\"}}"
            }.andExpect {
                status { isOk() }
            }.andReturn()

            assertThat(result.response.contentAsString)
                .contains("\"id\":1")
                .contains("\"session\":{\"id\":7")
                .doesNotContain("\"flashcard\"")
        }
    }
}

private fun flashcardDto(id: Long) = FlashcardDto(
    id = id,
    frontSide = "front $id",
    backSide = "back $id",
    stage = "S1",
    timesReviewed = 0,
    reviewHistory = ReviewHistoryDto(history = emptyList()),
    creationDate = LocalDate.of(2024, 1, 1),
    lastReviewDate = null,
    lastUpdatedAt = null,
)

private fun reviewSessionDto(id: Long) = ReviewSessionDto(
    id = id,
    type = "LIGHTSPEED",
    flashcardIds = listOf(1L),
    elapsedTime = 42,
    startedAt = ZonedDateTime.parse("2024-01-01T00:00:00Z"),
    finishedAt = null,
    lastUpdatedAt = null,
    metadata = null,
)
