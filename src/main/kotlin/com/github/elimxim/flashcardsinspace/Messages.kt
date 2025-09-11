package com.github.elimxim.flashcardsinspace

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.context.NoSuchMessageException
import org.springframework.stereotype.Component
import java.util.*

private val log = LoggerFactory.getLogger(Messages::class.java)

@Component
object Messages {
    @Autowired
    private lateinit var messageSource: MessageSource

    fun getMessage(code: String, args: List<Any> = emptyList()): String {
        return try {
          messageSource.getMessage(code, args.toTypedArray(), Locale.getDefault())
        } catch (e: NoSuchMessageException) {
            log.error("Message with code $code not found", e)
            return code;
        }
    }
}
