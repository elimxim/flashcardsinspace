package com.github.elimxim.flashcardsinspace.web.exception

import org.springframework.http.HttpStatus

annotation class ExceptionHttpStatus(val value: HttpStatus)
annotation class ExceptionID(val value: String)

abstract class HttpException(message: String) : Exception(message)
abstract class Http4xxException(message: String) : HttpException(message)
abstract class Http5xxException(message: String) : HttpException(message)

@ExceptionHttpStatus(HttpStatus.BAD_REQUEST)
abstract class BadRequestException(message: String) : Http4xxException(message)
@ExceptionHttpStatus(HttpStatus.NOT_FOUND)
abstract class NotFoundException(message: String) : Http4xxException(message)
@ExceptionHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
abstract class InternalServerErrorException(message: String) : Http5xxException(message)

@ExceptionID("JFK90J")
class FlashcardSetNotFoundException(message: String) : NotFoundException(message)
@ExceptionID("2SP0RI")
class FlashcardsSetAlreadyInitializedException(message: String) : BadRequestException(message)
@ExceptionID("PI4SP6")
class EmailIsAlreadyTakenException(message: String) : BadRequestException(message)
@ExceptionID("N7S44T")
class CorruptedChronoStateException(message: String) : InternalServerErrorException(message)
