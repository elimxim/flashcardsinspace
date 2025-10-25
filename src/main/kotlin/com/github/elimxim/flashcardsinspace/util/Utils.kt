package com.github.elimxim.flashcardsinspace.util

fun String.trimOneLine() = this.trimIndent()
    .split(Regex("\\r?\\n"))
    .filter { it.isNotBlank() }
    .joinToString(separator = " ") {
        it.trim()
    }
