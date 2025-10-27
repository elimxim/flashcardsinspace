package com.github.elimxim.flashcardsinspace.service

import com.github.elimxim.flashcardsinspace.entity.*
import com.github.elimxim.flashcardsinspace.entity.repository.FlashcardAudioRepository
import com.github.elimxim.flashcardsinspace.entity.repository.FlashcardRepository
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardAudioDto
import com.github.elimxim.flashcardsinspace.web.dto.toDto
import com.github.elimxim.flashcardsinspace.web.exception.AudioNotFoundException
import com.github.elimxim.flashcardsinspace.web.exception.InvalidRequestException
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
        log.info("Fetching audio with ID: $audioId")
        flashcardSetService.verifyUserHasAccess(user, setId)
        flashcardService.verifyUserOperation(user, setId, flashcardId)
        return getEntity(audioId)
    }

    @Transactional
    fun saveAudio(user: User, setId: Long, flashcardId: Long, side: String, file: MultipartFile): FlashcardAudioDto {
        log.info("Uploading audio file: ${file.originalFilename}, size: ${file.size} bytes")
        flashcardSetService.verifyUserHasAccess(user, setId)
        flashcardService.verifyUserOperation(user, setId, flashcardId)
        return saveAudio(flashcardService.getEntity(flashcardId), side, file)
    }

    @Transactional
    fun saveAudio(flashcard: Flashcard, side: String, file: MultipartFile): FlashcardAudioDto {
        val flashcardSide = parseSide(side)
        val audioData = file.bytes
        val audio = FlashcardAudio(
            side = flashcardSide,
            mimeType = file.contentType ?: "audio/webm",
            audioData = audioData,
            audioSize = audioData.size.toLong(),
            uploadedAt = ZonedDateTime.now(),
        )

        val savedAudio = flashcardAudioRepository.save(audio)
        log.info("Audio ${savedAudio.id} saved, side: ${savedAudio.side}, size: ${audio.sizeKB()} KBs")

        if (flashcardSide == FlashcardSide.FRONT) {
            flashcard.frontSideAudioId = savedAudio.id
        } else {
            flashcard.backSideAudioId = savedAudio.id
        }

        flashcardRepository.save(flashcard)

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
        else -> throw InvalidRequestException("Invalid side: $side for uploading audio")
    }
}
