package com.github.elimxim.flashcardsinspace.service

import com.github.elimxim.flashcardsinspace.entity.*
import com.github.elimxim.flashcardsinspace.web.exception.HttpInternalServerErrorException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import java.time.LocalDate
import java.time.ZonedDateTime
import kotlin.test.Test

class DayStreakServiceTest {
    private val service = DayStreakService()

    @Nested
    inner class CalcDayStreak {

        @Test
        fun `should not update day streak if size is less than 2`() {
            val flashcardSet = testFlashcardSet()
            val startDate = LocalDate.of(2024, 1, 1)

            val initialDay = testChronoday(1, startDate, ChronodayStatus.INITIAL, flashcardSet)
            flashcardSet.chronodays.add(initialDay)

            val dayStreak = DayStreak(
                streak = 5,
                lastDay = flashcardSet.chronodays[0],
                flashcardSet = flashcardSet
            )
            flashcardSet.dayStreak = dayStreak

            service.calcDayStreak(flashcardSet)

            assertThat(flashcardSet.dayStreak?.streak).isEqualTo(dayStreak.streak)
            assertThat(flashcardSet.dayStreak?.lastDay?.id).isEqualTo(dayStreak.lastDay.id)
        }

        @Test
        fun `should create DayStreak when it doesn't exist`() {
            val flashcardSet = testFlashcardSet()
            val startDate = LocalDate.of(2024, 1, 1)

            val initialDay = testChronoday(1, startDate, ChronodayStatus.INITIAL, flashcardSet)
            val completedDay = testChronoday(2, startDate.plusDays(1), ChronodayStatus.COMPLETED, flashcardSet)
            flashcardSet.chronodays.addAll(listOf(initialDay, completedDay))

            assertThat(flashcardSet.dayStreak).isNull()

            service.calcDayStreak(flashcardSet)

            assertThat(flashcardSet.dayStreak).isNotNull()
            assertThat(flashcardSet.dayStreak!!.streak).isEqualTo(1)
            assertThat(flashcardSet.dayStreak!!.lastDay.id).isEqualTo(2)
        }

        @Test
        fun `should update streak when progress is made`() {
            val flashcardSet = testFlashcardSet()
            val startDate = LocalDate.of(2024, 1, 1)

            val initialDay = testChronoday(1, startDate, ChronodayStatus.INITIAL, flashcardSet)
            val completedDay = testChronoday(2, startDate.plusDays(1), ChronodayStatus.COMPLETED, flashcardSet)
            flashcardSet.chronodays.addAll(listOf(initialDay, completedDay))

            service.calcDayStreak(flashcardSet)

            assertThat(flashcardSet.dayStreak!!.streak).isEqualTo(1)
            assertThat(flashcardSet.dayStreak!!.lastDay.id).isEqualTo(2)
        }

        @Test
        fun `should reset streak when no progress`() {
            val flashcardSet = testFlashcardSet()
            val startDate = LocalDate.of(2024, 1, 1)

            val initialDay = testChronoday(1, startDate, ChronodayStatus.INITIAL, flashcardSet)
            val dayOff = testChronoday(2, startDate.plusDays(1), ChronodayStatus.OFF, flashcardSet)

            flashcardSet.chronodays.addAll(listOf(initialDay, dayOff))

            val dayStreak = DayStreak(
                streak = 5,
                lastDay = flashcardSet.chronodays[0],
                flashcardSet = flashcardSet
            )
            flashcardSet.dayStreak = dayStreak

            service.calcDayStreak(flashcardSet)

            assertThat(flashcardSet.dayStreak!!.streak).isEqualTo(0)
        }

        @Test
        fun `should return false when no change in streak`() {
            val flashcardSet = testFlashcardSet()
            val startDate = LocalDate.of(2024, 1, 1)

            val initialDay = testChronoday(1, startDate, ChronodayStatus.INITIAL, flashcardSet)
            val completedDay = testChronoday(2, startDate.plusDays(1), ChronodayStatus.COMPLETED, flashcardSet)
            flashcardSet.chronodays.addAll(listOf(initialDay, completedDay))

            val dayStreak = DayStreak(
                streak = 5,
                lastDay = flashcardSet.chronodays[1],
                flashcardSet = flashcardSet
            )
            flashcardSet.dayStreak = dayStreak

            service.calcDayStreak(flashcardSet)

            assertThat(flashcardSet.dayStreak!!.streak).isEqualTo(5)
        }

        @Test
        fun `should accumulate streak over multiple calls`() {
            val flashcardSet = testFlashcardSet()
            val startDate = LocalDate.of(2024, 1, 1)

            val initialDay = testChronoday(1, startDate, ChronodayStatus.INITIAL, flashcardSet)
            val completedDay2 = testChronoday(2, startDate.plusDays(1), ChronodayStatus.COMPLETED, flashcardSet)
            flashcardSet.chronodays.addAll(listOf(initialDay, completedDay2))

            service.calcDayStreak(flashcardSet)
            assertThat(flashcardSet.dayStreak!!.streak).isEqualTo(1)

            val completedDay3 = testChronoday(3, startDate.plusDays(2), ChronodayStatus.COMPLETED, flashcardSet)
            flashcardSet.chronodays.add(completedDay3)

            service.calcDayStreak(flashcardSet)
            assertThat(flashcardSet.dayStreak!!.streak).isEqualTo(2)
        }
    }

