package com.github.elimxim.flashcardsinspace.service

import com.github.elimxim.flashcardsinspace.entity.*
import com.github.elimxim.flashcardsinspace.entity.repository.FlashcardSetRepository
import com.github.elimxim.flashcardsinspace.service.validation.RequestValidator
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardCreationRequest
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardSetInitResponse
import com.github.elimxim.flashcardsinspace.web.dto.ValidFlashcardCreationRequest
import com.github.elimxim.flashcardsinspace.web.dto.toDto
import com.github.elimxim.flashcardsinspace.web.exception.FlashcardsSetAlreadyStartedException
import org.slf4j.LoggerFactory.getLogger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZonedDateTime

private val log = getLogger(FlashcardSetInitService::class.java)

@Service
class FlashcardSetInitService(
    private val flashcardSetService: FlashcardSetService,
    private val flashcardSetRepository: FlashcardSetRepository,
    private val lightspeedService: LightspeedService,
    private val requestValidator: RequestValidator,
) {
    @Transactional
    fun init(user: User, id: Long, request: FlashcardCreationRequest): FlashcardSetInitResponse {
        log.info("Initializing flashcard set $id")
        flashcardSetService.verifyUserHasAccess(user, id)
        return init(id, requestValidator.validate(request))
    }

    @Transactional
    fun init(id: Long, request: ValidFlashcardCreationRequest): FlashcardSetInitResponse {
        val flashcardSet = flashcardSetService.getEntity(id)
        if (flashcardSet.chronodays.isNotEmpty()) {
            throw FlashcardsSetAlreadyStartedException(
                "Flashcard set $id is already started"
            )
        }

        val now = ZonedDateTime.now()
        val initial = Chronoday(
            chronodate = now.toLocalDate(),
            status = ChronodayStatus.INITIAL,
            flashcardSet = flashcardSet,
        )

        val flashcard = Flashcard(
            frontSide = request.frontSide,
            backSide = request.backSide,
            stage = request.stage,
            timesReviewed = 0,
            creationDate = request.creationDate,
            flashcardSet = flashcardSet,
        )

        flashcardSet.startedAt = now
        flashcardSet.flashcards.add(flashcard)
        flashcardSet.chronodays.add(initial)

        val updatedFlashcardSet = flashcardSetRepository.save(flashcardSet)
        val createdFlashcard = flashcardSet.flashcards.last()
        val createdInitial = updatedFlashcardSet.lastChronoday()!!
        val schedule = lightspeedService.createSchedule(chronodays = listOf(createdInitial))

        return FlashcardSetInitResponse(
            flashcardSet = updatedFlashcardSet.toDto(),
            flashcard = createdFlashcard.toDto(),
            currDay =  schedule.first(),
            chronodays = schedule
        )
    }
}
