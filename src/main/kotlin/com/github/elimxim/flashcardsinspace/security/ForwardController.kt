package com.github.elimxim.flashcardsinspace.security

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

private val log = LoggerFactory.getLogger(ForwardController::class.java)

@Controller
class ForwardController {
    @RequestMapping(value = ["/{path:^(?!api|api-public|auth|assets|images|\\.well-known|.*\\.).*}/**"])
    fun forward(@PathVariable path: String): String {
        log.debug("Forwarding '$path' to index.html")
        return "forward:/index.html"
    }
}