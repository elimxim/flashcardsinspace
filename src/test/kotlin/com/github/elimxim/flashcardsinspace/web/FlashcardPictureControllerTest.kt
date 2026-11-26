package com.github.elimxim.flashcardsinspace.web

import com.github.elimxim.flashcardsinspace.entity.User
import com.github.elimxim.flashcardsinspace.entity.repository.UserRepository
import com.github.elimxim.flashcardsinspace.security.JwtService
import com.github.elimxim.flashcardsinspace.service.FlashcardPictureService
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import kotlin.test.Test

@WebMvcTest(FlashcardPictureController::class)
class FlashcardPictureControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var flashcardPictureService: FlashcardPictureService

    @MockitoBean
    private lateinit var jwtService: JwtService

    @MockitoBean
    private lateinit var userRepository: UserRepository

    private val mockUser: User = mockk(relaxed = true) {
        every { emailVerified } returns true
        every { id } returns 1L
    }

    @Nested
    @DisplayName("GET /api/flashcard-sets/{setId}/pictures/thumbnails")
    inner class GetThumbnails {

        @Test
        fun `should pass the side and comma-separated ids to the service`() {
            given(flashcardPictureService.getThumbnails(any(), anyLong(), anyString(), any())).willReturn(emptyMap())

            mockMvc.get("/api/flashcard-sets/1/pictures/thumbnails?side=FRONT&ids=4,5") {
                with(user(mockUser))
            }

            then(flashcardPictureService).should().getThumbnails(
                any(),
                anyLong(),
                eq("FRONT"),
                argThat { ids?.map { it.id } == listOf("4", "5") },
            )
        }

        @Test
        fun `should send each thumbnail as a webp part named by its flashcard id`() {
            given(flashcardPictureService.getThumbnails(any(), anyLong(), anyString(), any())).willReturn(
                mapOf(4L to "webp-4".toByteArray(), 5L to "webp-5".toByteArray())
            )

            val result = mockMvc.get("/api/flashcard-sets/1/pictures/thumbnails?side=FRONT&ids=4,5") {
                with(user(mockUser))
            }.andExpect {
                status { isOk() }
            }.andReturn()

            assertThat(MediaType.parseMediaType(result.response.contentType!!).isCompatibleWith(MediaType.MULTIPART_FORM_DATA)).isTrue()
            val body = result.response.contentAsString
            assertThat(body)
                .contains("Content-Disposition: form-data; name=\"4\"; filename=\"4.webp\"")
                .contains("Content-Disposition: form-data; name=\"5\"; filename=\"5.webp\"")
                .contains("Content-Type: image/webp")
                .contains("webp-4")
                .contains("webp-5")
        }

        @Test
        fun `should answer 204 when there are no thumbnails`() {
            given(flashcardPictureService.getThumbnails(any(), anyLong(), anyString(), any())).willReturn(emptyMap())

            mockMvc.get("/api/flashcard-sets/1/pictures/thumbnails?side=FRONT&ids=4") {
                with(user(mockUser))
            }.andExpect {
                status { isNoContent() }
            }
        }

        @Test
        fun `should leave the ids for the validator to reject when they are missing`() {
            given(flashcardPictureService.getThumbnails(any(), anyLong(), anyString(), any())).willReturn(emptyMap())

            mockMvc.get("/api/flashcard-sets/1/pictures/thumbnails?side=FRONT") {
                with(user(mockUser))
            }

            then(flashcardPictureService).should().getThumbnails(any(), anyLong(), anyString(), argThat { ids == null })
        }
    }
}
