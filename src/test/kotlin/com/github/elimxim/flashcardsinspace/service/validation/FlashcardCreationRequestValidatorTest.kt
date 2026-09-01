package com.github.elimxim.flashcardsinspace.service.validation

import com.github.elimxim.flashcardsinspace.entity.FlashcardStage
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardCreationRequest
import com.github.elimxim.flashcardsinspace.web.exception.HttpInvalidRequestFieldsException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDate

@SpringBootTest
class FlashcardCreationRequestValidatorTest {
    @Autowired
    lateinit var validator: RequestValidator

    @Test
    fun `should pass validation if request is valid`() {
        val request = validRequest()

        val validRequest = validator.validate(request)

        assertThat(validRequest.frontSide).isEqualTo("What is the largest black hole?")
        assertThat(validRequest.backSide).isEqualTo("TON\n618")
        assertThat(validRequest.stage).isEqualTo(FlashcardStage.S1)
        assertThat(validRequest.creationDate).isEqualTo(LocalDate.of(2025, 1, 1))
    }

    @Test
    fun `should pass validation if frontSide is null (picture-only side)`() {
        // Text may be null when the side carries a picture instead (picture XOR text).
        val request = validRequest().apply {
            frontSide = null
        }

        val validRequest = validator.validate(request)

        assertThat(validRequest.frontSide).isNull()
    }

    @Test
    fun `should pass validation if frontSide is blank (picture-only side)`() {
        // Text may be blank when the side carries a picture instead (picture XOR text).
        val request = validRequest().apply {
            frontSide = ""
        }

        val validRequest = validator.validate(request)

        assertThat(validRequest.frontSide).isEmpty()
    }

    @Test
    fun `should fail validation if frontSide is too long`() {
        val request = validRequest().apply {
            frontSide = "a".repeat(513)
        }

        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        assertThat(exception.fields).containsExactly("frontSide")
    }

    @Test
    fun `should pass validation if backSide is null (picture-only side)`() {
        // Text may be null when the side carries a picture instead (picture XOR text).
        val request = validRequest().apply {
            backSide = null
        }

        val validRequest = validator.validate(request)

        assertThat(validRequest.backSide).isNull()
    }

    @Test
    fun `should pass validation if backSide is blank (picture-only side)`() {
        // Text may be blank when the side carries a picture instead (picture XOR text).
        val request = validRequest().apply {
            backSide = ""
        }

        val validRequest = validator.validate(request)

        assertThat(validRequest.backSide).isEmpty()
    }

    @Test
    fun `should fail validation if backSide is too long`() {
        val request = validRequest().apply {
            backSide = "a".repeat(513)
        }

        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        assertThat(exception.fields).containsExactly("backSide")
    }

    @Test
    fun `should fail validation if stage is null`() {
        val request = validRequest().apply {
            stage = null
        }

        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        assertThat(exception.fields).containsExactly("stage")
    }

    @Test
    fun `should fail validation if stage is empty`() {
        val request = validRequest().apply {
            stage = ""
        }

        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        assertThat(exception.fields).containsExactly("stage")
    }

    @Test
    fun `should fail validation if stage is blank`() {
        val request = validRequest().apply {
            stage = "   "
        }

        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        assertThat(exception.fields).containsExactly("stage")
    }

    @Test
    fun `should fail validation if stage is invalid`() {
        val request = validRequest().apply {
            stage = "INVALID_STAGE"
        }

        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        assertThat(exception.fields).containsExactly("stage")
    }

    @Test
    fun `should fail validation if createdAt is null`() {
        val request = validRequest().apply {
            creationDate = null
        }

        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        assertThat(exception.fields).containsExactly("creationDate")
    }

    @Test
    fun `should fail validation if createdAt is empty`() {
        val request = validRequest().apply {
            creationDate = ""
        }

        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        assertThat(exception.fields).containsExactly("creationDate")
    }

    @Test
    fun `should fail validation if createdAt is blank`() {
        val request = validRequest().apply {
            creationDate = "   "
        }

        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        assertThat(exception.fields).containsExactly("creationDate")
    }

    @Test
    fun `should fail validation if createdAt has invalid format`() {
        val request = validRequest().apply {
            creationDate = "2025/01/01"
        }

        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        assertThat(exception.fields).containsExactly("creationDate")
    }

    @Test
    fun `should fail validation if all fields are invalid`() {
        val request = FlashcardCreationRequest().apply {
            frontSide = "a".repeat(513)
            backSide = "b".repeat(513)
            stage = " "
            creationDate = "invalid-date"
        }

        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        assertThat(exception.fields).containsExactlyInAnyOrder("frontSide", "backSide", "stage", "creationDate")
    }

    private fun validRequest() = FlashcardCreationRequest(
        frontSide = "What is the largest black hole?",
        backSide = "TON\n618",
        stage = "S1",
        creationDate = "2025-01-01"
    )
}
