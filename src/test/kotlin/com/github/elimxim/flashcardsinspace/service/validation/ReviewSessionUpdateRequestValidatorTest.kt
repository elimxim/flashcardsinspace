package com.github.elimxim.flashcardsinspace.service.validation

import com.github.elimxim.flashcardsinspace.web.dto.ReviewSessionUpdateRequest
import com.github.elimxim.flashcardsinspace.web.exception.HttpInvalidRequestFieldsException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ReviewSessionUpdateRequestValidatorTest {
    @Autowired
    private lateinit var validator: RequestValidator

    @Test
    fun `should pass validation if request is valid`() {
        val request = validRequest()

        val validRequest = validator.validate(request)

        assertThat(validRequest.elapsedTime).isEqualTo(1000L)
        assertThat(validRequest.flashcardIds).containsExactly(1L, 2L)
    }

    @Test
    fun `should fail validation if flashcardIds contains non-numeric value`() {
        val request = validRequest().copy(
            flashcardIds = listOf(
                ReviewSessionUpdateRequest.FlashcardId("1"),
                ReviewSessionUpdateRequest.FlashcardId("abc"),
            )
        )

        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        assertThat(exception.fields).containsExactly("id")
    }

    private fun validRequest() = ReviewSessionUpdateRequest(
        elapsedTime = "1000",
        flashcardIds = listOf(
            ReviewSessionUpdateRequest.FlashcardId("1"),
            ReviewSessionUpdateRequest.FlashcardId("2"),
        ),
    )
}
