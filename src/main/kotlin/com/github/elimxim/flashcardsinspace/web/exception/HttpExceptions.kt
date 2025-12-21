package com.github.elimxim.flashcardsinspace.web.exception

import org.springframework.http.HttpStatus

annotation class ExceptionHttpStatus(val value: HttpStatus)
annotation class ErrorCode(val value: String)

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
class InvalidRequestFieldsException(msg: String, val fields: List<String>) : BadRequestException(msg)

@ErrorCode("IRE400")
class InvalidRequestException(msg: String) : BadRequestException(msg)

@ErrorCode("FSN400")
class FlashcardSetNotFoundException(msg: String) : BadRequestException(msg)

@ErrorCode("FCN400")
class FlashcardNotFoundException(msg: String) : BadRequestException(msg)

@ErrorCode("LNF400")
class LanguageNotFoundException(msg: String) : BadRequestException(msg)

@ErrorCode("CNF400")
class ChronodayNotFoundException(msg: String) : BadRequestException(msg)

@ErrorCode("CSI400")
class ChildSessionIllegalRequestException(msg: String) : BadRequestException(msg)

@ErrorCode("FSI400")
class FlashcardsSetAlreadyStartedException(msg: String) : BadRequestException(msg)

@ErrorCode("FSS400")
class FlashcardSetSuspendedException(msg: String) : BadRequestException(msg)

@ErrorCode("FAS400")
class FlashcardSetAlreadySuspendedException(msg: String) : BadRequestException(msg)

@ErrorCode("FMS400")
class FlashcardSetCannotBeSuspendedException(msg: String) : BadRequestException(msg)

@ErrorCode("FNS400")
class FlashcardSetNotStartedException(msg: String) : BadRequestException(msg)

@ErrorCode("PI4SP6")
class EmailIsAlreadyTakenException(msg: String) : BadRequestException(msg)

@ErrorCode("NRC400")
class ChronodayHasReviewsException(msg: String) : BadRequestException(msg)

@ErrorCode("CDO400")
class ChronodayIsDayOff(msg: String) : BadRequestException(msg)

@ErrorCode("89023F")
class UnmatchedFlashcardSetIdException(msg: String) : BadRequestException(msg)

@ErrorCode("AUB400")
class AudioUploadBusyException(msg: String, cause: Exception? = null) : BadRequestException(msg, cause = cause)

// 401 - UNAUTHORIZED

@ErrorCode("AFE401")
class AuthenticationFailedException(msg: String, cause: Exception) : UnauthorizedException(msg, cause = cause)

@ErrorCode("ONA401")
class UserOperationNotAllowedException(msg: String) : UnauthorizedException(msg)

// 404 - NOT FOUND

@ErrorCode("UNF404")
class UserNotFoundException(msg: String) : NotFoundException(msg)

@ErrorCode("ANF404")
class AudioNotFoundException(msg: String) : NotFoundException(msg)

@ErrorCode("RSN404")
class ReviewSessionNotFoundException(msg: String) : NotFoundException(msg)

// 500 - INTERNAL SERVER ERROR

@ErrorCode("UE0500")
class UnexpectedException(msg: String, cause: Exception) : InternalServerErrorException(msg, cause = cause)

@ErrorCode("CCS500")
class CorruptedChronoStateException(msg: String) : InternalServerErrorException(msg)
