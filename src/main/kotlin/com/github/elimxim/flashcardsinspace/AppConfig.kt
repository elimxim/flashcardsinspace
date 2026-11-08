package com.github.elimxim.flashcardsinspace

import com.github.elimxim.flashcardsinspace.service.mail.MailProperties
import com.github.elimxim.flashcardsinspace.service.mail.MailPropertiesValidator
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Role
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
        @Bean
        @JvmStatic
        @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
        fun configurationPropertiesValidator(environment: Environment): Validator =
            MailPropertiesValidator(environment)
    }
}
