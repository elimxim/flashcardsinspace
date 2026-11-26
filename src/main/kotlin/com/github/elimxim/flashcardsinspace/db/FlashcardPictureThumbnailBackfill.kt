package com.github.elimxim.flashcardsinspace.db

import com.github.elimxim.flashcardsinspace.service.PictureThumbnails
import liquibase.change.custom.CustomTaskChange
import liquibase.database.Database
import liquibase.database.jvm.JdbcConnection
import liquibase.exception.ValidationErrors
import liquibase.resource.ResourceAccessor
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.Timestamp
import java.time.Instant

private val log = LoggerFactory.getLogger(FlashcardPictureThumbnailBackfill::class.java)

class FlashcardPictureThumbnailBackfill : CustomTaskChange {

    private var created = 0
    private var failed = 0

    override fun execute(database: Database) {
        val connection = (database.connection as JdbcConnection).underlyingConnection
        val pictureIds = findPicturesWithoutThumbnail(connection)
        log.info("Creating thumbnails for ${pictureIds.size} pictures")

        val selectSql = "SELECT flashcard_id, side, picture_data FROM flashcard_picture WHERE id = ?"
        val insertSql = """
            INSERT INTO flashcard_picture_thumbnail (flashcard_id, flashcard_picture_id, side, thumbnail_data, thumbnail_size, created_at)
            VALUES (?, ?, ?, ?, ?, ?)
        """.trimIndent()

        connection.prepareStatement(selectSql).use { select ->
            connection.prepareStatement(insertSql).use { insert ->
                for (pictureId in pictureIds) {
                    select.setLong(1, pictureId)
                    select.executeQuery().use { row ->
                        if (!row.next()) return@use
                        val thumbnailData = try {
                            PictureThumbnails.create(row.getBytes("picture_data"))
                        } catch (e: Exception) {
                            log.warn("Failed to create a thumbnail for picture $pictureId", e)
                            failed++
                            return@use
                        }
                        insert.setLong(1, row.getLong("flashcard_id"))
                        insert.setLong(2, pictureId)
                        insert.setString(3, row.getString("side"))
                        insert.setBytes(4, thumbnailData)
                        insert.setLong(5, thumbnailData.size.toLong())
                        insert.setTimestamp(6, Timestamp.from(Instant.now()))
                        insert.executeUpdate()
                        created++
                    }
                }
            }
        }
    }

    private fun findPicturesWithoutThumbnail(connection: Connection): List<Long> {
        val sql = """
            SELECT p.id FROM flashcard_picture p
            WHERE NOT EXISTS (
                SELECT 1 FROM flashcard_picture_thumbnail t
                WHERE t.flashcard_id = p.flashcard_id AND t.side = p.side
            )
            ORDER BY p.id
        """.trimIndent()
        return connection.prepareStatement(sql).use {
            it.executeQuery().use { rows ->
                buildList { while (rows.next()) add(rows.getLong(1)) }
            }
        }
    }

    override fun getConfirmationMessage(): String =
        "Created $created picture thumbnails" + if (failed > 0) ", $failed failed (see the log)" else ""

    override fun setUp() {}

    override fun setFileOpener(resourceAccessor: ResourceAccessor) {}

    override fun validate(database: Database): ValidationErrors = ValidationErrors()
}
