package com.github.elimxim.flashcardsinspace.service

import com.github.elimxim.flashcardsinspace.entity.FlashcardAudio
import com.github.elimxim.flashcardsinspace.entity.FlashcardSide
import com.github.elimxim.flashcardsinspace.entity.User
import com.github.elimxim.flashcardsinspace.entity.repository.FlashcardAudioRepository
import com.github.elimxim.flashcardsinspace.entity.sizeKB
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardAudioDto
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardAudioMetadataDto
import com.github.elimxim.flashcardsinspace.web.dto.toDto
import com.github.elimxim.flashcardsinspace.web.exception.ApiErrorCode
import com.github.elimxim.flashcardsinspace.web.exception.HttpBadRequestException
import com.github.elimxim.flashcardsinspace.web.exception.HttpNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.ZonedDateTime

private val log = LoggerFactory.getLogger(FlashcardAudioService::class.java)

@Service
class FlashcardAudioService(
    private val flashcardAudioRepository: FlashcardAudioRepository,
    private val flashcardService: FlashcardService,
    private val flashcardSetService: FlashcardSetService,
) {
    @Transactional(readOnly = true)
    fun getMetadata(user: User, setId: Long): List<FlashcardAudioMetadataDto> {
        log.info("Getting audio metadata for flashcard set $setId")
        val flashcardSet = flashcardSetService.getEntity(setId)
        flashcardSetService.verifyUserHasAccess(user, flashcardSet)

        return flashcardAudioRepository.findAllMetadata(setId).map {
            FlashcardAudioMetadataDto(
                audioId = it.getAudioId(),
                flashcardSide = it.getSide().name,
                flashcardId = it.getFlashcardId(),
            )
        }
    }

    @Transactional(readOnly = true)
    fun fetchAudio(user: User, setId: Long, flashcardId: Long, side: String): FlashcardAudio? {
        log.info("Fetching audio for flashcard $flashcardId in set $setId, side: $side")
        val flashcard = flashcardService.getEntity(flashcardId)
        flashcardSetService.verifyUserHasAccess(user, flashcard.flashcardSet)
        flashcardService.verifyUserOperation(user, setId, flashcard)
        val side = parseSide(side)
        return flashcardAudioRepository.findByFlashcardIdAndSide(flashcardId, side)
    }

    @Transactional(readOnly = true)
    fun fetchAudio(user: User, setId: Long, flashcardId: Long, audioId: Long): FlashcardAudio {
        log.info("Fetching audio $audioId for flashcard $flashcardId in set $setId")
        val flashcard = flashcardService.getEntity(flashcardId)
        flashcardSetService.verifyUserHasAccess(user, flashcard.flashcardSet)
        flashcardService.verifyUserOperation(user, setId, flashcard)
        return getEntity(audioId)
    }

    @Transactional
    fun saveOrUpdateAudio(
        user: User,
        setId: Long,
        flashcardId: Long,
        side: String,
        file: MultipartFile,
    ): FlashcardAudioDto {
        log.info("Uploading audio file ${file.originalFilename} for flashcard $flashcardId in set $setId, size: ${file.size} bytes")
        val flashcard = flashcardService.getEntity(flashcardId)
        flashcardSetService.verifyUserHasAccess(user, flashcard.flashcardSet)
        flashcardService.verifyUserOperation(user, setId, flashcard)

        val flashcardSide = parseSide(side)
        val audioData = file.bytes

        val existingAudio = flashcardAudioRepository.findByFlashcardIdAndSide(flashcardId, flashcardSide)

        val updatedAudio = if (existingAudio != null) {
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
                flashcard = flashcard,
            )
        }

        return flashcardAudioRepository.save(updatedAudio).run {
            log.info("Audio $id saved, side: $side, size: ${sizeKB()} KBs")
            toDto()
        }
    }

    @Transactional
    fun removeAudio(user: User, setId: Long, flashcardId: Long, audioId: Long) {
        log.info("Removing audio $audioId for flashcard $flashcardId in set $setId")
        val flashcard = flashcardService.getEntity(flashcardId)
        flashcardSetService.verifyUserHasAccess(user, flashcard.flashcardSet)
        flashcardService.verifyUserOperation(user, setId, flashcard)

        val audio = getEntity(audioId)
        flashcardAudioRepository.delete(audio)
    }

    @Transactional(readOnly = true)
    fun getEntity(id: Long): FlashcardAudio =
        flashcardAudioRepository.findById(id).orElseThrow {
            HttpNotFoundException(ApiErrorCode.FAU404, "Audio with id $id not found")
        }

    private fun parseSide(side: String) = when {
        FlashcardSide.FRONT.name.equals(side, ignoreCase = true) -> FlashcardSide.FRONT
        FlashcardSide.BACK.name.equals(side, ignoreCase = true) -> FlashcardSide.BACK
        else -> throw HttpBadRequestException(ApiErrorCode.WFS400, "Invalid side $side for uploading audio")
    }
}
