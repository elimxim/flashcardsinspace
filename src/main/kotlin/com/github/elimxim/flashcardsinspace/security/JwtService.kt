package com.github.elimxim.flashcardsinspace.security

import com.github.elimxim.flashcardsinspace.entity.User
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import jakarta.annotation.PostConstruct
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.util.*
import javax.crypto.SecretKey

@Service
class JwtService(private val jwtProperties: JwtProperties) {
    private lateinit var secretKey: SecretKey

    @PostConstruct
    fun init() {
        secretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(jwtProperties.secret))
    }

    fun extractUsername(token: String): String = extractClaim(token, Claims::getSubject)

    fun isTokenValid(token: String, userDetails: UserDetails): Boolean {
        val username = extractUsername(token)
        return username == userDetails.username && !isTokenExpired(token)
    }

    fun setCookies(userDetails: UserDetails, response: HttpServletResponse) {
        val accessToken = generateAccessToken(userDetails)
        val accessTokenCookie = Cookie("accessToken", accessToken).apply {
            isHttpOnly = true
            secure = true
            path = "/"
            maxAge = (jwtProperties.accessTokenExpirationMs / 1000).toInt()
        }

        val refreshToken = generateRefreshToken(userDetails)
        val refreshTokenCookie = Cookie("refreshToken", refreshToken).apply {
            isHttpOnly = true
            secure = true
            path = "/api/auth/refresh-token"
            maxAge = (jwtProperties.refreshTokenExpirationMs / 1000).toInt()
        }

        response.addCookie(accessTokenCookie)
        response.addCookie(refreshTokenCookie)
    }

    fun clearCookies(response: HttpServletResponse) {
        val accessTokenCookie = Cookie("accessToken", null).apply {
            isHttpOnly = true
            secure = true
            path = "/"
            maxAge = 0
        }

        val refreshTokenCookie = Cookie("refreshToken", null).apply {
            isHttpOnly = true
            secure = true
            path = "/api/auth/refresh-token"
            maxAge = 0
        }

        response.addCookie(accessTokenCookie)
        response.addCookie(refreshTokenCookie)
    }

    private fun <T> extractClaim(token: String, claimsResolver: (Claims) -> T): T {
        val claims = extractAllClaims(token)
        return claimsResolver(claims)
    }

    private fun generateAccessToken(userDetails: UserDetails): String {
        return generateToken(userDetails, jwtProperties.accessTokenExpirationMs)
    }

    private fun generateRefreshToken(userDetails: UserDetails): String {
        return generateToken(userDetails, jwtProperties.refreshTokenExpirationMs)
    }

    private fun generateToken(userDetails: UserDetails, expirationMs: Long): String {
        val claims = mutableMapOf<String, Any>()
        if (userDetails is User) {
            claims["roles"] = userDetails.roles
        }
        return Jwts.builder()
            .setClaims(claims)
            .setSubject(userDetails.username)
            .setIssuedAt(Date(System.currentTimeMillis()))
            .setExpiration(Date(System.currentTimeMillis() + expirationMs))
            .signWith(secretKey)
            .compact()
    }

    private fun isTokenExpired(token: String): Boolean = extractExpiration(token).before(Date())

    private fun extractExpiration(token: String): Date = extractClaim(token, Claims::getExpiration)

    private fun extractAllClaims(token: String): Claims =
        Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
            .body
}