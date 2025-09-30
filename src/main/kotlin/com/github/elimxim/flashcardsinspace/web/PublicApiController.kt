package com.github.elimxim.flashcardsinspace.web

import com.github.elimxim.flashcardsinspace.service.LanguageService
import com.github.elimxim.flashcardsinspace.web.dto.LanguagesGetResponse
import com.github.elimxim.flashcardsinspace.web.dto.toDto
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api-public")
class PublicApiController(
    private val languageService: LanguageService,
) {
    @GetMapping("/languages")
    fun getLanguages(): ResponseEntity<LanguagesGetResponse> {
        val result = languageService.getAllLanguages().map { it.toDto() }
        return ResponseEntity.ok(LanguagesGetResponse(result))
    }
}
