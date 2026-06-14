package com.github.elimxim.flashcardsinspace.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

class WebpCodecAvailabilityTest {

    @Test
    fun `webp writer plugin is registered with ImageIO`() {
        assertThat(ImageIO.getImageWritersByFormatName("webp").hasNext()).isTrue()
    }

    @Test
    fun `webp reader plugin is registered with ImageIO`() {
        assertThat(ImageIO.getImageReadersByFormatName("webp").hasNext()).isTrue()
    }

    @Test
    fun `round-trips a BufferedImage through webp encode and decode`() {
        val source = BufferedImage(8, 6, BufferedImage.TYPE_INT_RGB)

        ByteArrayOutputStream().use {
            val written = ImageIO.write(source, "webp", it)
            assertThat(written).isTrue()
            val decoded = ImageIO.read(ByteArrayInputStream(it.toByteArray()))
            assertThat(decoded).isNotNull()
            assertThat(decoded.width).isEqualTo(8)
            assertThat(decoded.height).isEqualTo(6)
        }
    }
}
