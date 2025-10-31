package com.github.elimxim.flashcardsinspace.util

import com.github.elimxim.flashcardsinspace.entity.User
import org.slf4j.MDC
import java.io.PrintStream

const val APP_PACKAGE = "com.github.elimxim.flashcardsinspace"
const val USER_ID_MDC_KEY: String = "userId"

inline fun <T> withLoggingContext(block: () -> T): T {
    val user = getCurrentUser()
    return withLoggingContext(user?.id, block)
}

inline fun <T> withLoggingContext(user: User?, block: () -> T): T =
    withLoggingContext(user?.id, block)

inline fun <T> withLoggingContext(userId: Long?, block: () -> T): T {
    val previous = MDC.get(USER_ID_MDC_KEY)
    try {
        if (userId != null) {
            MDC.put(USER_ID_MDC_KEY, userId.toString())
        } else {
            MDC.remove(USER_ID_MDC_KEY)
        }
        return block()
    } finally {
        if (previous != null) {
            MDC.put(USER_ID_MDC_KEY, previous)
        }
    }
}

fun Throwable.printApplicationStackTrace() {
    this.printApplicationStackTrace(System.err)
}

fun Throwable.printApplicationStackTrace(out: PrintStream) {
    val user = getCurrentUser()

    val exceptionChain = mutableListOf<Pair<Throwable, List<StackTraceElement>>>()
    var currentException: Throwable? = this
    while (currentException != null) {
        val appStackTraces = currentException.stackTrace.filter {
            it.className.startsWith(APP_PACKAGE)
        }
        if (appStackTraces.isNotEmpty()) {
            exceptionChain.add(currentException to appStackTraces)
        }
        currentException = currentException.cause
    }

    if (exceptionChain.isEmpty()) {
        out.println("Application stack trace (${user?.id}): unknown")
        return
    }

    out.println("Application stack trace (${user?.id}):")
    exceptionChain.forEachIndexed { index, (exception, stackTraces) ->
        val indent = "  ".repeat(index)

        if (index > 0) {
            out.println("${indent}Caused by: ${exception.javaClass.simpleName}: ${exception.message}")
        }

        stackTraces.distinctBy {
            "${it.className}.${it.methodName}:${it.lineNumber}"
        }.forEach {
            out.println("$indent  at ${it.className}.${it.methodName}(${it.fileName}:${it.lineNumber})")
        }
    }
}
