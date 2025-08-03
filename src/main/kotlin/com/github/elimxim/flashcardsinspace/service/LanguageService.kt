package com.github.elimxim.flashcardsinspace.service

import com.github.elimxim.flashcardsinspace.entity.repository.LanguageRepository
import org.springframework.stereotype.Service

@Service
class LanguageService(
    private val languageRepository: LanguageRepository,
) {
    fun getAllLanguages() = languageRepository.findAll().sortedBy { it.name }
    fun getLanguage(id: Long) = languageRepository.findById(id)
}