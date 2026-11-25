package com.github.elimxim.flashcardsinspace.service.validation

import com.github.elimxim.flashcardsinspace.entity.FlashcardStage
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardBulkUpdateRequest
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardUpdateRequest
import com.github.elimxim.flashcardsinspace.web.dto.ReviewSessionUpdateRequest
import com.github.elimxim.flashcardsinspace.web.exception.HttpInvalidRequestFieldsException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class FlashcardBulkUpdateRequestValidatorTest {
    @Autowired
    lateinit var validator: RequestValidator

    @Test
    fun `should pass validation if request is valid`() {
        val request = validRequest()

        val validRequest = validator.validate(request)

        assertThat(validRequest.requests.map { it.id }).containsExactly(1L, 2L)
        assertThat(validRequest.requests.map { it.flashcard.stage })
            .containsExactly(FlashcardStage.S1, FlashcardStage.S2)
    }

    @Test
    fun `should fail validation if requests list is null`() {
        val request = FlashcardBulkUpdateRequest(requests = null)

        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        assertThat(exception.fields).containsExactly("requests")
    }

    @Test
    fun `should fail validation if requests list is empty`() {
        val request = FlashcardBulkUpdateRequest(requests = emptyList())

        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        assertThat(exception.fields).containsExactly("requests")
    }

    @Test
    fun `should fail validation if an id is not numeric`() {
        val request = FlashcardBulkUpdateRequest(requests = listOf(item("abc", "S1")))

        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        assertThat(exception.fields).containsExactly("id")
    }

    @Test
    fun `should fail validation if a flashcard is missing`() {
        val request = FlashcardBulkUpdateRequest(
            requests = listOf(FlashcardBulkUpdateRequest.Item(id = "1", flashcard = null))
        )

        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        assertThat(exception.fields).containsExactly("flashcard")
    }

    @Test
    fun `should fail validation if a flashcard has an invalid stage`() {
        val request = FlashcardBulkUpdateRequest(requests = listOf(item("1", "S9")))

        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        assertThat(exception.fields).containsExactly("stage")
    }

    @Test
    fun `should fail validation if the same flashcard is updated twice`() {
        val request = FlashcardBulkUpdateRequest(requests = listOf(item("1", "S1"), item("1", "S2")))

        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        assertThat(exception.fields).containsExactly("id")
    }

    @Test
    fun `should fail validation if a flashcard carries a session request`() {
        val request = FlashcardBulkUpdateRequest(
            requests = listOf(
                FlashcardBulkUpdateRequest.Item(
                    id = "1",
                    flashcard = FlashcardUpdateRequest(
                        stage = "S1",
                        sessionRequest = ReviewSessionUpdateRequest(elapsedTime = "42"),
                    ),
                )
            )
        )

        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        assertThat(exception.fields).containsExactly("sessionRequest")
    }

    private fun validRequest() = FlashcardBulkUpdateRequest(
        requests = listOf(item("1", "S1"), item("2", "S2"))
    )

    private fun item(id: String, stage: String) = FlashcardBulkUpdateRequest.Item(
        id = id,
        flashcard = FlashcardUpdateRequest(stage = stage),
    )
}
