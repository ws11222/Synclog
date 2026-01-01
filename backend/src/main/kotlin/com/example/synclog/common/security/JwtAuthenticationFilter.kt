package com.example.synclog.common.security

import com.example.synclog.common.exception.AuthenticateException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.servlet.HandlerExceptionResolver

@Component
class JwtAuthenticationFilter(
    private val jwtProvider: JwtProvider,
    @Qualifier("handlerExceptionResolver") private val resolver: HandlerExceptionResolver,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain,
    ) {
        // 1. Bearer 토큰 추출
        val token = extractBearerToken(request)

        // 2. 토큰이 없으면 그냥 다음 필터로 넘김 (인증이 필요한 경로는 SecurityConfig에서 차단됨)
        if (token == null) {
            chain.doFilter(request, response)
            return
        }

        // 3. 토큰 검증
        runCatching {
            val userId = jwtProvider.getUserIdFromToken(token)
            setAuthenticatedUser(userId, request)
        }.onFailure {
            resolver.resolveException(request, response, null, AuthenticateException())
            return
        }

        chain.doFilter(request, response)
    }

    private fun extractBearerToken(request: HttpServletRequest): String? =
        request.getHeader("Authorization")
            ?.takeIf { it.startsWith("Bearer ", ignoreCase = true) }
            ?.substring(7)
            ?.trim()

    private fun setAuthenticatedUser(
        userId: String,
        request: HttpServletRequest,
    ) {
        val authentication = UsernamePasswordAuthenticationToken(userId, null, listOf(SimpleGrantedAuthority("ROLE_USER")))
        SecurityContextHolder.getContext().authentication = authentication
        request.setAttribute("userId", userId)
    }
}
