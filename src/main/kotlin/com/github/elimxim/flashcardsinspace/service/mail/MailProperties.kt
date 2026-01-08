package com.github.elimxim.flashcardsinspace.service.mail

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.mail")
data class MailProperties(
    val apiKey: String,
    val senderName: String,
    val senderDomain: String,
    val replyToEmail: String,
)
