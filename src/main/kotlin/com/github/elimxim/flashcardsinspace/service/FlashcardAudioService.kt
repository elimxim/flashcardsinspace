package com.github.elimxim.flashcardsinspace.service

import com.github.elimxim.flashcardsinspace.entity.FlashcardAudio
import com.github.elimxim.flashcardsinspace.entity.FlashcardSide
import com.github.elimxim.flashcardsinspace.entity.User
import com.github.elimxim.flashcardsinspace.entity.repository.FlashcardAudioRepository
import com.github.elimxim.flashcardsinspace.entity.repository.FlashcardRepository
import com.github.elimxim.flashcardsinspace.entity.sizeKB
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardAudioDto
import com.github.elimxim.flashcardsinspace.web.dto.toDto
import com.github.elimxim.flashcardsinspace.web.exception.AudioNotFoundException
import com.github.elimxim.flashcardsinspace.web.exception.AudioUploadBusyException
import com.github.elimxim.flashcardsinspace.web.exception.FlashcardNotFoundException
import com.github.elimxim.flashcardsinspace.web.exception.InvalidRequestException
import org.hibernate.exception.LockTimeoutException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.ZonedDateTime

private val log = LoggerFactory.getLogger(FlashcardAudioService::class.java)

@Service
class FlashcardAudioService(
    private val flashcardAudioRepository: FlashcardAudioRepository,
    private val flashcardRepository: FlashcardRepository,
    private val flashcardService: FlashcardService,
    private val flashcardSetService: FlashcardSetService,
) {
    @Transactional
    fun fetchAudio(user: User, setId: Long, flashcardId: Long, audioId: Long): FlashcardAudio {
        log.info("Fetching audio $audioId")
        flashcardSetService.verifyUserHasAccess(user, setId)
        flashcardService.verifyUserOperation(user, setId, flashcardId)
        return getEntity(audioId)
    }

    @Transactional
    fun saveOrUpdateAudio(
        user: User,
        setId: Long,
        flashcardId: Long,
        side: String,
        file: MultipartFile
    ): FlashcardAudioDto {
        log.info("Uploading audio file: ${file.originalFilename}, size: ${file.size} bytes")
        flashcardSetService.verifyUserHasAccess(user, setId)
        flashcardService.verifyUserOperation(user, setId, flashcardId)

        return saveOrUpdateAudio(flashcardId, side, file)
    }

    @Transactional
    fun saveOrUpdateAudio(flashcardId: Long, side: String, file: MultipartFile): FlashcardAudioDto {
        val flashcardSide = parseSide(side)
        val audioData = file.bytes

        // Fetch flashcard with pessimistic lock to prevent race conditions
        val flashcard = try {
            flashcardRepository.findByIdWithLock(flashcardId).orElseThrow {
                FlashcardNotFoundException("Flashcard with id $flashcardId not found")
            }
        } catch (e: LockTimeoutException) {
            log.warn("Lock timeout while trying to upload audio for flashcard $flashcardId", e)
            throw AudioUploadBusyException(
                "Another audio upload is in progress for flashcard $flashcardId. Please try again.", e
            )
        }

        val audioId = if (flashcardSide == FlashcardSide.FRONT) {
            flashcard.frontSideAudioId
        } else {
            flashcard.backSideAudioId
        }

        val updatedAudio = if (audioId != null) {
            val existingAudio = getEntity(audioId)
            existingAudio.mimeType = file.contentType
            existingAudio.audioData = audioData
            existingAudio.audioSize = audioData.size.toLong()
            existingAudio.uploadedAt = ZonedDateTime.now()
            existingAudio
        } else {
            FlashcardAudio(
                side = flashcardSide,
                mimeType = file.contentType,
                audioData = audioData,
                audioSize = audioData.size.toLong(),
                uploadedAt = ZonedDateTime.now(),
            )
        }

        val savedAudio = flashcardAudioRepository.save(updatedAudio)
        log.info("Audio ${savedAudio.id} saved, side: ${savedAudio.side}, size: ${updatedAudio.sizeKB()} KBs")

        if (flashcardSide == FlashcardSide.FRONT) {
            flashcardRepository.updateFrontSideAudioId(flashcardId, savedAudio.id)
        } else {
            flashcardRepository.updateBackSideAudioId(flashcardId, savedAudio.id)
        }

        return savedAudio.toDto()
    }

    @Transactional
    fun removeAudio(user: User, setId: Long, flashcardId: Long, audioId: Long) {
        log.info("Removing audio with ID: $audioId")
        flashcardSetService.verifyUserHasAccess(user, setId)
        flashcardService.verifyUserOperation(user, setId, flashcardId)

        val audio = getEntity(audioId)
        val flashcard = flashcardService.getEntity(flashcardId)

        if (audio.side == FlashcardSide.FRONT) {
            flashcard.frontSideAudioId = null
            flashcardRepository.save(flashcard)
        } else {
            flashcard.backSideAudioId = null
            flashcardRepository.save(flashcard)
        }

        flashcardAudioRepository.delete(audio)
    }

    @Transactional
    fun getEntity(id: Long): FlashcardAudio =
        flashcardAudioRepository.findById(id).orElseThrow {
            AudioNotFoundException("Audio not found with ID: $id")
        }

    private fun parseSide(side: String) = when {
        FlashcardSide.FRONT.name.equals(side, ignoreCase = true) -> FlashcardSide.FRONT
        FlashcardSide.BACK.name.equals(side, ignoreCase = true) -> FlashcardSide.BACK
        else -> throw InvalidRequestException("Invalid side $side for uploading audio")
    }
}
