package com.github.elimxim.flashcardsinspace.service

import com.github.elimxim.flashcardsinspace.entity.Language
import com.github.elimxim.flashcardsinspace.entity.repository.LanguageRepository
import com.github.elimxim.flashcardsinspace.web.exception.ApiErrorCode
import com.github.elimxim.flashcardsinspace.web.exception.HttpNotFoundException
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
    fun getEntity(id: Long): Language {
        return languageRepository.findById(id).orElseThrow {
            HttpNotFoundException(ApiErrorCode.LAN404, "Language with id $id not found")
        }
    }
}
