package com.poroshin.rut.ar.api.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.security.MessageDigest
import java.nio.charset.StandardCharsets

@Component
class ApiKeyFilter(
    private val adminProperties: AdminProperties,
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        if (!request.requestURI.startsWith(ADMIN_PREFIX)) {
            filterChain.doFilter(request, response)
            return
        }

        val configuredKey = adminProperties.apiKey
        val providedKey = request.getHeader(API_KEY_HEADER)

        if (configuredKey.isBlank() || providedKey.isNullOrBlank() || !constantTimeEquals(configuredKey, providedKey)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or missing X-Admin-Api-Key")
            return
        }

        filterChain.doFilter(request, response)
    }

    private fun constantTimeEquals(expected: String, provided: String): Boolean {
        val expectedBytes = expected.toByteArray(StandardCharsets.UTF_8)
        val providedBytes = provided.toByteArray(StandardCharsets.UTF_8)
        return MessageDigest.isEqual(expectedBytes, providedBytes)
    }

    private companion object {
        const val ADMIN_PREFIX = "/admin/"
        const val API_KEY_HEADER = "X-Admin-Api-Key"
    }
}
