package com.github.elimxim.flashcardsinspace.service

import com.github.elimxim.flashcardsinspace.entity.*
import com.github.elimxim.flashcardsinspace.web.exception.HttpInternalServerErrorException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import java.time.LocalDate
import java.time.ZonedDateTime
import kotlin.test.Test

@DisplayName("LightspeedService")
class LightspeedServiceTest {

    private val service = LightspeedService()

    @Nested
    @DisplayName("createSchedule with ZonedDateTime")
    inner class CreateScheduleWithDateTime {

        @Test
        fun `should create initial schedule with correct sequence numbers`() {
            val startDatetime = ZonedDateTime.parse("2024-01-01T10:00:00Z")
            val schedule = service.createSchedule(startDatetime, capacity = 10)

            assertThat(schedule).hasSize(11)

            with(schedule[0]) {
                assertThat(seqNumber).isEqualTo(0)
                assertThat(status).isEqualTo(ChronodayStatus.INITIAL.name)
                assertThat(stages).isEmpty()
                assertThat(chronodate).isEqualTo(LocalDate.of(2024, 1, 1))
            }

            with(schedule[1]) {
                assertThat(seqNumber).isEqualTo(1)
                assertThat(status).isEqualTo(ChronodayStatus.NOT_STARTED.name)
                assertThat(stages).containsExactly("S1")
                assertThat(chronodate).isEqualTo(LocalDate.of(2024, 1, 2))
            }

            with(schedule[2]) {
                assertThat(seqNumber).isEqualTo(2)
                assertThat(stages).containsExactly("S1")
            }

            with(schedule[3]) {
                assertThat(seqNumber).isEqualTo(3)
                assertThat(stages).containsExactlyInAnyOrder("S2", "S1")
            }
        }

        @Test
        fun `should create schedule with default capacity`() {
            val startDatetime = ZonedDateTime.parse("2024-01-01T10:00:00Z")
            val schedule = service.createSchedule(startDatetime)

            assertThat(schedule).hasSize(201)
        }

        @Test
        fun `should generate correct stages for first 10 days`() {
            val startDatetime = ZonedDateTime.parse("2024-01-01T10:00:00Z")
            val schedule = service.createSchedule(startDatetime, capacity = 10)

            assertThat(schedule[1].stages).containsExactly("S1")
            assertThat(schedule[2].stages).containsExactly("S1")
            assertThat(schedule[3].stages).containsExactlyInAnyOrder("S2", "S1")
            assertThat(schedule[4].stages).containsExactly("S1")
            assertThat(schedule[5].stages).containsExactlyInAnyOrder("S2", "S1")
            assertThat(schedule[6].stages).containsExactlyInAnyOrder("S3", "S1")
            assertThat(schedule[7].stages).containsExactlyInAnyOrder("S2", "S1")
            assertThat(schedule[8].stages).containsExactly("S1")
            assertThat(schedule[9].stages).containsExactlyInAnyOrder("S2", "S1")
            assertThat(schedule[10].stages).containsExactlyInAnyOrder("S3", "S1")
        }
    }

