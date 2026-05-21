package com.poroshin.rut.ar.api.config

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

@Component
@Order(1)
class RateLimitFilter(
    @Value("\${rate-limit.admin.requests-per-minute:10}") private val adminRequestsPerMinute: Long,
    @Value("\${rate-limit.public.requests-per-minute:60}") private val publicRequestsPerMinute: Long,
) : OncePerRequestFilter() {

    private val adminBuckets = ConcurrentHashMap<String, Bucket>()
    private val publicBuckets = ConcurrentHashMap<String, Bucket>()

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val uri = request.requestURI
        val bucket: Bucket
        val key: String

        when {
            uri.startsWith(ADMIN_PREFIX) -> {
                key = resolveClientIp(request)
                bucket = adminBuckets.computeIfAbsent(key) { buildBucket(adminRequestsPerMinute) }
            }
            uri.startsWith(PUBLIC_PREFIX) -> {
                key = request.getHeader(DEVICE_ID_HEADER) ?: resolveClientIp(request)
                bucket = publicBuckets.computeIfAbsent(key) { buildBucket(publicRequestsPerMinute) }
            }
            else -> {
                filterChain.doFilter(request, response)
                return
            }
        }

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response)
        } else {
            response.status = STATUS_TOO_MANY_REQUESTS
            response.setHeader(RETRY_AFTER_HEADER, RETRY_AFTER_SECONDS)
            response.contentType = "application/json"
            response.writer.write("""{"error":"Too many requests. Please retry later."}""")
        }
    }

    private fun buildBucket(requestsPerMinute: Long): Bucket =
        Bucket.builder()
            .addLimit(
                Bandwidth.builder()
                    .capacity(requestsPerMinute)
                    .refillGreedy(requestsPerMinute, Duration.ofMinutes(1))
                    .build(),
            )
            .build()

    private fun resolveClientIp(request: HttpServletRequest): String {
        val forwarded = request.getHeader(X_FORWARDED_FOR_HEADER)
        return if (!forwarded.isNullOrBlank()) forwarded.split(",").first().trim() else request.remoteAddr
    }

    private companion object {
        const val ADMIN_PREFIX = "/admin/"
        const val PUBLIC_PREFIX = "/api/"
        const val DEVICE_ID_HEADER = "X-Device-Id"
        const val X_FORWARDED_FOR_HEADER = "X-Forwarded-For"
        const val RETRY_AFTER_HEADER = "Retry-After"
        const val RETRY_AFTER_SECONDS = "60"
        const val STATUS_TOO_MANY_REQUESTS = 429
    }
}
