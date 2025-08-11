package com.github.elimxim.flashcardsinspace.security

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty

@ConfigurationProperties(prefix = "app.security")
data class SecurityProperties(
    @NestedConfigurationProperty
    val jwt: JwtProperties,
    @NestedConfigurationProperty
    val cors: CorsProperties,
    @NestedConfigurationProperty
    val ssl: SslProperties,
)

data class JwtProperties(
    val secret: String,
    val accessTokenExpirationMs: Long,
    val refreshTokenExpirationMs: Long
)

data class CorsProperties(
    val allowedOrigins: List<String>,
)

data class SslProperties(
    val keyStoreType: String,
    val keyStore: String,
    val keyAlias: String,
    val keyStorePassword: String,
)