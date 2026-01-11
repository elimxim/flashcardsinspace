package com.github.elimxim.flashcardsinspace

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

const val CACHE_KEY_ALL = "all"

object CacheNames {
    const val LANGUAGE_LIST = "languageList"
    const val LANGUAGE_BY_ID = "languageById"
}

@Configuration
@EnableCaching
class CacheConfig {

    @Bean
    fun cacheManager(): CacheManager {
        val cacheManager = CaffeineCacheManager()
        cacheManager.registerCustomCache(
            CacheNames.LANGUAGE_LIST,
            Caffeine.newBuilder()
                .expireAfterWrite(40, TimeUnit.DAYS)
                .maximumSize(1)
                .build()
        )
        cacheManager.registerCustomCache(
            CacheNames.LANGUAGE_BY_ID,
            Caffeine.newBuilder()
                .expireAfterWrite(40, TimeUnit.DAYS)
                .maximumSize(400)
                .build()
        )
        return cacheManager
    }
}

