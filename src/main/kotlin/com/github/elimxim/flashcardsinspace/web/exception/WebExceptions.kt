package com.github.elimxim.flashcardsinspace.web.exception

open class NotFoundException(message: String) : Exception(message)
class FlashcardSetNotFoundException(message: String) : NotFoundException(message)

open class BadRequestException(message: String) : Exception(message)
class FlashcardsSetAlreadyInitializedException(message: String) : BadRequestException(message)

open class InternalServerErrorException(message: String) : Exception(message)
class CorruptedChronoStateException(message: String) : InternalServerErrorException(message)


