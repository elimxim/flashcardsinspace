package com.github.elimxim.flashcardsinspace.web.exception

import com.github.elimxim.flashcardsinspace.Messages
import org.slf4j.LoggerFactory.getLogger
import org.springframework.http.HttpStatus
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.superclasses

private val log = getLogger("AnnotationSupport")

fun getHttpStatus(e: HttpException): HttpStatus {
    val anno = findAnnotationInClassHierarchy<ExceptionHttpStatus>(e::class)
    return if (anno == null) {
        log.error("Couldn't extract exception status from ${e::class.simpleName}")
        HttpStatus.INTERNAL_SERVER_ERROR
    } else {
        anno.value
    }
}

fun getErrorCode(e: HttpException?): String {
    if (e == null) return "######"
    val anno = findAnnotationInClassHierarchy<ErrorCode>(e::class)
    return anno?.value ?: "######"
}

fun getUserMessage(e: HttpException?): String? {
    if (e == null) return null
    val anno = findAnnotationInClassHierarchy<UserMessageCode>(e::class)
    return anno?.let { Messages.getMessage(anno.value, e.args) }
}

private inline fun <reified T : Annotation> findAnnotationInClassHierarchy(kClass: KClass<*>): T? {
    val classHierarchy = mutableListOf(kClass)
    var currentClass: KClass<*>? = kClass
    while (currentClass != null) {
        val anno = currentClass.findAnnotation<T>()
        if (anno != null) return anno
        currentClass = currentClass.superclasses.firstOrNull()
        currentClass?.let { classHierarchy.add(it) }
    }

    val classHierarchyStr = classHierarchy.mapNotNull { it.simpleName }.joinToString(" -> ")
    log.error("Couldn't find ${UserMessageCode::class.simpleName} in class hierarchy $classHierarchyStr")
    return null
}
