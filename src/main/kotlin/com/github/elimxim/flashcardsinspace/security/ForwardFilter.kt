package com.github.elimxim.flashcardsinspace.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

private val log = LoggerFactory.getLogger(ForwardFilter::class.java)

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
class ForwardFilter : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val path = request.servletPath

        val isExcludedPath = path.startsWith("/api")
                || path.startsWith("/api-public")
                || path.startsWith("/auth")
                || path.startsWith("/assets")
                || path.startsWith("/images")
                || path.startsWith("/.well-known")
                || path.substringAfterLast('/').contains('.')

        if (isExcludedPath) {
            filterChain.doFilter(request, response)
        } else {
            log.debug("Forwarding request for '{}' to /index.html", path)
            request.getRequestDispatcher("/index.html").forward(request, response)
        }
    }
}