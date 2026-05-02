package com.github.elimxim.flashcardsinspace.service

import com.github.elimxim.flashcardsinspace.entity.Chronoday
import com.github.elimxim.flashcardsinspace.entity.ChronodayStatus
import com.github.elimxim.flashcardsinspace.entity.Flashcard
import com.github.elimxim.flashcardsinspace.entity.FlashcardSet
import com.github.elimxim.flashcardsinspace.entity.repository.FlashcardSetRepository
import com.github.elimxim.flashcardsinspace.web.dto.ChronodayDto
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardDto
import com.github.elimxim.flashcardsinspace.web.dto.FlashcardSetDto
import com.github.elimxim.flashcardsinspace.web.dto.toDto
import com.github.elimxim.flashcardsinspace.web.exception.ApiErrorCode
import com.github.elimxim.flashcardsinspace.web.exception.HttpBadRequestException
import org.slf4j.LoggerFactory.getLogger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZonedDateTime

private val log = getLogger(FlashcardSetInitService::class.java)

data class InitResult(
    val flashcardSet: FlashcardSetDto,
    val flashcards: List<FlashcardDto>,
    val schedule: List<ChronodayDto>,
)

@Service
class FlashcardSetInitService(
    private val flashcardSetRepository: FlashcardSetRepository,
    private val lightspeedService: LightspeedService,
) {

    @Transactional
    fun init(flashcardSet: FlashcardSet, flashcards: List<Flashcard>): InitResult {
        log.info("Initializing flashcard set ${flashcardSet.id}")
        if (flashcardSet.chronodays.isNotEmpty()) {
            throw HttpBadRequestException(
                ApiErrorCode.SAS400,
                "Flashcard set ${flashcardSet.id} is already started"
            )
        }

        val now = ZonedDateTime.now()
        val initial = Chronoday(
            chronodate = now.toLocalDate(),
            status = ChronodayStatus.INITIAL,
            flashcardSet = flashcardSet,
        )

        flashcardSet.startedAt = now
        flashcardSet.flashcards.addAll(flashcards)
        flashcardSet.chronodays.add(initial)

        val updatedFlashcardSet = flashcardSetRepository.save(flashcardSet)

        val createdFlashcards = updatedFlashcardSet.flashcards.takeLast(flashcards.size)
        val createdInitial = updatedFlashcardSet.chronodays.maxByOrNull { it.chronodate }!!
        val schedule = lightspeedService.createSchedule(updatedFlashcardSet, chronodays = listOf(createdInitial))

        return InitResult(
            flashcardSet = updatedFlashcardSet.toDto(),
            flashcards = createdFlashcards.map { it.toDto() },
            schedule = schedule,
        )
    }
}
