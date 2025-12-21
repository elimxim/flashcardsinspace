package com.github.elimxim.flashcardsinspace.util

import com.github.elimxim.flashcardsinspace.entity.MetadataField
import com.github.elimxim.flashcardsinspace.web.exception.ExceptionHttpStatus
import com.github.elimxim.flashcardsinspace.web.exception.HttpException
import org.slf4j.LoggerFactory.getLogger
import org.springframework.http.HttpStatus
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.superclasses
import kotlin.reflect.jvm.javaField

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

fun getMetadataFieldName(field: KProperty1<*, *>): String? {
    val anno = getAnnotationFrom<MetadataField>(field)
    return anno?.name
}

inline fun <reified T: Annotation> getAnnotationFrom(property: KProperty1<*, *>): T? {
    property.findAnnotation<T>()?.let { return it } // kotlin property
    return property.javaField?.getAnnotation(T::class.java) // java field
}
