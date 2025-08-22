package com.github.elimxim.flashcardsinspace.service

import com.github.elimxim.flashcardsinspace.entity.*
import com.github.elimxim.flashcardsinspace.entity.repository.ChronodayRepository
import com.github.elimxim.flashcardsinspace.entity.repository.FlashcardSetRepository
import com.github.elimxim.flashcardsinspace.schedule.LightspeedSchedule
import com.github.elimxim.flashcardsinspace.web.dto.ChronodayDto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZonedDateTime

private val log = LoggerFactory.getLogger(ChronodayService::class.java)

@Service
class ChronodayService(
    private val flashcardSetService: FlashcardSetService,
    private val flashcardSetRepository: FlashcardSetRepository,
    private val chronodayRepository: ChronodayRepository,
) {
    @Transactional
    fun getChronodays(flashcardSetId: Long, clientDatetime: ZonedDateTime): Pair<ChronodayDto, List<ChronodayDto>> {
        val flashcardSet = flashcardSetService.getEntity(flashcardSetId)
        if (flashcardSet.chronodays.isEmpty()) {
            val schedule = applyLightspeedSchedule(clientDatetime)
            val currDayStr = clientDatetime.toLocalDate().toString()
            val currDay = schedule.find { it.chronodate == currDayStr }
                ?: throw IllegalArgumentException("Can't find current day in schedule") // fixme
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

        val schedule = applyLightspeedSchedule(updatedFlashcardSet)
        val lastChronoDateStr = flashcardSet.lastChronoday()?.chronodate?.toString()
        val currDay = schedule.find { it.chronodate == lastChronoDateStr }
            ?: throw IllegalArgumentException("Can't find current day in schedule") // fixme
        return currDay to schedule
    }

    @Transactional
    fun addInitialChronoday(flashcardSetId: Long): ChronodayDto {
        val now = ZonedDateTime.now()

        val flashcardSet = flashcardSetService.getEntity(flashcardSetId)
        flashcardSet.startedAt = now

        val initial = Chronoday(
            chronodate = now.toLocalDate(),
            status = ChronodayStatus.INITIAL,
            flashcardSet = flashcardSet,
        )

        flashcardSet.chronodays.add(initial)
        val updatedFlashcardSet = flashcardSetRepository.save(flashcardSet)
        val schedule = applyLightspeedSchedule(updatedFlashcardSet)
        return schedule.last()
    }

    @Transactional
    fun addChronoday(flashcardSetId: Long): ChronodayDto {
        val flashcardSet = flashcardSetService.getEntity(flashcardSetId)
        if (flashcardSet.status == FlashcardSetStatus.SUSPENDED) {
            throw IllegalArgumentException("FlashcardSet is suspended") // fixme
        }

        val lastChronoday = flashcardSet.lastChronoday()
            ?: throw IllegalArgumentException("FlashcardSet is not started") // fixme

        val chronoday = Chronoday(
            chronodate = lastChronoday.chronodate.plusDays(1),
            status = ChronodayStatus.NOT_STARTED,
            flashcardSet = flashcardSet,
        )

        flashcardSet.chronodays.add(chronoday)
        val updatedFlashcardSet = flashcardSetRepository.save(flashcardSet)
        val schedule = applyLightspeedSchedule(updatedFlashcardSet)
        return schedule.last()
    }

    @Transactional
    fun updateChronodayStatus(flashcardSetId: Long, chronodayId: Long, status: String): ChronodayDto {
        // todo check for existence
        // todo check flashcard set for existence
        val chronoday = chronodayRepository.getReferenceById(chronodayId) // fixme

        if (chronoday.flashcardSet.status == FlashcardSetStatus.SUSPENDED) {
            throw IllegalArgumentException("FlashcardSet is suspended") // fixme
        }

        var changed = false
        if (chronoday.status.name != status) {
            val newStatus = ChronodayStatus.valueOf(status) // todo check if value exists
            chronoday.status = newStatus
            changed = true
        }

        val updatedChronoday = if (changed) {
            chronodayRepository.save(chronoday)
        } else chronoday

        val schedule = applyLightspeedSchedule(updatedChronoday.flashcardSet)
        return schedule.last()
    }

    @Transactional
    fun removeChronoday(flashcardSetId: Long, chronodayId: Long) {
        // todo check for existence
        // todo check if it's last
        // todo check flashcard set for existence
        val chronoday = chronodayRepository.getReferenceById(chronodayId) // fixme

        if (chronoday.flashcardSet.status == FlashcardSetStatus.SUSPENDED) {
            throw IllegalArgumentException("FlashcardSet is suspended") // fixme
        }

        val isNotRemovable = chronoday.flashcardSet.flashcards
            .any { it.lastReviewDate != null && !chronoday.chronodate.isAfter(it.lastReviewDate) }

        if (isNotRemovable) {
            throw IllegalArgumentException("Can't remove chronoday with flashcards get reviewed on this date") // fixme
        }

        chronodayRepository.deleteById(chronodayId)
    }

    private fun applyLightspeedSchedule(
        startDatetime: ZonedDateTime,
        capacity: Int = 200,
    ): List<ChronodayDto> {
        val scheduleDays = LightspeedSchedule(capacity).days()
        val startDate = startDatetime.toLocalDate()
        val result = mutableListOf<ChronodayDto>()
        for (i in 1..scheduleDays.size) {
            val date = startDate.plusDays(i.toLong())
            val scheduleDay = scheduleDays[i - 1]

            result.add(
                ChronodayDto(
                    id = 0,
                    chronodate = date.toString(),
                    seqNumber = scheduleDay.number,
                    status = ChronodayStatus.NOT_STARTED.name,
                    stages = scheduleDay.stages.map { it.name }
                )
            )
        }

        result.addFirst(
            ChronodayDto(
                id = 0,
                chronodate = startDate.toString(),
                seqNumber = 0,
                status = ChronodayStatus.INITIAL.name,
                stages = listOf()
            )
        )

        return result.toList()
    }

    private fun applyLightspeedSchedule(
        flashcardSet: FlashcardSet,
        daysAhead: Int = 200,
    ): List<ChronodayDto> {
        if (flashcardSet.chronodays.isEmpty()) return listOf()

        val scheduleDays = LightspeedSchedule(flashcardSet.chronodays.size + daysAhead).days()
        val chronodays = flashcardSet.chronodays.sortedBy { it.chronodate }
        val initialChronoday = flashcardSet.chronodays.first()
        val startDate = initialChronoday.chronodate

        var prevStatus: ChronodayStatus = initialChronoday.status
        val result = mutableListOf<ChronodayDto>()
        for (i in 1..scheduleDays.size) {
            val date = startDate.plusDays(i.toLong())
            val scheduleDay = scheduleDays[i - 1]
            val chronoday = chronodays.getOrElse(i) { null }

            if (chronoday != null) {
                // todo check if chronodays have inconsistent statuses:
                // todo prevStatus = IN_PROGRESS && currStatus = COMPLETED || INITIAL
                // todo prevStatus = NOT_STARTED && currStatus = IN_PROGRESS || COMPLETED || INITIAL
                // todo prevStatus = INITIAL && currStatus = INITIAL
                // todo prevStatus = COMPLETED && currStatus = INITIAL
            }

            result.add(
                ChronodayDto(
                    id = chronoday?.id ?: 0,
                    chronodate = date.toString(),
                    seqNumber = scheduleDay.number,
                    status = chronoday?.status?.name ?: ChronodayStatus.NOT_STARTED.name,
                    stages = scheduleDay.stages.map { it.name }
                )
            )
        }

        result.addFirst(
            ChronodayDto(
                id = initialChronoday.id,
                chronodate = initialChronoday.chronodate.toString(),
                seqNumber = 0,
                status = initialChronoday.status.name,
                stages = listOf()
            )
        )

        return result.toList()
    }

}
