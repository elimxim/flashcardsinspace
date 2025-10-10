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

enum class ChronoSyncDay { NEXT, PREV }

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
            val currDate = clientDatetime.toLocalDate()
            val currDay = schedule.find { it.chronodate.isEqual(currDate) }
                ?: throw CorruptedChronoStateException(
                    "Can't find current day $currDate in schedule"
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

        return applySchedule(updatedFlashcardSet)
    }

    @Transactional
    fun syncDay(user: User, setId: Long, day: ChronoSyncDay): Pair<ChronodayDto, List<ChronodayDto>> {
        log.info("User ${user.id}: chrono step $day for flashcard set $setId")
        flashcardSetService.verifyUserHasAccess(user, setId)
        return syncDay(setId, day)
    }

    @Transactional
    fun syncDay(setId: Long, day: ChronoSyncDay): Pair<ChronodayDto, List<ChronodayDto>> {
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

        when (day) {
            ChronoSyncDay.NEXT -> {
                val chronoday = Chronoday(
                    chronodate = lastChronoday.chronodate.plusDays(1),
                    status = ChronodayStatus.NOT_STARTED,
                    flashcardSet = flashcardSet,
                )

                flashcardSet.chronodays.add(chronoday)
            }
            ChronoSyncDay.PREV -> {
                val hasReviews = flashcardSet.flashcards.any {
                    it.lastReviewDate != null && !lastChronoday.chronodate.isAfter(it.lastReviewDate)
                }

                if (hasReviews) {
                    throw NotRemovableChronodayException(
                        """
                    Can't remove last chronoday ${lastChronoday.id} from 
                    flashcard set $setId with flashcards get reviewed on this date
                    """.trimOneLine()
                    )
                }

                flashcardSet.chronodays.remove(lastChronoday)
            }
        }

        val updatedFlashcardSet = flashcardSetRepository.save(flashcardSet)
        return applySchedule(updatedFlashcardSet)
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

        val updatedFlashcardSet = if (changed) {
            flashcardSetRepository.save(flashcardSet)
        } else flashcardSet

        val schedule = lightspeedService.createSchedule(updatedFlashcardSet.chronodays, daysAhead = 0)
        return schedule.filter { it.id in request.ids }
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

    private fun applySchedule(flashcardSet: FlashcardSet): Pair<ChronodayDto, List<ChronodayDto>> {
        val schedule = lightspeedService.createSchedule(flashcardSet.chronodays)
        val lastChronoDate = flashcardSet.lastChronoday()?.chronodate
        val currDay = schedule.find { it.chronodate.isEqual(lastChronoDate) }
            ?: throw CorruptedChronoStateException(
                """
                    Can't find last chrono day $lastChronoDate
                    in schedule for flashcard set ${flashcardSet.id}
                    """.trimOneLine()
            )
        return currDay to schedule
    }
}
