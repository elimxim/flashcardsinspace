package com.github.elimxim.flashcardsinspace.security

import com.github.elimxim.flashcardsinspace.web.IndexPageRenderer
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

private val log = LoggerFactory.getLogger(ForwardController::class.java)

@Controller
class ForwardController(
    private val indexPageRenderer: IndexPageRenderer,
) {
    @RequestMapping(value = ["/{path:^(?!api|api-public|auth|actuator|assets|images|\\.well-known|.*\\.).*}/**"])
    fun forward(request: HttpServletRequest): ResponseEntity<String> {
        log.debug("Serving index.html for '${request.requestURI}'")
        val html = indexPageRenderer.render(request.requestURI)
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok()
            .contentType(MediaType.TEXT_HTML)
            .body(html)
    }
}
