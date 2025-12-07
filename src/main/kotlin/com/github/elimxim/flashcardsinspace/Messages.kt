package com.github.elimxim.flashcardsinspace

import com.github.elimxim.flashcardsinspace.web.exception.HttpException
import com.github.elimxim.flashcardsinspace.web.exception.UserMessageCode
import com.github.elimxim.flashcardsinspace.util.findAnnotationInClassHierarchy
import org.slf4j.LoggerFactory
import org.springframework.context.MessageSource
import org.springframework.context.NoSuchMessageException
import org.springframework.stereotype.Component
import java.util.*

private val log = LoggerFactory.getLogger(Messages::class.java)

@Component
class Messages(private val messageSource: MessageSource) {
    fun getMessage(code: String, args: List<Any> = emptyList(), humor: Boolean = true): String {
        return try {
            val msgCode = if (humor) "$code.humor" else code
            messageSource.getMessage(msgCode, args.toTypedArray(), Locale.getDefault())
        } catch (e: NoSuchMessageException) {
            log.error("Message with code $code not found", e)
            return code
        }
    }

    fun getMessage(e: HttpException?, humor: Boolean = true): String? {
        if (e == null) return null
        val anno = findAnnotationInClassHierarchy<UserMessageCode>(e::class)
        return anno?.let { getMessage(anno.value, e.args, humor) }
    }
}
