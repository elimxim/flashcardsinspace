package com.github.elimxim.flashcardsinspace.service.validation

import com.github.elimxim.flashcardsinspace.web.dto.FlashcardPictureThumbnailsRequest
import com.github.elimxim.flashcardsinspace.web.dto.MAX_THUMBNAILS_PER_REQUEST
import com.github.elimxim.flashcardsinspace.web.exception.HttpInvalidRequestFieldsException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class FlashcardPictureThumbnailsRequestValidatorTest {
    @Autowired
    lateinit var validator: RequestValidator

    @Test
    fun `should pass validation if request is valid`() {
        val request = request("1", "2", "3")

        val validRequest = validator.validate(request)

        assertThat(validRequest.ids).containsExactly(1L, 2L, 3L)
    }

    @Test
    fun `should drop repeated ids`() {
        val request = request("1", "2", "1")

        val validRequest = validator.validate(request)

        assertThat(validRequest.ids).containsExactly(1L, 2L)
    }

    @Test
    fun `should fail validation if ids is null`() {
        val request = FlashcardPictureThumbnailsRequest(ids = null)

        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        assertThat(exception.fields).containsExactly("ids")
    }

    @Test
    fun `should fail validation if ids is empty`() {
        val request = FlashcardPictureThumbnailsRequest(ids = emptyList())

        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        assertThat(exception.fields).containsExactly("ids")
    }

    @Test
    fun `should fail validation if there are more ids than one page allows`() {
        val request = request(*Array(MAX_THUMBNAILS_PER_REQUEST + 1) { "${it + 1}" })

        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        assertThat(exception.fields).containsExactly("ids")
    }

    @Test
    fun `should fail validation if an id is not numeric`() {
        val request = request("1", "abc")

        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        assertThat(exception.fields).containsExactly("id")
    }

    private fun request(vararg ids: String) = FlashcardPictureThumbnailsRequest(
        ids = ids.map { FlashcardPictureThumbnailsRequest.FlashcardId(it) }
    )
}
