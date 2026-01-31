package com.github.elimxim.flashcardsinspace

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty

@ConfigurationProperties(prefix = "app.dev")
data class DevProperties(
    @NestedConfigurationProperty
    val responseLatency: ResponseLatencyProperties = ResponseLatencyProperties()
)

data class ResponseLatencyProperties(
    val enabled: Boolean = false,
    val delayMs: Long = 0,
)
