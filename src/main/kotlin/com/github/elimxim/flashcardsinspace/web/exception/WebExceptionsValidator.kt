package com.github.elimxim.flashcardsinspace.web.exception

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory
import org.springframework.core.type.filter.AssignableTypeFilter
import org.springframework.stereotype.Component

@Component
class WebExceptionsValidator : ApplicationRunner {
    override fun run(args: ApplicationArguments?) {
        val resolver = PathMatchingResourcePatternResolver()
        val metadataFactory = SimpleMetadataReaderFactory()
        val typeFilter = AssignableTypeFilter(HttpException::class.java)
        val resources = resolver.getResources(
            "classpath*:com/github/elimxim/flashcardsinspace/web/exception/**/*.class"
        )

        val codes = mutableSetOf<String>()
        for (resource in resources) {
            val metadataReader = metadataFactory.getMetadataReader(resource)
            if (!typeFilter.match(metadataReader, metadataFactory)) {
                continue
            }

            val exceptionClass = Class.forName(metadataReader.classMetadata.className)
            val exceptionIdAnnotation = exceptionClass.getAnnotation(ExceptionID::class.java)
            if (exceptionIdAnnotation != null) {
                val code = exceptionIdAnnotation.value
                if (!codes.add(code)) {
                    throw IllegalStateException(
                        """
                            Class ${exceptionClass.simpleName} has duplicated exception ID '$code'.
                            Exception IDs must be unique.
                        """.trimIndent()
                    )
                }
            }
        }
    }
}