package com.github.elimxim.flashcardsinspace.security

import com.github.elimxim.flashcardsinspace.entity.repository.UserRepository
import com.github.elimxim.flashcardsinspace.web.filter.RequestLoggingFilter
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest
import org.springframework.boot.actuate.health.HealthEndpoint
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(SecurityProperties::class)
class SecurityConfig(
    private val env: Environment,
    private val securityProperties: SecurityProperties,
    private val requestLoggingFilter: RequestLoggingFilter,
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun userDetailsService(userRepository: UserRepository): UserDetailsService =
        UserDetailsService { email ->
            userRepository.findByEmail(email)
                .orElseThrow {
                    UsernameNotFoundException("User not found by email: $email")
                }
        }

    @Bean
    fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager =
        config.authenticationManager

    @Bean
    fun jwtAuthFilter(jwtService: JwtService, userDetailsService: UserDetailsService) =
        JwtAuthFilter(jwtService, userDetailsService)

    @Bean
    fun apiSecurityFilterChain(
        http: HttpSecurity,
        jwtAuthFilter: JwtAuthFilter,
    ): SecurityFilterChain =
        if (securityProperties.enabled) {
            // resolve HTTP -> HTTPS redirect loop when run in dev mode
            if (env.activeProfiles.none { it == "dev" }) {
                http.requiresChannel { channel ->
                    channel
                        .requestMatchers(EndpointRequest.to(HealthEndpoint::class.java)).requiresInsecure()
                        .anyRequest().requiresSecure()
                }
            }

            http
                .cors { it.configurationSource(corsConfigurationSource()) }
                .csrf { it.disable() }
                .logout { it.disable() }
                .authorizeHttpRequests { auth ->
                    auth
                        .requestMatchers(EndpointRequest.to(HealthEndpoint::class.java)).permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .requestMatchers("/api-public/**").permitAll()
                        .requestMatchers("/auth/**").permitAll()
                        .anyRequest().permitAll()
                }
                .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)
                .addFilterAfter(requestLoggingFilter, UsernamePasswordAuthenticationFilter::class.java)
                .headers { headers ->
                    headers
                        .xssProtection { xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK) }
                        .contentSecurityPolicy { csp -> csp.policyDirectives("script-src 'self'") }
                }
                .build()
        } else {
            http
                .cors { it.disable() }
                .csrf { it.disable() }
                .logout { it.disable() }
                .authorizeHttpRequests { auth ->
                    auth.anyRequest().permitAll()
                }
                .build()
        }

    private fun corsConfigurationSource() = UrlBasedCorsConfigurationSource().apply {
        val configuration = CorsConfiguration()
        configuration.allowedOrigins = securityProperties.cors.allowedOrigins
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
        configuration.allowedHeaders = listOf("*")
        configuration.allowCredentials = true
        registerCorsConfiguration("/**", configuration)
    }

}
