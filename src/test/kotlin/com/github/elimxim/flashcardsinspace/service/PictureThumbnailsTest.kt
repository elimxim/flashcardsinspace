package com.github.elimxim.flashcardsinspace.service

import com.github.elimxim.flashcardsinspace.TestImageSamples
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

class PictureThumbnailsTest {

    @Test
    fun `should scale a landscape picture so its long edge is the thumbnail size`() {
        val thumbnail = PictureThumbnails.scaleDown(BufferedImage(1600, 1200, BufferedImage.TYPE_INT_RGB), 400)

        assertThat(thumbnail.width).isEqualTo(400)
        assertThat(thumbnail.height).isEqualTo(300)
    }

    @Test
    fun `should scale a portrait picture so its long edge is the thumbnail size`() {
        val thumbnail = PictureThumbnails.scaleDown(BufferedImage(900, 1500, BufferedImage.TYPE_INT_RGB), 400)

        assertThat(thumbnail.width).isEqualTo(240)
        assertThat(thumbnail.height).isEqualTo(400)
    }

    @Test
    fun `should not upscale a picture smaller than the thumbnail size`() {
        val thumbnail = PictureThumbnails.scaleDown(BufferedImage(120, 90, BufferedImage.TYPE_INT_RGB), 400)

        assertThat(thumbnail.width).isEqualTo(120)
        assertThat(thumbnail.height).isEqualTo(90)
    }

    @Test
    fun `should keep a very thin picture at least one pixel thick`() {
        val thumbnail = PictureThumbnails.scaleDown(BufferedImage(1600, 2, BufferedImage.TYPE_INT_RGB), 400)

        assertThat(thumbnail.width).isEqualTo(400)
        assertThat(thumbnail.height).isEqualTo(1)
    }

    @Test
    fun `should make a smaller webp from webp picture data`() {
        val picture = TestImageSamples.webp(width = 1600, height = 1200)

        val thumbnail = PictureThumbnails.create(picture)

        assertThat(String(thumbnail, 0, 4)).isEqualTo("RIFF")
        assertThat(String(thumbnail, 8, 4)).isEqualTo("WEBP")
        val decoded = ImageIO.read(ByteArrayInputStream(thumbnail))
        assertThat(decoded.width).isEqualTo(THUMBNAIL_MAX_DIMENSION)
        assertThat(decoded.height).isEqualTo(300)
    }

    @Test
    fun `should fail on data that is not a picture`() {
        assertThatThrownBy { PictureThumbnails.create(TestImageSamples.garbage()) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }
}
