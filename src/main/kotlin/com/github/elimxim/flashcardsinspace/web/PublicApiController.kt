package com.github.elimxim.flashcardsinspace.web

import com.github.elimxim.flashcardsinspace.service.LanguageService
import com.github.elimxim.flashcardsinspace.web.dto.LanguageDto
import com.github.elimxim.flashcardsinspace.web.dto.toDto
import org.springframework.http.CacheControl
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.TimeUnit

@RestController
@RequestMapping("/api-public")
class PublicApiController(
    private val languageService: LanguageService,
) {
    @GetMapping("/languages")
    fun getLanguages(): ResponseEntity<List<LanguageDto>> {
        val result = languageService.getAllLanguages().map { it.toDto() }
        return ResponseEntity.ok()
            .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic())
            .body(result)
    }
}
