package com.github.elimxim.flashcardsinspace.util

val numbersOnlyPattern = Regex("^\\d+$")

fun String.trimOneLine() = this.trimIndent()
    .split(Regex("\\r?\\n"))
    .filter { it.isNotBlank() }
    .joinToString(separator = " ") {
        it.trim()
    }
