package com.github.elimxim.flashcardsinspace.service

import com.github.elimxim.flashcardsinspace.entity.*
import com.github.elimxim.flashcardsinspace.util.trimOneLine
import com.github.elimxim.flashcardsinspace.web.exception.CorruptedChronoStateException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val log = LoggerFactory.getLogger(DayStreakService::class.java)

sealed class DayStreakScanResult {
    data class Progress(val count: Int, val lastDay: Chronoday) : DayStreakScanResult()
    data object Reset : DayStreakScanResult()
    data object NoChange : DayStreakScanResult()
}

@Service
class DayStreakService {

    @Transactional
    fun calcDayStreak(flashcardSet: FlashcardSet) {
        val chronodays = flashcardSet.chronodays.sortedBy { it.chronodate }
        if (chronodays.size < 2) return

        val dayStreak = getOrCreateDayStreak(flashcardSet, chronodays)
        val result = calcStreakDays(
            fromInclusive = chronodays.last(),
            toExclusive = dayStreak.lastDay,
            chronodays = chronodays,
        )

        when (result) {
            is DayStreakScanResult.Progress -> {
                log.info(
                    """
                    Day streak progressed for flashcard set ${flashcardSet.id}: 
                    ${dayStreak.streak} -> ${dayStreak.streak + result.count}
                    """.trimOneLine()
                )
                dayStreak.streak += result.count
                dayStreak.lastDay = result.lastDay
            }

            is DayStreakScanResult.Reset -> {
                log.info("Day streak reset for flashcard set ${flashcardSet.id}")
                dayStreak.streak = 0
            }

            is DayStreakScanResult.NoChange -> {
                log.info("Day streak did not change for flashcard set ${flashcardSet.id}")
            }
        }
    }

    private fun getOrCreateDayStreak(flashcardSet: FlashcardSet, chronodays: List<Chronoday>): DayStreak {
        val dayStreak = flashcardSet.dayStreak
        return if (dayStreak == null) {
            val initialDayStreak = DayStreak(
                streak = 0,
                lastDay = chronodays.first(),
                flashcardSet = flashcardSet,
            )
            flashcardSet.dayStreak = initialDayStreak
            initialDayStreak
        } else dayStreak
    }

    /**
     * Calculates day-streak progress between two chronodays within a flashcard set timeline.
     *
     * The scan walks backwards from `fromInclusive` down to (but excluding) `toExclusive` and counts
     * consecutive COMPLETED days. Once counting has started, OFF (day-off/vacation) days are allowed:
     * they do not increase the count and do not break the streak. Before the first COMPLETED day is seen:
     *  - IN_PROGRESS days are ignored (skipped)
     *  - NOT_STARTED is ignored only when it is exactly the `fromInclusive` day
     *  - any other status (including OFF) stops the scan and results in a reset
     *
     * Returns:
     *  - Progress: at least one COMPLETED day was found; `count` equals newly
     *    counted COMPLETED days, and `lastDay` is the last COMPLETED Chronoday encountered
     *  - Reset: no COMPLETED day was found before the scan stopped; the caller should reset the streak (to 0)
     *    and typically set the last day to the current tail chronoday
     *  - NoChange: nothing to do (e.g., `fromInclusive` is INITIAL or the range is empty/invalid)
     *
     * Preconditions/assumptions:
     *  - `chronodays` includes both boundary days (matched by id) and belongs to a single set
     *  - `chronodays` are in chronological order so that index(fromInclusive) >= index(toExclusive)
     *  - Throws CorruptedChronoStateException if a boundary day cannot be found
     *
     * Time complexity: O(k) where k is the number of days between the two boundaries.
     *
     * @param fromInclusive the most recent chronoday to start scanning from (included)
     * @param toExclusive the chronoday to stop before (excluded)
     * @param chronodays the full timeline of chronodays for the set
     * @return [DayStreakScanResult] describing whether the streak progressed, reset, or did not change
     */
    fun calcStreakDays(
        fromInclusive: Chronoday,
        toExclusive: Chronoday,
        chronodays: List<Chronoday>
    ): DayStreakScanResult {
        log.info(
            """
            Calculating streak days for flashcard set ${fromInclusive.flashcardSet.id}:
            [${fromInclusive.id} / ${fromInclusive.chronodate}, ${toExclusive.id} / ${toExclusive.chronodate})
            """.trimOneLine()
        )

        if (fromInclusive.isInitial())
            return DayStreakScanResult.NoChange

        val fromIndex = getChronoIndex(fromInclusive, chronodays)
        val toIndex = getChronoIndex(toExclusive, chronodays) + 1

        val toInclusive = chronodays.getOrNull(toIndex)
            ?: return DayStreakScanResult.NoChange

        if (fromInclusive.chronodate.isBefore(toInclusive.chronodate))
            return DayStreakScanResult.NoChange

        var counter = 0
        var lastDay = toExclusive
        var countingBroken = false
        var countingStarted = false
        for (i in fromIndex downTo toIndex) {
            val day = chronodays[i]
            if (!countingStarted) {
                if (day.isCompleted()) {
                    countingStarted = true
                    lastDay = day
                    counter++
                } else if (day.isInProgress()) {
                    continue
                } else if (day.isNotStarted() && day.id == fromInclusive.id) {
                    continue
                } else {
                    countingBroken = true
                    break
                }
            } else {
                if (day.isCompleted()) {
                    counter++
                } else if (!day.isOff()) {
                    break
                }
            }
        }

        return if (countingBroken) {
            DayStreakScanResult.Reset
        } else if (countingStarted) {
            DayStreakScanResult.Progress(counter, lastDay)
        } else {
            DayStreakScanResult.NoChange
        }
    }

    private fun getChronoIndex(day: Chronoday, chronodays: List<Chronoday>): Int {
        val idx = chronodays.indexOfFirst { it.id == day.id }
        if (idx == -1) {
            throw CorruptedChronoStateException("Chronoday ${day.id} not found in chronodays")
        }
        return idx
    }

}
