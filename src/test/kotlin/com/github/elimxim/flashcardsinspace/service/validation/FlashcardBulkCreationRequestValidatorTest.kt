package com.github.elimxim.flashcardsinspace.service.validation

import com.github.elimxim.flashcardsinspace.web.dto.FlashcardBulkCreationRequest
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardCreationRequest
import com.github.elimxim.flashcardsinspace.web.exception.HttpInvalidRequestFieldsException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class FlashcardBulkCreationRequestValidatorTest {
    @Autowired
    lateinit var validator: RequestValidator

    @Test
    fun `should pass validation if request is valid`() {
        // given:
        val request = validRequest()

        // when:
        val validRequest = validator.validate(request)

        // then:
        assertThat(validRequest.requests).hasSize(2)
        assertThat(validRequest.requests[0].frontSide).isEqualTo("Front 1")
        assertThat(validRequest.requests[0].backSide).isEqualTo("Back 1")
        assertThat(validRequest.requests[1].frontSide).isEqualTo("Front 2")
        assertThat(validRequest.requests[1].backSide).isEqualTo("Back 2")
    }

    @Test
    fun `should fail validation if requests list is null`() {
        // given:
        val request = FlashcardBulkCreationRequest(requests = null)

        // when:
        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("requests")
    }

    @Test
    fun `should fail validation if requests list is empty`() {
        // given:
        val request = FlashcardBulkCreationRequest(requests = emptyList())

        // when:
        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("requests")
    }

    @Test
    fun `should fail validation if an item has null frontSide`() {
        // given:
        val request = FlashcardBulkCreationRequest(
            requests = listOf(
                FlashcardCreationRequest(
                    frontSide = null,
                    backSide = "Back",
                    stage = "S1",
                    creationDate = "2025-01-01",
                )
            )
        )

        // when:
        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("frontSide")
    }

    @Test
    fun `should fail validation if an item has blank backSide`() {
        // given:
        val request = FlashcardBulkCreationRequest(
            requests = listOf(
                FlashcardCreationRequest(
                    frontSide = "Front",
                    backSide = "   ",
                    stage = "S1",
                    creationDate = "2025-01-01",
                )
            )
        )

        // when:
        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("backSide")
    }

    @Test
    fun `should fail validation if an item frontSide exceeds 512 characters`() {
        // given:
        val request = FlashcardBulkCreationRequest(
            requests = listOf(
                FlashcardCreationRequest(
                    frontSide = "a".repeat(513),
                    backSide = "Back",
                    stage = "S1",
                    creationDate = "2025-01-01",
                )
            )
        )

        // when:
        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("frontSide")
    }

    @Test
    fun `should fail validation if an item backSide exceeds 512 characters`() {
        // given:
        val request = FlashcardBulkCreationRequest(
            requests = listOf(
                FlashcardCreationRequest(
                    frontSide = "Front",
                    backSide = "b".repeat(513),
                    stage = "S1",
                    creationDate = "2025-01-01",
                )
            )
        )

        // when:
        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("backSide")
    }

    private fun validRequest() = FlashcardBulkCreationRequest(
        requests = listOf(
            FlashcardCreationRequest(
                frontSide = "Front 1",
                backSide = "Back 1",
                stage = "S1",
                creationDate = "2025-01-01",
            ),
            FlashcardCreationRequest(
                frontSide = "Front 2",
                backSide = "Back 2",
                stage = "S1",
                creationDate = "2025-01-01",
            ),
        )
    )
}
