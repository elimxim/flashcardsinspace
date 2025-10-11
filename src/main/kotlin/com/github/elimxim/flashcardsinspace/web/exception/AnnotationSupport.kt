package com.github.elimxim.flashcardsinspace.web.exception

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

fun getErrorCode(e: HttpException): String {
    val anno = findAnnotationInClassHierarchy<ErrorCode>(e::class)
    return anno?.value ?: "000042"
}

inline fun <reified T : Annotation> findAnnotationInClassHierarchy(kClass: KClass<*>): T? {
    val classHierarchy = mutableListOf(kClass)
    var currentClass: KClass<*>? = kClass
    while (currentClass != null) {
        val anno = currentClass.findAnnotation<T>()
        if (anno != null) return anno
        currentClass = currentClass.superclasses.firstOrNull()
        currentClass?.let { classHierarchy.add(it) }
    }
    return null
}
