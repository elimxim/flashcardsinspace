package com.github.elimxim.flashcardsinspace

import com.github.elimxim.flashcardsinspace.entity.repository.*
import com.github.elimxim.flashcardsinspace.security.SecurityProperties
import io.mockk.mockk
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableAutoConfiguration(
    exclude = [
        DataSourceAutoConfiguration::class,
        DataSourceTransactionManagerAutoConfiguration::class,
        HibernateJpaAutoConfiguration::class,
        LiquibaseAutoConfiguration::class,
        JpaRepositoriesAutoConfiguration::class,
    ]
)
@EnableConfigurationProperties(SecurityProperties::class)
class TestConfigurationContext {
    @Bean
    fun userRepository() = mockk<UserRepository>()

    @Bean
    fun languageRepository() = mockk<LanguageRepository>()

    @Bean
    fun flashcardSetRepository() = mockk<FlashcardSetRepository>()

    @Bean
    fun flashcardRepository() = mockk<FlashcardRepository>()

    @Bean
    fun chronodayRepository() = mockk<ChronodayRepository>()

    @Bean
    fun flashcardAudioRepository() = mockk<FlashcardAudioRepository>()

    @Bean
    fun dayStreakRepository() = mockk<DayStreakRepository>()
}
