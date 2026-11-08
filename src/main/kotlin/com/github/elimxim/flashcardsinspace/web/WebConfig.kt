package com.github.elimxim.flashcardsinspace.web

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.CacheControl
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.util.pattern.PathPatternParser
import tools.jackson.databind.cfg.DateTimeFeature
import java.util.concurrent.TimeUnit

@Configuration
class WebConfig : WebMvcConfigurer {
    @Bean
    fun jacksonCustomizer(): JsonMapperBuilderCustomizer {
        return JsonMapperBuilderCustomizer { builder ->
            builder
                // Disable writing dates as timestamps (arrays of numbers)
                .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                // Disable adjusting dates to context timezone during deserialization
                .disable(DateTimeFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
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
        val parser = PathPatternParser()
        parser.isCaseSensitive = false
        configurer.setPatternParser(parser)
    }
}
