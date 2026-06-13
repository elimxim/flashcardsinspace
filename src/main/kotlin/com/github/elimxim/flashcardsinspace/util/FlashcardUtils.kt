package com.github.elimxim.flashcardsinspace.util

import com.github.elimxim.flashcardsinspace.entity.FlashcardSide

fun String.parseSide(): FlashcardSide? = when {
    FlashcardSide.FRONT.name.equals(this, ignoreCase = true) -> FlashcardSide.FRONT
    FlashcardSide.BACK.name.equals(this, ignoreCase = true) -> FlashcardSide.BACK
    else -> null
}

fun String.parseSideOrThrow(exceptionBuilder: (String) -> Exception): FlashcardSide {
    return this.parseSide() ?: throw exceptionBuilder(this)
}
