package com.github.elimxim.flashcardsinspace.service.validation

import com.github.elimxim.flashcardsinspace.entity.FlashcardStage
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardUpdateRequest
import com.github.elimxim.flashcardsinspace.web.exception.HttpInvalidRequestFieldsException
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
        val request = validRequest()

        val validRequest = validator.validate(request)

        assertThat(validRequest.frontSide).isEqualTo("Updated front")
        assertThat(validRequest.backSide).isEqualTo("Updated back")
        assertThat(validRequest.stage).isEqualTo(FlashcardStage.S1)
        assertThat(validRequest.timesReviewed).isEqualTo(5)
        assertThat(validRequest.lastReviewDate).isEqualTo(LocalDate.of(2025, 9, 15))
        assertThat(validRequest.reviewHistory?.history).hasSize(1)
        assertThat(validRequest.reviewHistory?.history[0]?.stage).isEqualTo(FlashcardStage.S1)
        assertThat(validRequest.reviewHistory?.history[0]?.reviewDate).isEqualTo(LocalDate.of(2025, 9, 1))
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
    fun `should fail validation if stage is invalid`() {
        val request = validRequest().apply { stage = "INVALID" }

        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        assertThat(exception.fields).containsExactly("stage")
    }

    @Test
    fun `should fail validation if reviewCount is not a number`() {
        val request = validRequest().apply { timesReviewed = "abc" }

        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        assertThat(exception.fields).containsExactly("timesReviewed")
    }

    @Test
    fun `should fail validation if reviewedAt is not a valid date`() {
        val request = validRequest().apply { lastReviewDate = "2025/09/15" }

        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        assertThat(exception.fields).containsExactly("lastReviewDate")
    }

    @Test
    fun `should fail validation if reviewHistory contains invalid stage`() {
        val request = validRequest().apply {
            reviewHistory?.history = listOf(
                FlashcardUpdateRequest.ReviewInfo().apply {
                    stage = "INVALID"
                    reviewDate = "2025-09-01"
                }
            )
        }

        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        assertThat(exception.fields).containsExactly("stage")
    }

    @Test
    fun `should fail validation if reviewHistory contains invalid date`() {
        val request = validRequest().apply {
            reviewHistory = FlashcardUpdateRequest.ReviewHistory(
                history = listOf(FlashcardUpdateRequest.ReviewInfo().apply {
                    stage = "S1"
                    reviewDate = "invalid_date"
                })
            )
        }

        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        assertThat(exception.fields).containsExactly("reviewDate")
    }

    @Test
    fun `should fail validation with multiple invalid fields`() {
        val request = validRequest().apply {
            frontSide = "a".repeat(513)
            timesReviewed = "abc"
            lastReviewDate = "2025-13-01"
        }

        val exception = assertThrows<HttpInvalidRequestFieldsException> {
            validator.validate(request)
        }

        assertThat(exception.fields).containsExactlyInAnyOrder("frontSide", "timesReviewed", "lastReviewDate")
    }

    private fun validRequest(): FlashcardUpdateRequest {
        val reviewInfo = FlashcardUpdateRequest.ReviewInfo().apply {
            stage = "S1"
            reviewDate = "2025-09-01"
        }
        val reviewHistory = FlashcardUpdateRequest.ReviewHistory().apply {
            history = listOf(reviewInfo)
        }
        return FlashcardUpdateRequest().apply {
            frontSide = "Updated front"
            backSide = "Updated back"
            stage = "S1"
            timesReviewed = "5"
            this.reviewHistory = reviewHistory
            lastReviewDate = "2025-09-15"
        }
    }
}
