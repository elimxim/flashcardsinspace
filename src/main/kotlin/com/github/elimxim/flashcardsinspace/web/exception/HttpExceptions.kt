package com.github.elimxim.flashcardsinspace.web.exception

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

// 400 - BAD REQUEST

@ErrorCode("IRF400")
@UserMessageCode("user.message.error.request.fields.invalid")
class InvalidRequestFieldsException(msg: String, val fields: List<String>) : BadRequestException(msg)

@ErrorCode("FSN400")
@UserMessageCode("user.message.error.flashcardSet.notFound")
class FlashcardSetNotFoundException(msg: String) : BadRequestException(msg)

@ErrorCode("FCN400")
@UserMessageCode("user.message.error.flashcard.notFound")
class FlashcardNotFoundException(msg: String) : BadRequestException(msg)

@ErrorCode("LNF400")
@UserMessageCode("user.message.error.language.notFound")
class LanguageNotFoundException(msg: String) : BadRequestException(msg)

@ErrorCode("FSI400")
@UserMessageCode("user.message.error.flashcardSet.alreadyStarted")
class FlashcardsSetAlreadyStartedException(msg: String) : BadRequestException(msg)

@ErrorCode("FSS400")
@UserMessageCode("user.message.error.chronodays.invalidSync")
class InvalidSyncDayException(msg: String) : BadRequestException(msg)

@ErrorCode("FSS400")
@UserMessageCode("user.message.error.flashcardSet.suspended")
class FlashcardSetSuspendedException(msg: String) : BadRequestException(msg)

@ErrorCode("FNS400")
@UserMessageCode("user.message.error.flashcardSet.notStarted")
class FlashcardSetNotStartedException(msg: String) : BadRequestException(msg)

@ErrorCode("PI4SP6")
@UserMessageCode("user.message.error.auth.email.alreadyTaken")
class EmailIsAlreadyTakenException(msg: String) : BadRequestException(msg)

@ErrorCode("NRC400")
@UserMessageCode("user.message.error.chronoday.notRemovable")
class NotRemovableChronodayException(msg: String) : BadRequestException(msg)

@ErrorCode("89023F")
@UserMessageCode("user.message.error.flashcard.setId.unmatched")
class UnmatchedFlashcardSetIdException(msg: String) : BadRequestException(msg)

// 401 - UNAUTHORIZED

@ErrorCode("AFE401")
@UserMessageCode("user.message.error.auth.failed")
class AuthenticationFailedException(msg: String, cause: Exception) : UnauthorizedException(msg, cause = cause)

@ErrorCode("ONA401")
@UserMessageCode("user.message.error.auth.forbidden")
class UserOperationNotAllowedException(msg: String) : UnauthorizedException(msg)

// 404 - NOT FOUND

@ErrorCode("UNF404")
@UserMessageCode("user.message.error.auth.user.notFound")
class UserNotFoundException(msg: String) : NotFoundException(msg)

// 500 - INTERNAL SERVER ERROR

@ErrorCode("UE0500")
@UserMessageCode("user.message.error.unexpected")
class UnexpectedException(msg: String, cause: Exception) : InternalServerErrorException(msg, cause = cause)

@ErrorCode("CCS500")
@UserMessageCode("user.message.error.chronodays.corrupted")
class CorruptedChronoStateException(msg: String) : InternalServerErrorException(msg)
