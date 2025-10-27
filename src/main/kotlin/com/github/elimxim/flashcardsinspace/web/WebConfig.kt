package com.github.elimxim.flashcardsinspace.web

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.util.AntPathMatcher
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

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
                .modules(JavaTimeModule())
        }
    }

    override fun configurePathMatch(configurer: PathMatchConfigurer) {
        val matcher = AntPathMatcher()
        matcher.setCaseSensitive(false)
        configurer.setPathMatcher(matcher)
    }
}
