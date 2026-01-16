package com.github.elimxim.flashcardsinspace.security

import com.github.elimxim.flashcardsinspace.entity.VerificationType
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty

@ConfigurationProperties(prefix = "app.security")
data class SecurityProperties(
    val enabled: Boolean = true,
    @NestedConfigurationProperty
    val jwt: JwtProperties,
    @NestedConfigurationProperty
    val cors: CorsProperties = CorsProperties(),
    @NestedConfigurationProperty
    val verificationTokens: VerificationTokenProperties,
)

data class JwtProperties(
    val secret: String,
    val accessTokenExpirationMs: Long,
    val refreshTokenExpirationMs: Long
)

data class CorsProperties(
    val allowedOrigins: List<String> = listOf(),
)

data class VerificationTokenProperties(
    @NestedConfigurationProperty
    val registrationRequest: SecurityTokenProperties,
    @NestedConfigurationProperty
    val passwordResetRequest: SecurityTokenProperties,
    @NestedConfigurationProperty
    val passwordResetAccess: SecurityTokenProperties,
)

data class SecurityTokenProperties(
    val length: Int,
    val maxAge: Int,
)

fun SecurityProperties.getSecurityTokenProperties(type: VerificationType): SecurityTokenProperties {
    return when (type) {
        VerificationType.REGISTRATION_REQUEST -> this.verificationTokens.registrationRequest
        VerificationType.PASSWORD_RESET_REQUEST -> this.verificationTokens.passwordResetRequest
        VerificationType.PASSWORD_RESET_ACCESS -> this.verificationTokens.passwordResetAccess
    }
}
