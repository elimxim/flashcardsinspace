package com.github.elimxim.flashcardsinspace.service

import com.github.elimxim.flashcardsinspace.CACHE_KEY_ALL
import com.github.elimxim.flashcardsinspace.CacheNames
import com.github.elimxim.flashcardsinspace.entity.Language
import com.github.elimxim.flashcardsinspace.entity.repository.LanguageRepository
import com.github.elimxim.flashcardsinspace.web.exception.ApiErrorCode
import com.github.elimxim.flashcardsinspace.web.exception.HttpNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.cache.CacheManager
import org.springframework.cache.get
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val log = LoggerFactory.getLogger(LanguageService::class.java)

@Service
class LanguageService(
    private val languageRepository: LanguageRepository,
    private val cacheManager: CacheManager,
) {
    @Transactional(readOnly = true)
    fun getAllLanguages(): List<Language> {
        val cache = cacheManager.getCache(CacheNames.LANGUAGE_LIST)
        val cached = cache?.get<List<Language>>(CACHE_KEY_ALL)
        if (cached != null) {
            log.debug("Cache hit for all languages")
            return cached
        }

        log.debug("Cache miss for all languages, fetching from database")
        val languages = languageRepository.findAll().sortedBy { it.name }
        cache?.put(CACHE_KEY_ALL, languages)
        return languages
    }

    @Transactional(readOnly = true)
    fun getEntity(id: Long): Language {
        val cache = cacheManager.getCache(CacheNames.LANGUAGE_BY_ID)
        val cached = cache?.get<Language>(id)
        if (cached != null) {
            log.debug("Cache hit for language id=$id")
            return cached
        }

        log.debug("Cache miss for language id=$id, fetching from database")
        val language = languageRepository.findById(id).orElseThrow {
            HttpNotFoundException(ApiErrorCode.LAN404, "Language with id $id not found")
        }
        cache?.put(id, language)
        return language
    }
}
