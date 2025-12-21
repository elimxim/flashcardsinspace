package com.github.elimxim.flashcardsinspace.web.exception

import org.springframework.http.HttpStatus

annotation class ExceptionHttpStatus(val value: HttpStatus)

sealed class HttpException(val apiErrorCode: ApiErrorCode, message: String, cause: Exception?) :
    Exception(message, cause)

sealed class Http4xxException(apiErrorCode: ApiErrorCode, message: String, cause: Exception?) :
    HttpException(apiErrorCode, message, cause)

sealed class Http5xxException(apiErrorCode: ApiErrorCode, message: String, cause: Exception?) :
    HttpException(apiErrorCode, message, cause)

@ExceptionHttpStatus(HttpStatus.BAD_REQUEST)
open class HttpBadRequestException(apiErrorCode: ApiErrorCode, message: String, cause: Exception? = null) :
    Http4xxException(apiErrorCode, message, cause)

@ExceptionHttpStatus(HttpStatus.UNAUTHORIZED)
open class HttpUnauthorizedException(apiErrorCode: ApiErrorCode, message: String, cause: Exception? = null) :
    Http4xxException(apiErrorCode, message, cause)

@ExceptionHttpStatus(HttpStatus.FORBIDDEN)
open class HttpForbiddenException(apiErrorCode: ApiErrorCode, message: String, cause: Exception? = null) :
    Http4xxException(apiErrorCode, message, cause)

@ExceptionHttpStatus(HttpStatus.NOT_FOUND)
open class HttpNotFoundException(apiErrorCode: ApiErrorCode, message: String, cause: Exception? = null) :
    Http4xxException(apiErrorCode, message, cause)

@ExceptionHttpStatus(HttpStatus.CONFLICT)
open class HttpConflictException(apiErrorCode: ApiErrorCode, message: String, cause: Exception? = null) :
    Http4xxException(apiErrorCode, message, cause)

@ExceptionHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
open class HttpInternalServerErrorException(apiErrorCode: ApiErrorCode, message: String, cause: Exception? = null) :
    Http5xxException(apiErrorCode, message, cause)

class HttpInvalidRequestFieldsException(val fields: List<String>, message: String) :
    HttpBadRequestException(ApiErrorCode.IRF400, message)
