package com.github.elimxim.flashcardsinspace

import com.github.elimxim.flashcardsinspace.service.mail.MailProperties
import com.github.elimxim.flashcardsinspace.service.mail.MailPropertiesValidator
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.ReloadableResourceBundleMessageSource
import org.springframework.core.env.Environment
import org.springframework.validation.Validator

@Configuration
@EnableConfigurationProperties(
    MailProperties::class,
    DevProperties::class
)
class AppConfig {
    @Bean
    fun messageSource(): MessageSource {
        val messageSource = ReloadableResourceBundleMessageSource()
        messageSource.setBasename("classpath:messages")
        messageSource.setDefaultEncoding("UTF-8")
        return messageSource
    }

    companion object {
        /**
         * Spring Boot applies a bean with this exact name to every @ConfigurationProperties
         * class while binding; it must be static since binding happens early.
         */
        @Bean
        @JvmStatic
        fun configurationPropertiesValidator(environment: Environment): Validator =
            MailPropertiesValidator(environment)
    }
}