    @Nested
    @DisplayName("createSchedule with Chronodays")
    inner class CreateScheduleWithChronodays {

        @Test
        fun `should return empty list when chronodays is empty`() {
            val schedule = service.createSchedule(testFlashcardSet(), emptyList())

            assertThat(schedule).isEmpty()
        }

        @Test
        fun `should handle OFF days with null seqNumber and empty stages`() {
            val flashcardSet = testFlashcardSet()
            val startDate = LocalDate.of(2024, 1, 1)

            val chronodays = listOf(
                testChronoday(0, startDate, ChronodayStatus.INITIAL, flashcardSet),
                testChronoday(1, startDate.plusDays(1), ChronodayStatus.NOT_STARTED, flashcardSet),
                testChronoday(2, startDate.plusDays(2), ChronodayStatus.NOT_STARTED, flashcardSet),
                testChronoday(3, startDate.plusDays(3), ChronodayStatus.OFF, flashcardSet),
                testChronoday(4, startDate.plusDays(4), ChronodayStatus.OFF, flashcardSet),
                testChronoday(5, startDate.plusDays(5), ChronodayStatus.NOT_STARTED, flashcardSet),
                testChronoday(6, startDate.plusDays(6), ChronodayStatus.NOT_STARTED, flashcardSet),
            )

            val schedule = service.createSchedule(flashcardSet, chronodays, daysAhead = 5)

            assertThat(schedule).hasSize(13)

            with(schedule[0]) {
                assertThat(seqNumber).isEqualTo(0)
                assertThat(status).isEqualTo(ChronodayStatus.INITIAL.name)
                assertThat(stages).isEmpty()
            }

            with(schedule[1]) {
                assertThat(seqNumber).isEqualTo(1)
                assertThat(status).isEqualTo(ChronodayStatus.NOT_STARTED.name)
                assertThat(stages).isNotEmpty()
            }

            with(schedule[2]) {
                assertThat(seqNumber).isEqualTo(2)
                assertThat(status).isEqualTo(ChronodayStatus.NOT_STARTED.name)
                assertThat(stages).isNotEmpty()
            }

            with(schedule[3]) {
                assertThat(seqNumber).isNull()
                assertThat(status).isEqualTo(ChronodayStatus.OFF.name)
                assertThat(stages).isEmpty()
            }

            with(schedule[4]) {
                assertThat(seqNumber).isNull()
                assertThat(status).isEqualTo(ChronodayStatus.OFF.name)
                assertThat(stages).isEmpty()
            }

            with(schedule[5]) {
                assertThat(seqNumber).isEqualTo(3)
                assertThat(status).isEqualTo(ChronodayStatus.NOT_STARTED.name)
                assertThat(stages).isNotEmpty()
            }

            with(schedule[6]) {
                assertThat(seqNumber).isEqualTo(4)
                assertThat(status).isEqualTo(ChronodayStatus.NOT_STARTED.name)
                assertThat(stages).isNotEmpty()
            }
        }

        @Test
        fun `should handle consecutive OFF days at the start`() {
            val flashcardSet = testFlashcardSet()
            val startDate = LocalDate.of(2024, 1, 1)

            val chronodays = listOf(
                testChronoday(0, startDate, ChronodayStatus.INITIAL, flashcardSet),
                testChronoday(1, startDate.plusDays(1), ChronodayStatus.OFF, flashcardSet),
                testChronoday(2, startDate.plusDays(2), ChronodayStatus.OFF, flashcardSet),
                testChronoday(3, startDate.plusDays(3), ChronodayStatus.NOT_STARTED, flashcardSet),
                testChronoday(4, startDate.plusDays(4), ChronodayStatus.NOT_STARTED, flashcardSet),
            )

            val schedule = service.createSchedule(flashcardSet, chronodays, daysAhead = 0)

            with(schedule[1]) {
                assertThat(seqNumber).isNull()
                assertThat(status).isEqualTo(ChronodayStatus.OFF.name)
            }
            with(schedule[2]) {
                assertThat(seqNumber).isNull()
                assertThat(status).isEqualTo(ChronodayStatus.OFF.name)
            }
            with(schedule[3]) {
                assertThat(seqNumber).isEqualTo(1)
                assertThat(status).isEqualTo(ChronodayStatus.NOT_STARTED.name)
            }
            with(schedule[4]) {
                assertThat(seqNumber).isEqualTo(2)
                assertThat(status).isEqualTo(ChronodayStatus.NOT_STARTED.name)
            }
        }

        @Test
        fun `should handle consecutive OFF days at the end`() {
            val flashcardSet = testFlashcardSet()
            val startDate = LocalDate.of(2024, 1, 1)

            val chronodays = listOf(
                testChronoday(0, startDate, ChronodayStatus.INITIAL, flashcardSet),
                testChronoday(1, startDate.plusDays(1), ChronodayStatus.NOT_STARTED, flashcardSet),
                testChronoday(2, startDate.plusDays(2), ChronodayStatus.NOT_STARTED, flashcardSet),
                testChronoday(3, startDate.plusDays(3), ChronodayStatus.OFF, flashcardSet),
                testChronoday(4, startDate.plusDays(4), ChronodayStatus.OFF, flashcardSet),
            )

            val schedule = service.createSchedule(flashcardSet, chronodays, daysAhead = 0)

            assertThat(schedule[1].seqNumber).isEqualTo(1)
            assertThat(schedule[2].seqNumber).isEqualTo(2)
            assertThat(schedule[3].seqNumber).isNull()
            assertThat(schedule[4].seqNumber).isNull()
        }

        @Test
        fun `should handle all OFF days`() {
            val flashcardSet = testFlashcardSet()
            val startDate = LocalDate.of(2024, 1, 1)

            val chronodays = listOf(
                testChronoday(0, startDate, ChronodayStatus.INITIAL, flashcardSet),
                testChronoday(1, startDate.plusDays(1), ChronodayStatus.OFF, flashcardSet),
                testChronoday(2, startDate.plusDays(2), ChronodayStatus.OFF, flashcardSet),
            )

            val schedule = service.createSchedule(flashcardSet, chronodays, daysAhead = 0)

            assertThat(schedule).hasSize(4)
            assertThat(schedule[0].seqNumber).isEqualTo(0)
            assertThat(schedule[1].seqNumber).isNull()
            assertThat(schedule[2].seqNumber).isNull()
            assertThat(schedule[3].seqNumber).isEqualTo(1)
        }

        @Test
        fun `should preserve chronoday IDs`() {
            val flashcardSet = testFlashcardSet()
            val startDate = LocalDate.of(2024, 1, 1)

            val chronodays = listOf(
                testChronoday(100, startDate, ChronodayStatus.INITIAL, flashcardSet),
                testChronoday(101, startDate.plusDays(1), ChronodayStatus.NOT_STARTED, flashcardSet),
                testChronoday(102, startDate.plusDays(2), ChronodayStatus.OFF, flashcardSet),
            )

            val schedule = service.createSchedule(flashcardSet, chronodays, daysAhead = 0)

            assertThat(schedule[0].id).isEqualTo(100)
            assertThat(schedule[1].id).isEqualTo(101)
            assertThat(schedule[2].id).isEqualTo(102)
        }

        @Test
        fun `should handle alternating OFF and active days`() {
            val flashcardSet = testFlashcardSet()
            val startDate = LocalDate.of(2024, 1, 1)

            val chronodays = listOf(
                testChronoday(0, startDate, ChronodayStatus.INITIAL, flashcardSet),
                testChronoday(1, startDate.plusDays(1), ChronodayStatus.NOT_STARTED, flashcardSet),
                testChronoday(2, startDate.plusDays(2), ChronodayStatus.OFF, flashcardSet),
                testChronoday(3, startDate.plusDays(3), ChronodayStatus.NOT_STARTED, flashcardSet),
                testChronoday(4, startDate.plusDays(4), ChronodayStatus.OFF, flashcardSet),
                testChronoday(5, startDate.plusDays(5), ChronodayStatus.NOT_STARTED, flashcardSet),
            )

            val schedule = service.createSchedule(flashcardSet, chronodays, daysAhead = 0)

            assertThat(schedule[0].seqNumber).isEqualTo(0)
            assertThat(schedule[1].seqNumber).isEqualTo(1)
            assertThat(schedule[2].seqNumber).isNull()
            assertThat(schedule[3].seqNumber).isEqualTo(2)
            assertThat(schedule[4].seqNumber).isNull()
            assertThat(schedule[5].seqNumber).isEqualTo(3)
        }

        @Test
        fun `should handle daysAhead parameter correctly`() {
            val flashcardSet = testFlashcardSet()
            val startDate = LocalDate.of(2024, 1, 1)

            val chronodays = listOf(
                testChronoday(0, startDate, ChronodayStatus.INITIAL, flashcardSet),
                testChronoday(1, startDate.plusDays(1), ChronodayStatus.NOT_STARTED, flashcardSet),
            )

            val schedule = service.createSchedule(flashcardSet, chronodays, daysAhead = 3)

            assertThat(schedule).hasSize(6)
            assertThat(schedule[0].seqNumber).isEqualTo(0)
            assertThat(schedule[1].seqNumber).isEqualTo(1)
            assertThat(schedule[2].seqNumber).isEqualTo(2)
            assertThat(schedule[3].seqNumber).isEqualTo(3)
            assertThat(schedule[4].seqNumber).isEqualTo(4)
            assertThat(schedule[5].seqNumber).isEqualTo(5)
        }

        @Test
        fun `should handle mixed statuses correctly`() {
            val flashcardSet = testFlashcardSet()
            val startDate = LocalDate.of(2024, 1, 1)

            val chronodays = listOf(
                testChronoday(0, startDate, ChronodayStatus.INITIAL, flashcardSet),
                testChronoday(1, startDate.plusDays(1), ChronodayStatus.IN_PROGRESS, flashcardSet),
                testChronoday(2, startDate.plusDays(2), ChronodayStatus.COMPLETED, flashcardSet),
                testChronoday(3, startDate.plusDays(3), ChronodayStatus.OFF, flashcardSet),
                testChronoday(4, startDate.plusDays(4), ChronodayStatus.NOT_STARTED, flashcardSet),
            )

            val schedule = service.createSchedule(flashcardSet, chronodays, daysAhead = 0)

            assertThat(schedule[0].status).isEqualTo(ChronodayStatus.INITIAL.name)
            assertThat(schedule[1].status).isEqualTo(ChronodayStatus.IN_PROGRESS.name)
            assertThat(schedule[2].status).isEqualTo(ChronodayStatus.COMPLETED.name)
            assertThat(schedule[3].status).isEqualTo(ChronodayStatus.OFF.name)
            assertThat(schedule[4].status).isEqualTo(ChronodayStatus.NOT_STARTED.name)

            assertThat(schedule[0].seqNumber).isEqualTo(0)
            assertThat(schedule[1].seqNumber).isEqualTo(1)
            assertThat(schedule[2].seqNumber).isEqualTo(2)
            assertThat(schedule[3].seqNumber).isNull()
            assertThat(schedule[4].seqNumber).isEqualTo(3)
        }

        @Test
        fun `should throw exception when duplicate dates are detected`() {
            val flashcardSet = testFlashcardSet()
            val startDate = LocalDate.of(2024, 1, 1)

            val chronodays = listOf(
                testChronoday(0, startDate, ChronodayStatus.INITIAL, flashcardSet),
                testChronoday(1, startDate.plusDays(1), ChronodayStatus.NOT_STARTED, flashcardSet),
                testChronoday(2, startDate.plusDays(1), ChronodayStatus.NOT_STARTED, flashcardSet),
            )

            assertThatThrownBy { service.createSchedule(flashcardSet, chronodays, daysAhead = 0) }
                .isInstanceOf(HttpInternalServerErrorException::class.java)
                .hasMessageContaining("Duplicated dates")
                .hasMessageContaining("2024-01-02")
        }

        @Test
        fun `should sort chronodays by date before processing`() {
            val flashcardSet = testFlashcardSet()
            val startDate = LocalDate.of(2024, 1, 1)

            val chronodays = listOf(
                testChronoday(0, startDate, ChronodayStatus.INITIAL, flashcardSet),
                testChronoday(3, startDate.plusDays(3), ChronodayStatus.NOT_STARTED, flashcardSet),
                testChronoday(1, startDate.plusDays(1), ChronodayStatus.NOT_STARTED, flashcardSet),
                testChronoday(2, startDate.plusDays(2), ChronodayStatus.NOT_STARTED, flashcardSet),
            )

            val schedule = service.createSchedule(flashcardSet, chronodays, daysAhead = 0)

            assertThat(schedule[0].chronodate).isEqualTo(startDate)
            assertThat(schedule[1].chronodate).isEqualTo(startDate.plusDays(1))
            assertThat(schedule[2].chronodate).isEqualTo(startDate.plusDays(2))
            assertThat(schedule[3].chronodate).isEqualTo(startDate.plusDays(3))
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
