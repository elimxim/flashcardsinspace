package com.github.elimxim.flashcardsinspace.util

val numbersOnlyPattern = Regex("^\\d+$")
val newLinePattern = Regex("\\r?\\n")

fun String.trimOneLine() = this.trimIndent()
    .split(newLinePattern)
    .filter { it.isNotBlank() }
    .joinToString(separator = " ") {
        it.trim()
    }
