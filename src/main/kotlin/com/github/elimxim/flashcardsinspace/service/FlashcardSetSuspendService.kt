package com.github.elimxim.flashcardsinspace.service

import com.github.elimxim.flashcardsinspace.entity.ChronodayStatus
import com.github.elimxim.flashcardsinspace.entity.FlashcardSetStatus
import com.github.elimxim.flashcardsinspace.entity.User
import com.github.elimxim.flashcardsinspace.entity.lastChronoday
import com.github.elimxim.flashcardsinspace.entity.repository.FlashcardSetRepository
import com.github.elimxim.flashcardsinspace.service.validation.RequestValidator
import com.github.elimxim.flashcardsinspace.util.trimOneLine
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardSetSuspendResponse
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardSetUpdateRequest
import com.github.elimxim.flashcardsinspace.web.dto.ValidFlashcardSetUpdateRequest
import com.github.elimxim.flashcardsinspace.web.dto.toDto
import com.github.elimxim.flashcardsinspace.web.exception.CorruptedChronoStateException
import com.github.elimxim.flashcardsinspace.web.exception.FlashcardSetAlreadySuspendedException
import com.github.elimxim.flashcardsinspace.web.exception.FlashcardSetCannotBeSuspendedException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val log = LoggerFactory.getLogger(FlashcardSetSuspendService::class.java)

@Service
class FlashcardSetSuspendService(
    private val flashcardSetService: FlashcardSetService,
    private val requestValidator: RequestValidator,
    private val flashcardSetRepository: FlashcardSetRepository,
    private val lightspeedService: LightspeedService,
) {
    @Transactional
    fun suspend(user: User, id: Long, request: FlashcardSetUpdateRequest): FlashcardSetSuspendResponse {
        log.info("User ${user.id}: suspending flashcard set $id")
        flashcardSetService.verifyUserHasAccess(user, id)
        return suspend(id, requestValidator.validate(request))
    }

    @Transactional
    fun suspend(id: Long, request: ValidFlashcardSetUpdateRequest): FlashcardSetSuspendResponse {
        val flashcardSet = flashcardSetService.getEntity(id)

        if (flashcardSet.status == FlashcardSetStatus.SUSPENDED) {
            throw FlashcardSetAlreadySuspendedException("Flashcard set $id is already suspended")
        }

        val lastChronoday = flashcardSet.lastChronoday()
            ?: throw FlashcardSetCannotBeSuspendedException("Flashcard set $id has no chronodays")

        flashcardSetService.mergeFlashcardSet(flashcardSet, request)
        flashcardSet.status = FlashcardSetStatus.SUSPENDED

        if (lastChronoday.status == ChronodayStatus.NOT_STARTED) {
            lastChronoday.status = ChronodayStatus.OFF
        }

        val updatedFlashcardSet = flashcardSetRepository.save(flashcardSet)
        val schedule = lightspeedService.createSchedule(updatedFlashcardSet.chronodays)

        val currDay = if (lastChronoday.status == ChronodayStatus.INITIAL) {
            schedule.first()
        } else {
            schedule.find { it.chronodate.isEqual(lastChronoday.chronodate) }
                ?: throw CorruptedChronoStateException(
                    """
                    Can't find last chrono day ${lastChronoday.chronodate}
                    in schedule for flashcard set ${flashcardSet.id}
                    """.trimOneLine()
                )
        }

        return FlashcardSetSuspendResponse(
            flashcardSet = updatedFlashcardSet.toDto(),
            chronodays = schedule,
            currDay = currDay
        )
    }
}
