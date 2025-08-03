package com.github.elimxim.flashcardsinspace.web

import com.github.elimxim.flashcardsinspace.service.LanguageService
import com.github.elimxim.flashcardsinspace.web.dto.LanguageResponse
import com.github.elimxim.flashcardsinspace.web.dto.toDto
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/public/languages")
class LanguageController(
    private val languageService: LanguageService,
) {
    @GetMapping()
    fun getLanguages(): ResponseEntity<LanguageResponse> {
        val languages = languageService.getAllLanguages().map { it.toDto() }
        return ResponseEntity.ok(LanguageResponse(languages))
    }
}