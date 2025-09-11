package com.github.elimxim.flashcardsinspace.web.exception

import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

private val log = LoggerFactory.getLogger(HttpExceptionsValidator::class.java)

@Component
class HttpExceptionsValidator : ApplicationRunner {
    override fun run(args: ApplicationArguments?) {
        val codes = mutableSetOf<String>()
        HttpException::class.sealedSubclasses.forEach { httpGroupException ->
            log.info("Scanning exception class ${httpGroupException.simpleName}")
            httpGroupException::class.sealedSubclasses.forEach { httpCodeException ->
                log.info("Checking ${httpCodeException.simpleName}")
                checkExceptionHttpStatus(httpCodeException)
                httpCodeException::class.sealedSubclasses.forEach { httpUserException ->
                    log.info("Checking ${httpUserException.simpleName}")
                    checkErrorCode(httpUserException, codes)
                    checkExceptionUserMessageCode(httpCodeException)
                }
            }
        }
    }

    private fun checkExceptionHttpStatus(kClass: KClass<*>) {
        kClass.findAnnotation<ExceptionHttpStatus>()
            ?: throw IllegalStateException(
                """
                    Class ${kClass.simpleName} doesn't have defined ${ExceptionHttpStatus::class.simpleName}.
                    ${ExceptionHttpStatus::class.simpleName} must be defined.
                    """.trimIndent()
            )
    }
}

private fun checkErrorCode(kClass: KClass<*>, codes: MutableSet<String>) {
    val anno = kClass.findAnnotation<ErrorCode>()
        ?: throw IllegalStateException(
            """
                Class ${kClass.simpleName} doesn't have defined ${ErrorCode::class.simpleName}.
                ${ErrorCode::class.simpleName} must be defined.
                """.trimIndent()
        )

    if (anno.value.isBlank()) {
        throw IllegalStateException(
            """
                Class ${kClass.simpleName} has blank ${ErrorCode::class.simpleName}.
                ${ErrorCode::class.simpleName} must have non-blank value.
                """
        )
    } else if (!codes.add(anno.value)) {
        throw IllegalStateException(
            """
                Class ${kClass.simpleName} has duplicate ${ErrorCode::class.simpleName}='${anno.value}'.
                ${ErrorCode::class.simpleName} must be unique.
                """.trimIndent()
        )
    }
}

private fun checkExceptionUserMessageCode(kClass: KClass<*>) {
    val anno = kClass.findAnnotation<UserMessageCode>()
        ?: throw IllegalStateException(
            """
                Class ${kClass.simpleName} doesn't have defined ${UserMessageCode::class.simpleName}.
                ${UserMessageCode::class.simpleName} must be defined.
                """.trimIndent()
        )

    if (anno.value.isBlank()) {
        throw IllegalStateException(
            """
                Class ${kClass.simpleName} has blank ${UserMessageCode::class.simpleName}.
                ${UserMessageCode::class.simpleName} must have non-blank value.
                """
        )
    }
}
