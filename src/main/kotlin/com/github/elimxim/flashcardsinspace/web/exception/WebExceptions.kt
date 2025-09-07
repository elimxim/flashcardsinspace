package com.github.elimxim.flashcardsinspace.web.exception

// todo check if the codes are unique

open class WebException(val code: String, message: String) : Exception(message)

open class NotFoundException(code: String, message: String) : WebException(code, message)
class FlashcardSetNotFoundException(message: String) : NotFoundException(code = "RHR917", message)

open class BadRequestException(code: String, message: String) : WebException(code, message)
class FlashcardsSetAlreadyInitializedException(message: String) : BadRequestException(code = "2SP0RI", message)
class EmailIsAlreadyTakenException(message: String) : BadRequestException(code = "PI4SP6", message)

open class InternalServerErrorException(code: String, message: String) : WebException(code, message)
class CorruptedChronoStateException(message: String) : InternalServerErrorException(code = "N7S44T", message)


