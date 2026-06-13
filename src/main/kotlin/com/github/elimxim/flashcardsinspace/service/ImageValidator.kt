package com.github.elimxim.flashcardsinspace.service

import com.github.elimxim.flashcardsinspace.web.exception.ApiErrorCode
import com.github.elimxim.flashcardsinspace.web.exception.HttpBadRequestException
import com.github.elimxim.flashcardsinspace.web.exception.HttpPayloadTooLargeException
import com.github.elimxim.flashcardsinspace.web.exception.HttpUnprocessableEntityException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO
import javax.imageio.ImageReader

private val log = LoggerFactory.getLogger(ImageValidator::class.java)

private const val MAX_SIZE_BYTES = 500 * 1024
private const val MAX_DIMENSION = 1600

data class ValidatedImage(
    val bytes: ByteArray,
    val width: Int,
    val height: Int,
)

@Component
class ImageValidator {
    fun validateWebp(bytes: ByteArray): ValidatedImage {
        // 1. payload size (checked before any decode work)
        if (bytes.size > MAX_SIZE_BYTES) {
            throw HttpPayloadTooLargeException(
                ApiErrorCode.FPI413,
                "Picture payload is ${bytes.size} bytes, exceeds the ${MAX_SIZE_BYTES} byte limit"
            )
        }

        // 2. magic-byte WebP detection - this is what enforces "WebP only".
        // A real PNG/JPEG decodes fine and must still be rejected here.
        if (!isWebp(bytes)) {
            throw HttpBadRequestException(
                ApiErrorCode.FPI400,
                "Picture is not a WebP image (RIFF/WEBP magic bytes missing)"
            )
        }

        // 3. dimensions are read from the header FIRST, then checked, and only then
        // is the raster decoded. This is the defense against an "image bomb": a tiny
        // file can declare huge dimensions (WebP allows up to 16383x16383), and a full
        // ImageIO.read would allocate width*height*4 bytes (~1 GB) and OOM the process.
        return decode(bytes)
    }

    private fun decode(bytes: ByteArray): ValidatedImage {
        val imageStream = ImageIO.createImageInputStream(ByteArrayInputStream(bytes))
            ?: throw HttpBadRequestException(ApiErrorCode.FPI400, "Picture could not be decoded")

        imageStream.use { iis ->
            val readers = ImageIO.getImageReaders(iis)
            if (!readers.hasNext()) {
                throw HttpBadRequestException(ApiErrorCode.FPI400, "Picture could not be decoded")
            }
            val reader: ImageReader = readers.next()
            try {
                reader.setInput(iis, true, true)

                // Header-only dimension read (no raster allocation yet).
                val width: Int
                val height: Int
                try {
                    width = reader.getWidth(0)
                    height = reader.getHeight(0)
                } catch (e: Exception) {
                    log.info("Failed to read WebP picture dimensions: ${e.message}")
                    throw HttpBadRequestException(ApiErrorCode.FPI400, "Picture could not be decoded", e)
                }

                // Reject oversized images BEFORE materializing the raster.
                if (width > MAX_DIMENSION || height > MAX_DIMENSION) {
                    throw HttpUnprocessableEntityException(
                        ApiErrorCode.FPI422,
                        "Picture dimensions ${width}x${height} exceed the ${MAX_DIMENSION}px limit"
                    )
                }

                // Now safe: the raster is bounded to MAX_DIMENSION^2 * 4 bytes (~10 MB).
                val image: BufferedImage = try {
                    reader.read(0)
                } catch (e: Exception) {
                    log.info("Failed to decode WebP picture: ${e.message}")
                    throw HttpBadRequestException(ApiErrorCode.FPI400, "Picture could not be decoded", e)
                }

                return ValidatedImage(bytes, image.width, image.height)
            } finally {
                reader.dispose()
            }
        }
    }

    private fun isWebp(bytes: ByteArray): Boolean {
        if (bytes.size < 12) return false
        return bytes[0] == 'R'.code.toByte() &&
            bytes[1] == 'I'.code.toByte() &&
            bytes[2] == 'F'.code.toByte() &&
            bytes[3] == 'F'.code.toByte() &&
            bytes[8] == 'W'.code.toByte() &&
            bytes[9] == 'E'.code.toByte() &&
            bytes[10] == 'B'.code.toByte() &&
            bytes[11] == 'P'.code.toByte()
    }
}
