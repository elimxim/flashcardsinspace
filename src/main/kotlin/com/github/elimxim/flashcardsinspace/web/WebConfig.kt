package com.github.elimxim.flashcardsinspace.web

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.CacheControl
import org.springframework.util.AntPathMatcher
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.util.concurrent.TimeUnit

@Configuration
class WebConfig : WebMvcConfigurer {
    @Bean
    fun jacksonCustomizer(): Jackson2ObjectMapperBuilderCustomizer {
        return Jackson2ObjectMapperBuilderCustomizer { builder ->
            builder
                // Disable writing dates as timestamps (arrays of numbers)
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                // Disable adjusting dates to context timezone during deserialization
                .featuresToDisable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
                // Enable JavaTimeModule for proper LocalDate and ZonedDateTime serialization
                .modulesToInstall(JavaTimeModule())
        }
    }

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/assets/**")
            .addResourceLocations("classpath:/static/assets/")
            .setCacheControl(
                CacheControl.maxAge(30, TimeUnit.DAYS)
                    .cachePublic()
                    .immutable()
            )

        registry.addResourceHandler("/images/**")
            .addResourceLocations("classpath:/static/images/")
            .setCacheControl(
                CacheControl.maxAge(30, TimeUnit.DAYS)
                    .cachePublic()
            )
    }

    override fun configurePathMatch(configurer: PathMatchConfigurer) {
        val matcher = AntPathMatcher()
        matcher.setCaseSensitive(false)
        configurer.setPathMatcher(matcher)
    }
}