    @Nested
    inner class CalcStreakDays {

        @Test
        fun `should return NoChange when fromInclusive is INITIAL`() {
            val flashcardSet = testFlashcardSet()
            val startDate = LocalDate.of(2024, 1, 1)

            val chronodays = listOf(
                testChronoday(1, startDate, ChronodayStatus.INITIAL, flashcardSet),
                testChronoday(2, startDate.plusDays(1), ChronodayStatus.COMPLETED, flashcardSet),
            )

            val result = service.calcStreakDays(
                fromInclusive = chronodays[0],
                toExclusive = chronodays[0],
                chronodays = chronodays
            )

            assertThat(result).isInstanceOf(DayStreakScanResult.NoChange::class.java)
        }

        @Test
        fun `should return NoChange when range is empty (fromIndex is less than toIndex)`() {
            val flashcardSet = testFlashcardSet()
            val startDate = LocalDate.of(2024, 1, 1)

            val chronodays = listOf(
                testChronoday(1, startDate, ChronodayStatus.INITIAL, flashcardSet),
                testChronoday(2, startDate.plusDays(1), ChronodayStatus.COMPLETED, flashcardSet),
                testChronoday(3, startDate.plusDays(2), ChronodayStatus.COMPLETED, flashcardSet),
            )

            val result = service.calcStreakDays(
                fromInclusive = chronodays[1],
                toExclusive = chronodays[2],
                chronodays = chronodays
            )

            assertThat(result).isInstanceOf(DayStreakScanResult.NoChange::class.java)
        }

        @Test
        fun `should count single COMPLETED day`() {
            val flashcardSet = testFlashcardSet()
            val startDate = LocalDate.of(2024, 1, 1)

            val chronodays = listOf(
                testChronoday(1, startDate, ChronodayStatus.INITIAL, flashcardSet),
                testChronoday(2, startDate.plusDays(1), ChronodayStatus.COMPLETED, flashcardSet),
            )

            val result = service.calcStreakDays(
                fromInclusive = chronodays[1],
                toExclusive = chronodays[0],
                chronodays = chronodays
            )

            assertThat(result).isInstanceOf(DayStreakScanResult.Progress::class.java)
            val progress = result as DayStreakScanResult.Progress
            assertThat(progress.count).isEqualTo(1)
            assertThat(progress.lastDay.id).isEqualTo(2)
        }

        @Test
        fun `should count consecutive COMPLETED days`() {
            val flashcardSet = testFlashcardSet()
            val startDate = LocalDate.of(2024, 1, 1)

            val chronodays = listOf(
                testChronoday(1, startDate, ChronodayStatus.INITIAL, flashcardSet),
                testChronoday(2, startDate.plusDays(1), ChronodayStatus.COMPLETED, flashcardSet),
                testChronoday(3, startDate.plusDays(2), ChronodayStatus.COMPLETED, flashcardSet),
                testChronoday(4, startDate.plusDays(3), ChronodayStatus.COMPLETED, flashcardSet),
            )

            val result = service.calcStreakDays(
                fromInclusive = chronodays[3],
                toExclusive = chronodays[0],
                chronodays = chronodays
            )

            assertThat(result).isInstanceOf(DayStreakScanResult.Progress::class.java)
            val progress = result as DayStreakScanResult.Progress
            assertThat(progress.count).isEqualTo(3)
            assertThat(progress.lastDay.id).isEqualTo(4)
        }

        @Test
        fun `should skip IN_PROGRESS days before first COMPLETED day`() {
            val flashcardSet = testFlashcardSet()
            val startDate = LocalDate.of(2024, 1, 1)

            val chronodays = listOf(
                testChronoday(1, startDate, ChronodayStatus.INITIAL, flashcardSet),
                testChronoday(2, startDate.plusDays(1), ChronodayStatus.COMPLETED, flashcardSet),
                testChronoday(3, startDate.plusDays(2), ChronodayStatus.IN_PROGRESS, flashcardSet),
                testChronoday(4, startDate.plusDays(3), ChronodayStatus.IN_PROGRESS, flashcardSet)
            )

            val result = service.calcStreakDays(
                fromInclusive = chronodays[3],
                toExclusive = chronodays[0],
                chronodays = chronodays
            )

            assertThat(result).isInstanceOf(DayStreakScanResult.Progress::class.java)
            val progress = result as DayStreakScanResult.Progress
            assertThat(progress.count).isEqualTo(1)
            assertThat(progress.lastDay.id).isEqualTo(2)
        }

        @Test
        fun `should skip NOT_STARTED only when it is fromInclusive`() {
            val flashcardSet = testFlashcardSet()
            val startDate = LocalDate.of(2024, 1, 1)

            val chronodays = listOf(
                testChronoday(1, startDate, ChronodayStatus.INITIAL, flashcardSet),
                testChronoday(2, startDate.plusDays(1), ChronodayStatus.COMPLETED, flashcardSet),
                testChronoday(3, startDate.plusDays(2), ChronodayStatus.NOT_STARTED, flashcardSet),
            )

            val result = service.calcStreakDays(
                fromInclusive = chronodays[2],
                toExclusive = chronodays[0],
                chronodays = chronodays
            )

            assertThat(result).isInstanceOf(DayStreakScanResult.Progress::class.java)
            val progress = result as DayStreakScanResult.Progress
            assertThat(progress.count).isEqualTo(1)
            assertThat(progress.lastDay.id).isEqualTo(2)
        }

        @Test
        fun `should reset when there are multiple NOT_STARTED`() {
            val flashcardSet = testFlashcardSet()
            val startDate = LocalDate.of(2024, 1, 1)

            val chronodays = listOf(
                testChronoday(1, startDate, ChronodayStatus.INITIAL, flashcardSet),
                testChronoday(2, startDate.plusDays(1), ChronodayStatus.COMPLETED, flashcardSet),
                testChronoday(3, startDate.plusDays(2), ChronodayStatus.NOT_STARTED, flashcardSet),
                testChronoday(4, startDate.plusDays(3), ChronodayStatus.NOT_STARTED, flashcardSet),
            )

            val result = service.calcStreakDays(
                fromInclusive = chronodays[3],
                toExclusive = chronodays[0],
                chronodays = chronodays
            )

            assertThat(result).isInstanceOf(DayStreakScanResult.Reset::class.java)
        }

        @Test
        fun `should allow OFF days after counting starts`() {
            val flashcardSet = testFlashcardSet()
            val startDate = LocalDate.of(2024, 1, 1)

            val chronodays = listOf(
                testChronoday(1, startDate, ChronodayStatus.INITIAL, flashcardSet),
                testChronoday(2, startDate.plusDays(1), ChronodayStatus.COMPLETED, flashcardSet),
                testChronoday(3, startDate.plusDays(2), ChronodayStatus.OFF, flashcardSet),
                testChronoday(4, startDate.plusDays(3), ChronodayStatus.COMPLETED, flashcardSet),
            )

            val result = service.calcStreakDays(
                fromInclusive = chronodays[3],
                toExclusive = chronodays[0],
                chronodays = chronodays
            )

            assertThat(result).isInstanceOf(DayStreakScanResult.Progress::class.java)
            val progress = result as DayStreakScanResult.Progress
            assertThat(progress.count).isEqualTo(2)
            assertThat(progress.lastDay.id).isEqualTo(4)
        }

        @Test
        fun `should reset when first day is OFF day`() {
            val flashcardSet = testFlashcardSet()
            val startDate = LocalDate.of(2024, 1, 1)

            val chronodays = listOf(
                testChronoday(1, startDate, ChronodayStatus.INITIAL, flashcardSet),
                testChronoday(2, startDate.plusDays(1), ChronodayStatus.COMPLETED, flashcardSet),
                testChronoday(3, startDate.plusDays(2), ChronodayStatus.OFF, flashcardSet),
            )

            val result = service.calcStreakDays(
                fromInclusive = chronodays[2],
                toExclusive = chronodays[0],
                chronodays = chronodays
            )

            assertThat(result).isInstanceOf(DayStreakScanResult.Reset::class.java)
        }

        @Test
        fun `should throw exception when fromInclusive not found`() {
            val flashcardSet = testFlashcardSet()
            val startDate = LocalDate.of(2024, 1, 1)

            val chronodays = listOf(
                testChronoday(1, startDate, ChronodayStatus.INITIAL, flashcardSet),
                testChronoday(2, startDate.plusDays(1), ChronodayStatus.COMPLETED, flashcardSet),
            )

            val notInList = testChronoday(999, startDate.plusDays(5), ChronodayStatus.COMPLETED, flashcardSet)

            assertThatThrownBy {
                service.calcStreakDays(
                    fromInclusive = notInList,
                    toExclusive = chronodays[0],
                    chronodays = chronodays
                )
            }
                .isInstanceOf(HttpInternalServerErrorException::class.java)
                .hasMessageContaining("Chronoday 999 not found")
        }

        @Test
        fun `should throw exception when toExclusive not found`() {
            val flashcardSet = testFlashcardSet()
            val startDate = LocalDate.of(2024, 1, 1)

            val chronodays = listOf(
                testChronoday(1, startDate, ChronodayStatus.INITIAL, flashcardSet),
                testChronoday(2, startDate.plusDays(1), ChronodayStatus.COMPLETED, flashcardSet),
            )

            val notInList = testChronoday(999, startDate.plusDays(5), ChronodayStatus.COMPLETED, flashcardSet)

            assertThatThrownBy {
                service.calcStreakDays(
                    fromInclusive = chronodays[1],
                    toExclusive = notInList,
                    chronodays = chronodays
                )
            }
                .isInstanceOf(HttpInternalServerErrorException::class.java)
                .hasMessageContaining("Chronoday 999 not found")
        }

        @Test
        fun `should handle multiple consecutive OFF days in streak`() {
            val flashcardSet = testFlashcardSet()
            val startDate = LocalDate.of(2024, 1, 1)

            val chronodays = listOf(
                testChronoday(1, startDate, ChronodayStatus.INITIAL, flashcardSet),
                testChronoday(2, startDate.plusDays(1), ChronodayStatus.COMPLETED, flashcardSet),
                testChronoday(3, startDate.plusDays(2), ChronodayStatus.OFF, flashcardSet),
                testChronoday(4, startDate.plusDays(3), ChronodayStatus.OFF, flashcardSet),
                testChronoday(5, startDate.plusDays(4), ChronodayStatus.COMPLETED, flashcardSet),
            )

            val result = service.calcStreakDays(
                fromInclusive = chronodays[4],
                toExclusive = chronodays[0],
                chronodays = chronodays
            )

            assertThat(result).isInstanceOf(DayStreakScanResult.Progress::class.java)
            val progress = result as DayStreakScanResult.Progress
            assertThat(progress.count).isEqualTo(2)
        }
    }

    private fun testFlashcardSet() = FlashcardSet(
        id = 1,
        name = "Test Set",
        status = FlashcardSetStatus.ACTIVE,
        createdAt = ZonedDateTime.now(),
        language = Language(id = 1, name = "English", code = "en"),
        user = User(
            id = 1,
            email = "test@test.com",
            emailVerified = true,
            name = "Test User",
            secret = "secret",
            roles = "USER",
            registeredAt = ZonedDateTime.now(),
            language = Language(id = 1, name = "English", code = "en")
        )
    )

    private fun testChronoday(
        id: Long,
        date: LocalDate,
        status: ChronodayStatus,
        flashcardSet: FlashcardSet
    ) = Chronoday(
        id = id,
        chronodate = date,
        status = status,
        flashcardSet = flashcardSet
    )
}

