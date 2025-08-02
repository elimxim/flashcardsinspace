package com.github.elimxim.flashcardsinspace

import com.github.elimxim.flashcardsinspace.security.JwtProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(JwtProperties::class)
class AppConfig
