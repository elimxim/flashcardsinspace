package com.github.elimxim.flashcardsinspace.service

import com.github.elimxim.flashcardsinspace.entity.*
import com.github.elimxim.flashcardsinspace.entity.repository.ChronodayRepository
import com.github.elimxim.flashcardsinspace.entity.repository.FlashcardSetRepository
import com.github.elimxim.flashcardsinspace.service.validation.RequestValidator
import com.github.elimxim.flashcardsinspace.util.trimOneLine
import com.github.elimxim.flashcardsinspace.web.dto.ChronoBulkUpdateRequest
import com.github.elimxim.flashcardsinspace.web.dto.ChronoSyncRequest
import com.github.elimxim.flashcardsinspace.web.dto.ChronodayDto
import com.github.elimxim.flashcardsinspace.web.dto.ValidChronoBulkUpdateRequest
import com.github.elimxim.flashcardsinspace.web.exception.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZonedDateTime

private val log = LoggerFactory.getLogger(ChronoService::class.java)

@Service
class ChronoService(
    private val flashcardSetService: FlashcardSetService,
    private val flashcardSetRepository: FlashcardSetRepository,
    private val chronodayRepository: ChronodayRepository,
    private val lightspeedService: LightspeedService,
    private val requestValidator: RequestValidator,
) {
    @Transactional
    fun sync(user: User, setId: Long, request: ChronoSyncRequest): Pair<ChronodayDto, List<ChronodayDto>> {
        log.info("User ${user.id}: syncing chronodays for flashcard set $setId")
        flashcardSetService.verifyUserHasAccess(user, setId)
        return sync(setId, requestValidator.validate(request).clientDatetime)
    }

    @Transactional
    fun sync(setId: Long, clientDatetime: ZonedDateTime): Pair<ChronodayDto, List<ChronodayDto>> {
        val flashcardSet = flashcardSetService.getEntity(setId)
        if (flashcardSet.chronodays.isEmpty()) {
            val schedule = lightspeedService.createSchedule(startDatetime = clientDatetime)
            val currDayStr = clientDatetime.toLocalDate().toString()
            val currDay = schedule.find { it.chronodate == currDayStr }
                ?: throw CorruptedChronoStateException(
                    "Can't find current day $currDayStr in schedule"
                )
            return currDay to schedule
        }

        val currDate = clientDatetime.toLocalDate()
        val lastDate = flashcardSet.lastChronoday()?.chronodate ?: currDate
        val updatedFlashcardSet = if (currDate.isAfter(lastDate)) {
            val status = if (flashcardSet.status == FlashcardSetStatus.SUSPENDED) {
                ChronodayStatus.OFF
            } else ChronodayStatus.NOT_STARTED

            lastDate.plusDays(1).datesUntil(currDate.plusDays(1)).forEach {
                flashcardSet.chronodays.add(
                    Chronoday(
                        chronodate = it,
                        status = status,
                        flashcardSet = flashcardSet,
                    )
                )
            }

            flashcardSetRepository.save(flashcardSet)
        } else flashcardSet

        val schedule = lightspeedService.createSchedule(updatedFlashcardSet.chronodays)
        val lastChronoDateStr = flashcardSet.lastChronoday()?.chronodate?.toString()
        val currDay = schedule.find { it.chronodate == lastChronoDateStr }
            ?: throw CorruptedChronoStateException(
                """
                    Can't find last chrono day $lastChronoDateStr 
                    in schedule for flashcard set $setId
                    """.trimOneLine()
            )
        return currDay to schedule
    }

    @Transactional
    fun addNext(user: User, setId: Long): ChronodayDto {
        log.info("User ${user.id}: adding next chronoday for flashcard set $setId")
        flashcardSetService.verifyUserHasAccess(user, setId)

        val flashcardSet = flashcardSetService.getEntity(setId)
        if (flashcardSet.status == FlashcardSetStatus.SUSPENDED) {
            throw FlashcardSetSuspendedException(
                "Flashcard set $setId is suspended"
            )
        }

        val lastChronoday = flashcardSet.lastChronoday()
            ?: throw FlashcardSetNotStartedException(
                "Flashcard set $setId is not started"
            )

        val chronoday = Chronoday(
            chronodate = lastChronoday.chronodate.plusDays(1),
            status = ChronodayStatus.NOT_STARTED,
            flashcardSet = flashcardSet,
        )

        flashcardSet.chronodays.add(chronoday)
        val updatedFlashcardSet = flashcardSetRepository.save(flashcardSet)
        val schedule = lightspeedService.createSchedule(updatedFlashcardSet.chronodays)
        return schedule.last()
    }

    @Transactional
    fun bulkUpdate(user: User, setId: Long, request: ChronoBulkUpdateRequest): List<ChronodayDto> {
        log.info("User ${user.id}: bulk updating chronodays for flashcard set $setId")
        flashcardSetService.verifyUserHasAccess(user, setId)
        return bulkUpdate(setId, requestValidator.validate(request))
    }

    @Transactional
    fun bulkUpdate(setId: Long, request: ValidChronoBulkUpdateRequest): List<ChronodayDto> {
        val flashcardSet = flashcardSetService.getEntity(setId)
        if (flashcardSet.status == FlashcardSetStatus.SUSPENDED) {
            throw FlashcardSetSuspendedException(
                "Flashcard set $setId is suspended"
            )
        }

        var changed = false
        flashcardSet.chronodays.forEach { chronoday ->
            if (chronoday.id in request.ids) {
                chronoday.status = request.status
                changed = true
            }
        }

        val updateFlashcardSet = if (changed) {
            flashcardSetRepository.save(flashcardSet)
        } else flashcardSet

        val chronodays = updateFlashcardSet.chronodays.filter { it.id in request.ids }
        val schedule = lightspeedService.createSchedule(chronodays, daysAhead = 0)
        return schedule
    }

    @Transactional
    fun remove(user: User, setId: Long, id: Long) {
        log.info("User ${user.id}: removing chronoday $id from set $setId")
        flashcardSetService.verifyUserHasAccess(user, setId)
        verifyUserOperation(user, setId, id)

        val chronoday = getEntity(id)
        val flashcardSet = chronoday.flashcardSet
        val lastChronoday = flashcardSet.lastChronoday()
        if (!chronoday.chronodate.isEqual(lastChronoday?.chronodate)) {
            throw NotRemovableChronodayException(
                "Chronoday $id is not the last one in set $setId"
            )
        }

        if (flashcardSet.status == FlashcardSetStatus.SUSPENDED) {
            throw FlashcardSetSuspendedException(
                "Flashcard set $setId is suspended"
            )
        }

        val isNotRemovable = flashcardSet.flashcards
            .any { it.lastReviewDate != null && !chronoday.chronodate.isAfter(it.lastReviewDate) }

        if (isNotRemovable) {
            throw NotRemovableChronodayException(
                "Can't remove chronoday $id from flashcard set $setId with flashcards get reviewed on this date"
            )
        }

        flashcardSet.chronodays.remove(chronoday)
        chronodayRepository.delete(chronoday)
    }

    @Transactional
    fun getEntity(id: Long): Chronoday =
        chronodayRepository.findById(id).orElseThrow {
            ChronodayNotFoundException("Chronoday with id $id not found")
        }

    @Transactional
    fun verifyUserOperation(user: User, setId: Long, id: Long) {
        if (getEntity(id).flashcardSet.id != setId) {
            throw UnmatchedFlashcardSetIdException(
                """
                    User ${user.id} requested chronoday $id 
                    doesn't belong to the requested set $setId
                    """.trimOneLine()
            )
        }
    }
}
