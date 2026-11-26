package com.github.elimxim.flashcardsinspace.service

import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

const val THUMBNAIL_MAX_DIMENSION = 400
private const val THUMBNAIL_QUALITY = 0.75f

object PictureThumbnails {
    fun create(image: BufferedImage): ByteArray = encodeWebp(scaleDown(image, THUMBNAIL_MAX_DIMENSION))

    fun create(pictureData: ByteArray): ByteArray {
        val image = ImageIO.read(ByteArrayInputStream(pictureData))
            ?: throw IllegalArgumentException("Picture could not be decoded")
        return create(image)
    }

    internal fun scaleDown(image: BufferedImage, maxDimension: Int): BufferedImage {
        val scale = min(1.0, maxDimension.toDouble() / max(image.width, image.height))
        val targetWidth = max(1, (image.width * scale).roundToInt())
        val targetHeight = max(1, (image.height * scale).roundToInt())

        var current = image
        do {
            val width = max(targetWidth, current.width / 2)
            val height = max(targetHeight, current.height / 2)
            val next = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
            val graphics = next.createGraphics()
            try {
                graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
                graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
                graphics.drawImage(current, 0, 0, width, height, null)
            } finally {
                graphics.dispose()
            }
            current = next
        } while (current.width != targetWidth || current.height != targetHeight)
        return current
    }

    private fun encodeWebp(image: BufferedImage): ByteArray {
        val writer = ImageIO.getImageWritersByMIMEType("image/webp").next()
        try {
            val param = writer.defaultWriteParam.apply {
                compressionMode = ImageWriteParam.MODE_EXPLICIT
                compressionType = "Lossy"
                compressionQuality = THUMBNAIL_QUALITY
            }
            val bytes = ByteArrayOutputStream()
            ImageIO.createImageOutputStream(bytes).use { output ->
                writer.output = output
                writer.write(null, IIOImage(image, null, null), param)
            }
            return bytes.toByteArray()
        } finally {
            writer.dispose()
        }
    }
}
