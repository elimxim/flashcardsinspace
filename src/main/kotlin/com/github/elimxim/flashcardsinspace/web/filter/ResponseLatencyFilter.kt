package com.github.elimxim.flashcardsinspace.web.filter

import com.github.elimxim.flashcardsinspace.DevProperties
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

private val log = LoggerFactory.getLogger(ResponseLatencyFilter::class.java)

@Component
@ConditionalOnProperty(name = ["app.dev.response-latency.enabled"], havingValue = "true", matchIfMissing = false)
class ResponseLatencyFilter(
    private val devProperties: DevProperties,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        log.info("Delaying response ${request.requestURI} by ${devProperties.responseLatency.delayMs}ms")
        Thread.sleep(devProperties.responseLatency.delayMs)
        filterChain.doFilter(request, response)
    }
}
