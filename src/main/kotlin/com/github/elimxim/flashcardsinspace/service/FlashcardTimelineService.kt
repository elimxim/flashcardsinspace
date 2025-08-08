package com.github.elimxim.flashcardsinspace.service

import com.github.elimxim.flashcardsinspace.entity.Chronoday
import com.github.elimxim.flashcardsinspace.entity.DayStatus
import com.github.elimxim.flashcardsinspace.entity.FlashcardTimeline
import com.github.elimxim.flashcardsinspace.entity.TimelineStatus
import com.github.elimxim.flashcardsinspace.entity.repository.ChronodayRepository
import com.github.elimxim.flashcardsinspace.entity.repository.FlashcardTimelineRepository
import com.github.elimxim.flashcardsinspace.schedule.LightspeedSchedule
import com.github.elimxim.flashcardsinspace.web.dto.ChronodayDto
import com.github.elimxim.flashcardsinspace.web.dto.TimelineDto
import com.github.elimxim.flashcardsinspace.web.dto.toDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZonedDateTime

@Service
class FlashcardTimelineService(
    private val flashcardSetService: FlashcardSetService,
    private val flashcardTimelineRepository: FlashcardTimelineRepository,
    private val chronodayRepository: ChronodayRepository,
) {
    @Transactional
    fun getTimeline(flashcardSetId: Long): TimelineDto {
        // todo check for existence
        // todo check flashcard set existence
        val flashcardSet = flashcardSetService.getFlashcardSet(flashcardSetId)
        val timeline = flashcardSet.timeline
        if (timeline == null) {
            throw IllegalArgumentException("Flashcard set doesn't have timeline") // fixme
        }
        return timeline.toDto()
    }

    @Transactional
    fun createTimeline(flashcardSetId: Long, clientDateTime: ZonedDateTime): TimelineDto {
        // todo check flashcard set for existence
        val flashcardSet = flashcardSetService.getFlashcardSet(flashcardSetId)

        val timeline = FlashcardTimeline(
            startedAt = clientDateTime,
            status = TimelineStatus.ACTIVE,
            flashcardSet = flashcardSet,
        )

        flashcardSet.timeline = timeline
        val savedTimeline = flashcardTimelineRepository.save(timeline)
        return savedTimeline.toDto()
    }

    @Transactional
    fun updateTimelineStatus(flashcardSetId: Long, status: String): TimelineDto {
        // todo check for existence
        // todo check flashcard set existence
        // todo check timeline for existence
        val flashcardSet = flashcardSetService.getFlashcardSet(flashcardSetId)
        val timeline = flashcardSet.timeline
        if (timeline == null) {
            throw IllegalArgumentException("Flashcard set doesn't have timeline") // fixme
        }

        var changed = false
        if (timeline.status.name != status) {
            timeline.status = TimelineStatus.valueOf(status) // todo check if value exists
            changed = true
        }

        if (changed) {
            val updatedTimeline = flashcardTimelineRepository.save(timeline)
            return updatedTimeline.toDto()
        } else {
            return timeline.toDto()
        }
    }

    // todo split into two services

    @Transactional
    fun getChronodays(flashcardSetId: Long, clientDatetime: ZonedDateTime): List<ChronodayDto> {
        // todo check flashcard set for existence
        // todo check timeline for existence
        val flashcardSet = flashcardSetService.getFlashcardSet(flashcardSetId)
        val timeline = flashcardSet.timeline
        if (timeline == null) {
            return applyLightspeedSchedule(clientDatetime)
        }

        val currDate = clientDatetime.toLocalDate()
        val lastDate = timeline.lastChronoday()?.chronodate ?: currDate
        val updatedTimeLine = if (!currDate.isBefore(lastDate)) {
            val status = if (timeline.status == TimelineStatus.SUSPENDED) {
                DayStatus.OFF
            } else DayStatus.NOT_STARTED

            lastDate.datesUntil(currDate.plusDays(1)).forEach {
                timeline.chronodays.add(
                    Chronoday(
                        chronodate = it,
                        status = status,
                        timeline = timeline,
                    )
                )
            }

            flashcardTimelineRepository.save(timeline)
        } else timeline

        return applyLightspeedSchedule(updatedTimeLine)
    }

    @Transactional
    fun addChronoday(flashcardSetId: Long): ChronodayDto {
        val flashcardSet = flashcardSetService.getFlashcardSet(flashcardSetId)
        val timeline = flashcardSet.timeline
        if (timeline == null) {
            throw IllegalArgumentException("Flashcard set doesn't have timeline") // fixme
        }

        if (timeline.status == TimelineStatus.SUSPENDED) {
            throw IllegalArgumentException("Timeline is suspended") // fixme
        }

        val lastChronoday = timeline.lastChronoday()
            ?: throw IllegalArgumentException("Flashcard set doesn't have chronodays yet") // fixme

        val chronoday = Chronoday(
            chronodate = lastChronoday.chronodate.plusDays(1),
            status = DayStatus.NOT_STARTED,
            timeline = timeline,
        )

        timeline.chronodays.add(chronoday)
        val updatedTimeline = flashcardTimelineRepository.save(timeline)
        val lightspeedDays = applyLightspeedSchedule(updatedTimeline)
        return lightspeedDays.last()
    }

    @Transactional
    fun updateChronodayStatus(flashcardSetId: Long, chronodayId: Long, status: String): ChronodayDto {
        // todo check for existence
        // todo check flashcard set for existence
        // todo check timeline for existence
        val chronoday = chronodayRepository.getReferenceById(chronodayId) // fixme

        if (chronoday.timeline.status == TimelineStatus.SUSPENDED) {
            throw IllegalArgumentException("Timeline is suspended") // fixme
        }

        var changed = false
        if (chronoday.status.name != status) {
            val newStatus = DayStatus.valueOf(status) // todo check if value exists
            chronoday.status = newStatus
            changed = true
        }

        val updatedChronoday = if (changed) {
            chronodayRepository.save(chronoday)
        } else chronoday

        val lightspeedDays = applyLightspeedSchedule(updatedChronoday.timeline)
        return lightspeedDays.last()
    }

    @Transactional
    fun removeChronoday(flashcardSetId: Long, chronodayId: Long) {
        // todo check for existence
        // todo check if it's last
        // todo check flashcard set for existence
        // todo check timeline for existence
        val chronoday = chronodayRepository.getReferenceById(chronodayId) // fixme

        if (chronoday.timeline.status == TimelineStatus.SUSPENDED) {
            throw IllegalArgumentException("Timeline is suspended") // fixme
        }

        val isRemovable = chronoday.timeline.flashcardSet.flashcards
            .any { it.lastReviewDate != chronoday.chronodate }

        if (isRemovable) {
            chronodayRepository.deleteById(chronodayId)
        } else {
            throw IllegalArgumentException("Can't remove chronoday with flashcards get reviewed on this date") // fixme
        }
    }

    private fun applyLightspeedSchedule(
        startDatetime: ZonedDateTime,
        capacity: Int = 200,
    ): List<ChronodayDto> {
        println(startDatetime)
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
                    status = DayStatus.NOT_STARTED.name,
                    stages = scheduleDay.stages.map { it.name }
                )
            )
        }

        result.addFirst(
            ChronodayDto(
                id = 0,
                chronodate = startDate.toString(),
                seqNumber = 0,
                status = DayStatus.INITIAL.name,
                stages = listOf()
            )
        )

        return result.toList()
    }

    private fun applyLightspeedSchedule(
        timeline: FlashcardTimeline,
        daysAhead: Int = 200,
    ): List<ChronodayDto> {
        if (timeline.chronodays.isEmpty()) return listOf()

        val scheduleDays = LightspeedSchedule(timeline.chronodays.size + daysAhead).days()
        val chronodays = timeline.chronodays.sortedBy { it.chronodate }
        val initialChronoday = timeline.chronodays.first()
        val startDate = initialChronoday.chronodate

        var prevStatus: DayStatus = initialChronoday.status
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
                    status = chronoday?.status?.name ?: DayStatus.NOT_STARTED.name,
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
