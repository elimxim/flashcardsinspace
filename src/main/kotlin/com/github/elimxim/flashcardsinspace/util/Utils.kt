package com.github.elimxim.flashcardsinspace.util

fun List<String>.joinWithBraces() =
    this.joinToString(separator = "', '", prefix = "'", postfix = "'")

fun String.trimOneLine() = this.trimIndent()
    .split(Regex("\\r?\\n"))
    .filter { it.isNotBlank() }
    .joinToString(separator = " ") {
        it.trim()
    }
