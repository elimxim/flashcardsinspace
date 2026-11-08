package com.github.elimxim.flashcardsinspace

import com.github.elimxim.flashcardsinspace.entity.repository.*
import com.github.elimxim.flashcardsinspace.security.SecurityProperties
import io.mockk.mockk
import jakarta.persistence.EntityManager
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration
import org.springframework.boot.jdbc.autoconfigure.DataSourceTransactionManagerAutoConfiguration
import org.springframework.boot.liquibase.autoconfigure.LiquibaseAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableAutoConfiguration(
    exclude = [
        DataSourceAutoConfiguration::class,
        DataSourceTransactionManagerAutoConfiguration::class,
        HibernateJpaAutoConfiguration::class,
        LiquibaseAutoConfiguration::class,
        DataJpaRepositoriesAutoConfiguration::class,
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
    fun flashcardPictureRepository() = mockk<FlashcardPictureRepository>()

    @Bean
    fun dayStreakRepository() = mockk<DayStreakRepository>()

    @Bean
    fun reviewSessionRepository() = mockk<ReviewSessionRepository>()

    @Bean
    fun verificationIntentRepository() = mockk<VerificationIntentRepository>()

    @Bean
    fun entityManager() = mockk<EntityManager>()
}
