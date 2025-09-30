package com.github.elimxim.flashcardsinspace.service

import com.github.elimxim.flashcardsinspace.entity.Chronoday
import com.github.elimxim.flashcardsinspace.entity.ChronodayStatus
import com.github.elimxim.flashcardsinspace.schedule.LightspeedSchedule
import com.github.elimxim.flashcardsinspace.util.trimOneLine
import com.github.elimxim.flashcardsinspace.web.dto.ChronodayDto
import com.github.elimxim.flashcardsinspace.web.exception.CorruptedChronoStateException
import org.springframework.stereotype.Service
import java.time.ZonedDateTime

@Service
class LightspeedService {

    fun createSchedule(
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

    fun createSchedule(
        chronodays: List<Chronoday>,
        daysAhead: Int = 200,
    ): List<ChronodayDto> {
        if (chronodays.isEmpty()) return listOf()

        val scheduleDays = LightspeedSchedule(chronodays.size + daysAhead).days()
        val chronodays = chronodays.sortedBy { it.chronodate }
        val initialChronoday = chronodays.first()
        val startDate = initialChronoday.chronodate

        var prevChronoday: Chronoday? = initialChronoday
        val result = mutableListOf<ChronodayDto>()
        for (i in 1..scheduleDays.size) {
            val date = startDate.plusDays(i.toLong())
            val scheduleDay = scheduleDays[i - 1]
            val chronoday = chronodays.getOrElse(i) { null }

            if (chronoday != null) {
                if (chronoday.chronodate.isEqual(prevChronoday?.chronodate)) {
                    throw CorruptedChronoStateException(
                        """
                        Duplicated dates ${chronoday.chronodate} were detected
                        in flashcard set with id=${chronoday.flashcardSet.id}
                        """.trimOneLine()
                    )
                }
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

            prevChronoday = chronoday
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
