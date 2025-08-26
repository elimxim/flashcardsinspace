package com.github.elimxim.flashcardsinspace.web.filter

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.ContentCachingResponseWrapper
import java.nio.charset.StandardCharsets

private val log = LoggerFactory.getLogger(RequestLoggingFilter::class.java)

@Component
class RequestLoggingFilter(
    private val objectMapper: ObjectMapper,
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val wrappedRequest = ContentCachingRequestWrapper(request)
        val wrappedResponse = ContentCachingResponseWrapper(response)

        try {
            val startTime = System.currentTimeMillis()
            // processing the request so the body can be cached
            filterChain.doFilter(wrappedRequest, wrappedResponse)
            val duration = System.currentTimeMillis() - startTime
            logRequestDetails(wrappedRequest, wrappedResponse, duration)
        } finally {
            // coping the cached response body to the actual response
            wrappedResponse.copyBodyToResponse()
        }

    }

    private fun logRequestDetails(
        request: ContentCachingRequestWrapper,
        response: ContentCachingResponseWrapper,
        duration: Long
    ) {
        val principal = SecurityContextHolder.getContext().authentication?.name ?: "ANONYMOUS"
        val clientIp = request.getHeader("X-Forwarded-For") ?: request.remoteAddr
        val requestQuery = request.queryString?.let { "?$it" } ?: ""
        val userAgent = request.getHeader("User-Agent") ?: "N/A"
        val headers = headers(request)
        val body = requestBody(request)

        val requestDetails = mapOf(
            "Principal" to principal,
            "Client IP" to clientIp,
            "Request" to "${request.method} ${request.requestURI}$requestQuery",
            "Status" to response.status,
            "Duration" to "${duration}ms",
            "User-Agent" to userAgent,
            "Headers" to headers,
            "Request Body" to body
        )

        log.info(objectMapper.writeValueAsString(requestDetails))
    }

    private fun headers(request: HttpServletRequest): Map<String, String> =
        buildMap {
            for (headerName in request.headerNames.asSequence()) {
                if (headerName == "cookie") continue
                put(headerName, request.getHeader(headerName))
            }
        }

    private fun requestBody(request: ContentCachingRequestWrapper): Any {
        val requestBody = request.contentAsByteArray
        if (requestBody.isEmpty()) return "NA"

        return try {
            objectMapper.readTree(requestBody)
        } catch (_: Exception) {
            String(requestBody, StandardCharsets.UTF_8)
        }
    }

}