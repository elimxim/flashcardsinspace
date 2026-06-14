package com.github.elimxim.flashcardsinspace.service

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import javax.imageio.ImageIO

/**
 * Runtime image-generation helper for tests.
 */
object TestImages {

    fun webp(width: Int = 8, height: Int = 6): ByteArray {
        val img = solidImage(width, height)
        val baos = ByteArrayOutputStream()
        val written = ImageIO.write(img, "webp", baos)
        check(written) { "WebP writer is not available - cannot generate test image" }
        return baos.toByteArray()
    }

    fun png(width: Int = 8, height: Int = 6): ByteArray {
        val baos = ByteArrayOutputStream()
        check(ImageIO.write(solidImage(width, height), "png", baos)) { "PNG writer unavailable" }
        return baos.toByteArray()
    }

    fun jpeg(width: Int = 8, height: Int = 6): ByteArray {
        val baos = ByteArrayOutputStream()
        check(ImageIO.write(solidImage(width, height), "jpg", baos)) { "JPEG writer unavailable" }
        return baos.toByteArray()
    }

    fun corruptWebp(): ByteArray {
        val junk = ByteArray(32) { 0x7F }
        val baos = ByteArrayOutputStream()
        baos.write("RIFF".toByteArray(StandardCharsets.US_ASCII))
        // little-endian length: junk + "WEBP"
        val len = junk.size + 4
        baos.write(byteArrayOf(
            (len and 0xFF).toByte(),
            ((len shr 8) and 0xFF).toByte(),
            ((len shr 16) and 0xFF).toByte(),
            ((len shr 24) and 0xFF).toByte(),
        ))
        baos.write("WEBP".toByteArray(StandardCharsets.US_ASCII))
        baos.write(junk)
        return baos.toByteArray()
    }

    fun garbage(size: Int = 64): ByteArray = ByteArray(size) { (it % 251).toByte() }

    fun oversize(size: Int = 600 * 1024): ByteArray = ByteArray(size)

    /**
     * An "image bomb": a tiny (~30 byte) WebP whose VP8L header DECLARES huge dimensions
     * but carries no real pixel data. A full ImageIO.read would try to allocate
     * width*height*4 bytes (~1 GB at 16384^2) and OOM the process. A correct validator
     * must read the header dimensions and reject BEFORE decoding the raster.
     */
    fun webpBomb(width: Int = 16384, height: Int = 16384): ByteArray {
        require(width in 1..16384 && height in 1..16384) { "VP8L dimensions are 14-bit (max 16384)" }
        // VP8L bitstream: signature 0x2F, then LSB-first 14b(width-1) | 14b(height-1) | 1b alpha | 3b version
        val bits = ((width - 1).toLong() and 0x3FFF) or (((height - 1).toLong() and 0x3FFF) shl 14)
        val payload = byteArrayOf(
            0x2F,
            (bits and 0xFF).toByte(),
            ((bits shr 8) and 0xFF).toByte(),
            ((bits shr 16) and 0xFF).toByte(),
            ((bits shr 24) and 0xFF).toByte(),
        )
        val padded = if (payload.size % 2 == 1) payload + byteArrayOf(0) else payload
        return ByteArrayOutputStream().use {
            it.write("RIFF".toByteArray(StandardCharsets.US_ASCII))
            it.write(leInt(4 + 4 + 4 + padded.size)) // "WEBP" + "VP8L" + chunk-size field + payload
            it.write("WEBP".toByteArray(StandardCharsets.US_ASCII))
            it.write("VP8L".toByteArray(StandardCharsets.US_ASCII))
            it.write(leInt(payload.size))
            it.write(padded)
            it.toByteArray()
        }
    }

    private fun leInt(v: Int): ByteArray = byteArrayOf(
        (v and 0xFF).toByte(),
        ((v shr 8) and 0xFF).toByte(),
        ((v shr 16) and 0xFF).toByte(),
        ((v shr 24) and 0xFF).toByte(),
    )

    private fun solidImage(width: Int, height: Int): BufferedImage {
        val img = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val g = img.createGraphics()
        g.color = Color(40, 80, 160)
        g.fillRect(0, 0, width, height)
        g.dispose()
        return img
    }
}
