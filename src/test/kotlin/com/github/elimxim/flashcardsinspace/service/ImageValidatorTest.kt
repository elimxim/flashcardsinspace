package com.github.elimxim.flashcardsinspace.service

import com.github.elimxim.flashcardsinspace.web.exception.ApiErrorCode
import com.github.elimxim.flashcardsinspace.web.exception.HttpBadRequestException
import com.github.elimxim.flashcardsinspace.web.exception.HttpPayloadTooLargeException
import com.github.elimxim.flashcardsinspace.web.exception.HttpUnprocessableEntityException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class ImageValidatorTest {

    private val validator = ImageValidator()

    @Test
    fun `accepts a valid webp and returns server-measured dimensions`() {
        val result = validator.validateWebp(TestImages.webp(width = 12, height = 9))

        assertThat(result.width).isEqualTo(12)
        assertThat(result.height).isEqualTo(9)
        assertThat(result.bytes).isNotEmpty()
    }

    @Test
    fun `rejects oversize payload with FPI413 before decoding`() {
        assertThatThrownBy { validator.validateWebp(TestImages.oversize()) }
            .isInstanceOf(HttpPayloadTooLargeException::class.java)
            .extracting("apiErrorCode").isEqualTo(ApiErrorCode.FPI413)
    }

    @Test
    fun `rejects a valid PNG on the magic-byte gate with FPI400`() {
        assertThatThrownBy { validator.validateWebp(TestImages.png()) }
            .isInstanceOf(HttpBadRequestException::class.java)
            .extracting("apiErrorCode").isEqualTo(ApiErrorCode.FPI400)
    }

    @Test
    fun `rejects a valid JPEG on the magic-byte gate with FPI400`() {
        assertThatThrownBy { validator.validateWebp(TestImages.jpeg()) }
            .isInstanceOf(HttpBadRequestException::class.java)
            .extracting("apiErrorCode").isEqualTo(ApiErrorCode.FPI400)
    }

    @Test
    fun `rejects non-RIFF garbage with FPI400`() {
        assertThatThrownBy { validator.validateWebp(TestImages.garbage()) }
            .isInstanceOf(HttpBadRequestException::class.java)
            .extracting("apiErrorCode").isEqualTo(ApiErrorCode.FPI400)
    }

    @Test
    fun `rejects a RIFF-WEBP-headed but corrupt payload with FPI400`() {
        assertThatThrownBy { validator.validateWebp(TestImages.corruptWebp()) }
            .isInstanceOf(HttpBadRequestException::class.java)
            .extracting("apiErrorCode").isEqualTo(ApiErrorCode.FPI400)
    }

    @Test
    fun `rejects an over-dimensions webp with FPI422`() {
        assertThatThrownBy { validator.validateWebp(TestImages.webp(width = 2000, height = 2000)) }
            .isInstanceOf(HttpUnprocessableEntityException::class.java)
            .extracting("apiErrorCode").isEqualTo(ApiErrorCode.FPI422)
    }

    @Test
    fun `rejects an image bomb on header dimensions with FPI422 without decoding the raster`() {
        val bomb = TestImages.webpBomb()
        assertThat(bomb.size).isLessThan(64)
        assertThatThrownBy { validator.validateWebp(bomb) }
            .isInstanceOf(HttpUnprocessableEntityException::class.java)
            .extracting("apiErrorCode").isEqualTo(ApiErrorCode.FPI422)
    }
}
