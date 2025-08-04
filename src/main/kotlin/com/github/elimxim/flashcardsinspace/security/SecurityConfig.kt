package com.github.elimxim.flashcardsinspace.security

import com.github.elimxim.flashcardsinspace.entity.repository.UserRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
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
class SecurityConfig {

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
    ): SecurityFilterChain = http
        .requiresChannel { channel ->
            channel.anyRequest().requiresSecure()
        }
        .cors { it.configurationSource(corsConfigurationSource()) }
        .csrf { it.disable() }
        .authorizeHttpRequests { auth ->
            auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/users/**").authenticated()
                .requestMatchers("/api/flashcard-sets/**").authenticated()
                .anyRequest().authenticated()
        }
        .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)
        .oauth2ResourceServer { oauth2 -> oauth2.jwt {} }
        .headers { headers ->
            headers
                .xssProtection { xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK) }
                .contentSecurityPolicy { csp -> csp.policyDirectives("script-src 'self'") }
        }
        .build()

    @Bean
    fun corsConfigurationSource() = UrlBasedCorsConfigurationSource().apply {
        val configuration = CorsConfiguration()
        configuration.allowedOrigins = listOf("https://localhost:5176")
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
        configuration.allowedHeaders = listOf("*")
        configuration.allowCredentials = true
        registerCorsConfiguration("/**", configuration)
    }

}
