package com.github.elimxim.flashcardsinspace

import com.github.elimxim.flashcardsinspace.service.mail.MailProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.ReloadableResourceBundleMessageSource

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
}
