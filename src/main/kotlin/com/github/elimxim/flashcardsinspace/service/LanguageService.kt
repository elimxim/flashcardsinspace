package com.github.elimxim.flashcardsinspace.service

import com.github.elimxim.flashcardsinspace.entity.Language
import com.github.elimxim.flashcardsinspace.entity.repository.LanguageRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class LanguageService(
    private val languageRepository: LanguageRepository,
) {
    @Transactional
    fun getAllLanguages(): List<Language> {
        return languageRepository.findAll().sortedBy { it.name }
    }

    @Transactional
    fun getLanguage(id: Long): Language {
        return languageRepository.getReferenceById(id) // fixme check for existence
    }
}