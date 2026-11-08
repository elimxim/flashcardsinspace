package com.github.elimxim.flashcardsinspace.web

import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

private val log = LoggerFactory.getLogger(IndexPageController::class.java)

@Controller
class IndexPageController(
    private val indexPageRenderer: IndexPageRenderer,
) {
    @GetMapping(
        "/",
        "/control-panel",
        "/user",
        "/get-help",
        "/website-guide",
        "/emotional-support",
        "/signup",
        "/login",
        "/logout",
        "/password-reset",
        "/email-confirmation",
        "/code-verification",
        "/review",
    )
    fun page(request: HttpServletRequest): ResponseEntity<String> {
        log.debug("Serving index.html for '${request.requestURI}'")
        val html = indexPageRenderer.render(request.requestURI)
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok()
            .contentType(MediaType.TEXT_HTML)
            .body(html)
    }
}
