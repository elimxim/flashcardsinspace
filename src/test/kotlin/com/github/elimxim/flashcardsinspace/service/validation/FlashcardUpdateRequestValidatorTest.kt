package com.github.elimxim.flashcardsinspace.service.validation

import com.github.elimxim.flashcardsinspace.entity.FlashcardStage
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardUpdateRequest
import com.github.elimxim.flashcardsinspace.web.exception.InvalidRequestFieldsException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDate

@SpringBootTest
class FlashcardUpdateRequestValidatorTest {
    @Autowired
    private lateinit var validator: RequestValidator

    @Test
    fun `should pass validation if request is valid`() {
        // given:
        val request = validRequest()

        // when:
        val validRequest = validator.validate(request)

        // then:
        assertThat(validRequest.id).isEqualTo(123L)
        assertThat(validRequest.frontSide).isEqualTo("Updated front")
        assertThat(validRequest.backSide).isEqualTo("Updated back")
        assertThat(validRequest.stage).isEqualTo(FlashcardStage.S1)
        assertThat(validRequest.timesReviewed).isEqualTo(5)
        assertThat(validRequest.lastReviewDate).isEqualTo(LocalDate.of(2025, 9, 15))
        assertThat(validRequest.reviewHistory.history).hasSize(1)
        assertThat(validRequest.reviewHistory.history[0].stage).isEqualTo(FlashcardStage.S1)
        assertThat(validRequest.reviewHistory.history[0].reviewDate).isEqualTo(LocalDate.of(2025, 9, 1))
    }

    @Test
    fun `should fail validation if id is null`() {
        // given:
        val request = validRequest().apply {
            id = null
        }

        // when:
        val exception = assertThrows<InvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("id")
    }

    @Test
    fun `should fail validation if id is not a number`() {
        // given:
        val request = validRequest().apply {
            id = "abc"
        }

        // when:
        val exception = assertThrows<InvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("id")
    }

    @Test
    fun `should fail validation if frontSide is null`() {
        // given:
        val request = validRequest().apply {
            frontSide = null
        }

        // when:
        val exception = assertThrows<InvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("frontSide")
    }

    @Test
    fun `should fail validation if frontSide is too long`() {
        // given:
        val request = validRequest().apply {
            frontSide = "a".repeat(513)
        }

        // when:
        val exception = assertThrows<InvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("frontSide")
    }

    @Test
    fun `should fail validation if backSide is blank`() {
        // given:
        val request = validRequest().apply {
            backSide = "   "
        }

        // when:
        val exception = assertThrows<InvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("backSide")
    }

    @Test
    fun `should fail validation if backSide is too long`() {
        // given:
        val request = validRequest().apply {
            backSide = "a".repeat(513)
        }

        // when:
        val exception = assertThrows<InvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("backSide")
    }

    @Test
    fun `should fail validation if stage is invalid`() {
        // given:
        val request = validRequest().apply { stage = "INVALID" }

        // when:
        val exception = assertThrows<InvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("stage")
    }

    @Test
    fun `should fail validation if reviewCount is not a number`() {
        // given:
        val request = validRequest().apply { reviewCount = "abc" }

        // when:
        val exception = assertThrows<InvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("reviewCount")
    }

    @Test
    fun `should fail validation if reviewedAt is not a valid date`() {
        // given:
        val request = validRequest().apply { reviewedAt = "2025/09/15" }

        // when:
        val exception = assertThrows<InvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("reviewedAt")
    }

    @Test
    fun `should fail validation if reviewHistory is null`() {
        // given:
        val request = validRequest().apply { reviewHistory = null }

        // when:
        val exception = assertThrows<InvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("reviewHistory")
    }

    @Test
    fun `should fail validation if reviewHistory history is empty`() {
        // given:
        val request =
            validRequest().apply { reviewHistory = FlashcardUpdateRequest.ReviewHistory(history = emptyList()) }

        // when:
        val exception = assertThrows<InvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("history")
    }

    @Test
    fun `should fail validation if reviewHistory contains invalid stage`() {
        // given:
        val invalidReviewInfo = FlashcardUpdateRequest.ReviewInfo().apply {
            stage = "INVALID"
            reviewedAt = "2025-09-01"
        }
        val request = validRequest().apply {
            reviewHistory?.history = listOf(invalidReviewInfo)
        }

        // when:
        val exception = assertThrows<InvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("stage")
    }

    @Test
    fun `should fail validation if reviewHistory contains invalid date`() {
        // given:
        val invalidReviewInfo = FlashcardUpdateRequest.ReviewInfo().apply {
            stage = "S1"
            reviewedAt = "invalid-date"
        }
        val request = validRequest().apply {
            reviewHistory?.history = listOf(invalidReviewInfo)
        }

        // when:
        val exception = assertThrows<InvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactly("reviewedAt")
    }

    @Test
    fun `should fail validation with multiple invalid fields`() {
        // given:
        val request = validRequest().apply {
            id = "abc"
            frontSide = " ".repeat(513)
            reviewCount = null
            reviewedAt = "2025-13-01"
        }

        // when:
        val exception = assertThrows<InvalidRequestFieldsException> {
            validator.validate(request)
        }

        // then:
        assertThat(exception.fields).containsExactlyInAnyOrder("id", "frontSide", "reviewCount", "reviewedAt")
    }


    private fun validRequest(): FlashcardUpdateRequest {
        val reviewInfo = FlashcardUpdateRequest.ReviewInfo().apply {
            stage = "S1"
            reviewedAt = "2025-09-01"
        }
        val reviewHistory = FlashcardUpdateRequest.ReviewHistory().apply {
            history = listOf(reviewInfo)
        }
        return FlashcardUpdateRequest().apply {
            id = "123"
            frontSide = "Updated front"
            backSide = "Updated back"
            stage = "S1"
            reviewCount = "5"
            this.reviewHistory = reviewHistory
            reviewedAt = "2025-09-15"
        }
    }
}
