package com.github.elimxim.flashcardsinspace.web.exception

import com.github.elimxim.flashcardsinspace.util.joinWithBraces
import org.springframework.http.HttpStatus

annotation class ExceptionHttpStatus(val value: HttpStatus)
annotation class ErrorCode(val value: String)
annotation class UserMessageCode(val value: String)

sealed class HttpException(val args: List<Any>, msg: String, cause: Exception?) : Exception(msg, cause)
sealed class Http4xxException(args: List<Any>, msg: String, cause: Exception?) : HttpException(args, msg, cause)
sealed class Http5xxException(args: List<Any>, msg: String, cause: Exception?) : HttpException(args, msg, cause)

@ExceptionHttpStatus(HttpStatus.BAD_REQUEST)
sealed class BadRequestException(
    msg: String, args: List<Any> = emptyList<Any>(), cause: Exception? = null
) : Http4xxException(args, msg, cause)

@ExceptionHttpStatus(HttpStatus.UNAUTHORIZED)
sealed class UnauthorizedException(
    msg: String, args: List<Any> = emptyList<Any>(), cause: Exception? = null
) : Http4xxException(args, msg, cause)

@ExceptionHttpStatus(HttpStatus.FORBIDDEN)
sealed class ForbiddenException(
    msg: String, args: List<Any> = emptyList<Any>(), cause: Exception? = null
) : Http4xxException(args, msg, cause)

@ExceptionHttpStatus(HttpStatus.NOT_FOUND)
sealed class NotFoundException(
    msg: String, args: List<Any> = emptyList<Any>(), cause: Exception? = null
) : Http4xxException(args, msg, cause)

@ExceptionHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
sealed class InternalServerErrorException(
    msg: String, args: List<Any> = emptyList<Any>(), cause: Exception? = null
) : Http5xxException(args, msg, cause)

@ErrorCode("VIJ6NK")
@UserMessageCode("user.message.error.auth.failed")
class AuthenticationFailedException(msg: String, cause: Exception) : UnauthorizedException(msg, cause = cause)

@ErrorCode("283J29")
@UserMessageCode("user.message.error.auth.forbidden")
class UserOperationNotAllowedException(msg: String) : UnauthorizedException(msg)

@ErrorCode("HC28C8")
@UserMessageCode("user.message.error.auth.user.notFound")
class UserNotFoundException(msg: String) : NotFoundException(msg)

@ErrorCode("7FO2VL")
@UserMessageCode("user.message.error.request.fields.invalid")
class InvalidRequestFieldsException(msg: String, val fields: List<String>) :
    BadRequestException(msg, listOf(fields.joinWithBraces()))

@ErrorCode("JFK90J")
@UserMessageCode("user.message.error.flashcardSet.notFound")
class FlashcardSetNotFoundException(msg: String) : BadRequestException(msg)

@ErrorCode("JFO09E")
@UserMessageCode("user.message.error.flashcard.notFound")
class FlashcardNotFoundException(msg: String) : BadRequestException(msg)

@ErrorCode("DJ420F")
@UserMessageCode("user.message.error.chronoday.notFound")
class ChronodayNotFoundException(msg: String) : BadRequestException(msg)

@ErrorCode("Q4SG3F")
@UserMessageCode("user.message.error.language.notFound")
class LanguageNotFoundException(msg: String) : BadRequestException(msg)

@ErrorCode("2SP0RI")
@UserMessageCode("user.message.error.flashcardSet.alreadyInitialized")
class FlashcardsSetAlreadyInitializedException(msg: String) : BadRequestException(msg)

@ErrorCode("FG024F")
@UserMessageCode("user.message.error.flashcardSet.suspended")
class FlashcardSetSuspendedException(msg: String) : BadRequestException(msg)

@ErrorCode("23J423")
@UserMessageCode("user.message.error.flashcardSet.notStarted")
class FlashcardSetNotStartedException(msg: String) : BadRequestException(msg)

@ErrorCode("PI4SP6")
@UserMessageCode("user.message.error.auth.email.alreadyTaken")
class EmailIsAlreadyTakenException(msg: String) : BadRequestException(msg)

@ErrorCode("N7S44T")
@UserMessageCode("user.message.error.chronodays.corrupted")
class CorruptedChronoStateException(msg: String) : InternalServerErrorException(msg)

@ErrorCode("OWF890")
@UserMessageCode("user.message.error.chronoday.notRemovable")
class NotRemovableChronodayException(msg: String) : BadRequestException(msg)

@ErrorCode("89023F")
@UserMessageCode("user.message.error.flashcard.setId.unmatched")
class UnmatchedFlashcardSetIdException(msg: String) : BadRequestException(msg)
