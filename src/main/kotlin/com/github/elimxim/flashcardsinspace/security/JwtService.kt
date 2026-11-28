package com.github.elimxim.flashcardsinspace.security

import com.github.elimxim.flashcardsinspace.entity.User
import com.github.elimxim.flashcardsinspace.util.addAccessTokenCookie
import com.github.elimxim.flashcardsinspace.util.addRefreshTokenCookie
import com.github.elimxim.flashcardsinspace.util.clearAccessTokenCookie
import com.github.elimxim.flashcardsinspace.util.clearRefreshTokenCookie
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import jakarta.annotation.PostConstruct
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.util.*
import javax.crypto.SecretKey

@Service
class JwtService(private val securityProperties: SecurityProperties) {
    private lateinit var secretKey: SecretKey

    @PostConstruct
    fun init() {
        secretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(securityProperties.jwt.secret))
    }

    fun extractUsername(token: String): String = extractClaim(token, Claims::getSubject)

    fun isTokenValid(token: String, userDetails: UserDetails): Boolean {
        val username = extractUsername(token)
        return username == userDetails.username && !isTokenExpired(token)
    }

    fun setCookies(userDetails: UserDetails, response: HttpServletResponse) {
        val secure = securityProperties.secureCookies
        val accessMaxAge = (securityProperties.jwt.accessTokenExpirationMs / 1000).toInt()
        val refreshMaxAge = (securityProperties.jwt.refreshTokenExpirationMs / 1000).toInt()
        addAccessTokenCookie(response, generateAccessToken(userDetails), accessMaxAge, secure)
        addRefreshTokenCookie(response, generateRefreshToken(userDetails), refreshMaxAge, secure)
    }

    fun clearCookies(response: HttpServletResponse) {
        clearAccessTokenCookie(response, securityProperties.secureCookies)
        clearRefreshTokenCookie(response, securityProperties.secureCookies)
    }

    private fun <T> extractClaim(token: String, claimsResolver: (Claims) -> T): T {
        val claims = extractAllClaims(token)
        return claimsResolver(claims)
    }

    private fun generateAccessToken(userDetails: UserDetails): String {
        return generateToken(userDetails, securityProperties.jwt.accessTokenExpirationMs)
    }

    private fun generateRefreshToken(userDetails: UserDetails): String {
        return generateToken(userDetails, securityProperties.jwt.refreshTokenExpirationMs)
    }

    private fun generateToken(userDetails: UserDetails, expirationMs: Long): String {
        val claims = mutableMapOf<String, Any>()
        if (userDetails is User) {
            claims["roles"] = userDetails.roles
        }
        return Jwts.builder()
            .claims(claims)
            .subject(userDetails.username)
            .issuedAt(Date(System.currentTimeMillis()))
            .expiration(Date(System.currentTimeMillis() + expirationMs))
            .signWith(secretKey)
            .compact()
    }

    private fun isTokenExpired(token: String): Boolean = extractExpiration(token).before(Date())

    private fun extractExpiration(token: String): Date = extractClaim(token, Claims::getExpiration)

    private fun extractAllClaims(token: String): Claims =
        Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload
}
