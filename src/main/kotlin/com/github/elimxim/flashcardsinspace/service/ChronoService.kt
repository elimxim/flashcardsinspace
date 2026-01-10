package com.github.elimxim.flashcardsinspace.service

import com.github.elimxim.flashcardsinspace.entity.*
import com.github.elimxim.flashcardsinspace.entity.repository.ChronodayRepository
import com.github.elimxim.flashcardsinspace.service.validation.RequestValidator
import com.github.elimxim.flashcardsinspace.util.trimOneLine
import com.github.elimxim.flashcardsinspace.web.dto.*
import com.github.elimxim.flashcardsinspace.web.exception.ApiErrorCode
import com.github.elimxim.flashcardsinspace.web.exception.HttpBadRequestException
import com.github.elimxim.flashcardsinspace.web.exception.HttpInternalServerErrorException
import com.github.elimxim.flashcardsinspace.web.exception.HttpNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZoneId
import java.time.ZonedDateTime

private val log = LoggerFactory.getLogger(ChronoService::class.java)

enum class ChronoSyncDay { NEXT, PREV }

@Service
class ChronoService(
    private val flashcardSetService: FlashcardSetService,
    private val flashcardSetDbService: FlashcardSetDbService,
    private val lightspeedService: LightspeedService,
    private val requestValidator: RequestValidator,
    private val dayStreakService: DayStreakService,
    private val chronodayRepository: ChronodayRepository,
) {
    @Transactional
    fun sync(user: User, setId: Long, request: ChronoSyncRequest): ChronoSyncResponse {
        log.info("Syncing chronodays for flashcard set $setId, timezone: ${user.timezone}")
        flashcardSetService.verifyUserHasAccess(user, setId)
        return sync(setId, requestValidator.validate(request).clientDatetime, clientTimezone = user.timezone)
    }

    @Transactional
    fun sync(setId: Long, clientDatetime: ZonedDateTime, clientTimezone: String): ChronoSyncResponse {
        val flashcardSet = flashcardSetDbService.findById(setId, mode = FlashcardSetFetchWithMode.CHRONODAYS)

        val clientZoneId = ZoneId.of(clientTimezone)
        val datetimeInUserZone = clientDatetime.withZoneSameInstant(clientZoneId)
        val currDate = datetimeInUserZone.toLocalDate()

        if (flashcardSet.chronodays.isEmpty()) {
            val schedule = lightspeedService.createSchedule(startDatetime = clientDatetime)
            val currDay = schedule.find { it.chronodate.isEqual(currDate) }
                ?: throw HttpInternalServerErrorException(
                    ApiErrorCode.CCF500,
                    "Can't find current day $currDate in schedule"
                )
            return ChronoSyncResponse(
                currDay = currDay,
                chronodays = schedule,
                dayStreak = 0,
            )
        }

        val lastDate = flashcardSet.lastChronoday()?.chronodate ?: currDate
        val updatedFlashcardSet = if (currDate.isAfter(lastDate)) {
            val status = if (flashcardSet.isSuspended()) ChronodayStatus.OFF else ChronodayStatus.NOT_STARTED

            lastDate.plusDays(1).datesUntil(currDate.plusDays(1)).forEach {
                flashcardSet.chronodays.add(
                    Chronoday(
                        chronodate = it,
                        status = status,
                        flashcardSet = flashcardSet,
                    )
                )
            }

            flashcardSetDbService.save(flashcardSet)
        } else flashcardSet

        dayStreakService.calcDayStreak(updatedFlashcardSet)

        val dayStreak = updatedFlashcardSet.dayStreak?.streak ?: 0
        val (currDay, chronodays) = applySchedule(updatedFlashcardSet)

        return ChronoSyncResponse(
            currDay = currDay,
            chronodays = chronodays,
            dayStreak = dayStreak,
        )
    }

    @Transactional
    fun syncDay(user: User, setId: Long, day: ChronoSyncDay): ChronoSyncResponse {
        log.info("User ${user.id}: syncing day $day for flashcard set $setId")
        flashcardSetService.verifyUserHasAccess(user, setId)
        flashcardSetService.verifyNotSuspended(setId)
        return syncDay(setId, day)
    }

    @Transactional
    fun syncDay(setId: Long, day: ChronoSyncDay): ChronoSyncResponse {
        val flashcardSet = flashcardSetDbService.findById(setId, mode = FlashcardSetFetchWithMode.ALL)

        val lastChronoday = flashcardSet.lastChronoday()
            ?: throw HttpBadRequestException(ApiErrorCode.FSI400, "Flashcard set $setId is not started")

        if (flashcardSet.flashcards.isEmpty()) {
            throw HttpBadRequestException(ApiErrorCode.FNC400, "Flashcard set $setId has no flashcards")
        }

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
                    throw HttpBadRequestException(
                        ApiErrorCode.CDU400,
                        """
                        Can't remove last chronoday ${lastChronoday.id} from 
                        flashcard set $setId with flashcards get reviewed on this date
                        """.trimOneLine()
                    )
                } else if (lastChronoday.status == ChronodayStatus.OFF) {
                    throw HttpBadRequestException(
                        ApiErrorCode.CDO400,
                        """
                        Can't remove last chronoday ${lastChronoday.id} from 
                        flashcard set $setId with status ${lastChronoday.status}
                        """.trimOneLine()
                    )
                }

                flashcardSet.chronodays.remove(lastChronoday)
            }
        }

        dayStreakService.calcDayStreak(flashcardSet)
        val updatedFlashcardSet = flashcardSetDbService.save(flashcardSet)

        val dayStreak = updatedFlashcardSet.dayStreak?.streak ?: 0
        val (currDay, chronodays) = applySchedule(updatedFlashcardSet)

        return ChronoSyncResponse(
            currDay = currDay,
            chronodays = chronodays,
            dayStreak = dayStreak,
        )
    }

    @Transactional
    fun bulkUpdate(user: User, setId: Long, request: ChronoBulkUpdateRequest): ChronoUpdateResponse {
        log.info("Bulk updating chronodays for flashcard set $setId")
        flashcardSetService.verifyUserHasAccess(user, setId)
        flashcardSetService.verifyNotSuspended(setId)
        return bulkUpdate(setId, requestValidator.validate(request))
    }

    @Transactional
    fun bulkUpdate(setId: Long, request: ValidChronoBulkUpdateRequest): ChronoUpdateResponse {
        val flashcardSet = flashcardSetDbService.findById(setId, mode = FlashcardSetFetchWithMode.CHRONODAYS)

        val chronodaysToUpdate = flashcardSet.chronodays
            .filter { it.id in request.ids }
            .sortedBy { it.chronodate }
            .toMutableList()

        val lastChronoday = chronodaysToUpdate.removeLastOrNull()
        val updatedFlashcardSet = if (lastChronoday != null) {
            lastChronoday.status = request.status
            dayStreakService.calcDayStreak(flashcardSet)
            chronodaysToUpdate.forEach { it.status = request.status }
            flashcardSetDbService.save(flashcardSet)
        } else flashcardSet

        val dayStreak = updatedFlashcardSet.dayStreak?.streak ?: 0
        val schedule = lightspeedService.createSchedule(updatedFlashcardSet.chronodays, daysAhead = 0)
        val updatedDays = schedule.filter { it.id in request.ids }

        return ChronoUpdateResponse(
            chronodays = updatedDays,
            dayStreak = dayStreak,
        )
    }

    @Transactional(readOnly = true)
    fun getEntity(id: Long): Chronoday {
        return chronodayRepository.findById(id).orElseThrow {
            throw HttpNotFoundException(ApiErrorCode.CHR404, "Chronoday $id not found")
        }
    }

    private fun applySchedule(flashcardSet: FlashcardSet): Pair<ChronodayDto, List<ChronodayDto>> {
        val schedule = lightspeedService.createSchedule(flashcardSet.chronodays)
        val lastChronoDate = flashcardSet.lastChronoday()?.chronodate
        val currDay = schedule.find { it.chronodate.isEqual(lastChronoDate) }
            ?: throw HttpInternalServerErrorException(
                ApiErrorCode.LCF500,
                """
                Can't find last chrono day $lastChronoDate
                in schedule for flashcard set ${flashcardSet.id}
                """.trimOneLine()
            )
        return currDay to schedule
    }
}
