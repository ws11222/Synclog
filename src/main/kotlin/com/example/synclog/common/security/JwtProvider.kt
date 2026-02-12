package com.example.synclog.common.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.Date

@Component
class JwtProvider {
    private val secretKey =
        System.getenv("JWT_SECRET_KEY")
            ?.let { Keys.hmacShaKeyFor(it.toByteArray(StandardCharsets.UTF_8)) }
            ?: throw IllegalStateException("JWT_SECRET_KEY is not set!")
    private val expirationTime = 1000 * 60 * 60 * 2 // 2 hours

    fun generateToken(id: String): String {
        return Jwts.builder()
            .setSubject(id)
            .signWith(secretKey)
            .setExpiration(Date(Date().time + expirationTime))
            .compact()
    }

    fun getUserIdFromToken(token: String): String {
        val claims =
            Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
        return claims.body.subject
    }

    fun validateToken(token: String): Boolean {
        return try {
            val claims =
                Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
            !claims.body.expiration.before(Date())
        } catch (e: Exception) {
            false
        }
    }
}
