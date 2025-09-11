package com.github.elimxim.flashcardsinspace.security

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty

@ConfigurationProperties(prefix = "app.security")
data class SecurityProperties(
    val enabled: Boolean = true,
    @NestedConfigurationProperty
    val jwt: JwtProperties,
    @NestedConfigurationProperty
    val cors: CorsProperties = CorsProperties(),
)

data class JwtProperties(
    val secret: String,
    val accessTokenExpirationMs: Long,
    val refreshTokenExpirationMs: Long
)

data class CorsProperties(
    val allowedOrigins: List<String> = listOf(),
)
