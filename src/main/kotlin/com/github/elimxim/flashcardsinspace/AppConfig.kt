package com.github.elimxim.flashcardsinspace

import com.github.elimxim.flashcardsinspace.security.SecurityProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(SecurityProperties::class)
class AppConfig
