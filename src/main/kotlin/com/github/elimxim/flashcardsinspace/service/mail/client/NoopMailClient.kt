package com.github.elimxim.flashcardsinspace.service.mail.client

import com.github.elimxim.flashcardsinspace.security.maskSecret
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.stereotype.Service

private val log = LoggerFactory.getLogger(NoopMailClient::class.java)

@Service
@ConditionalOnMissingBean(BrevoMailClient::class)
class NoopMailClient: MailClient {
    override fun send(recipient: Recipient, mail: Mail) {
        log.info("Mail sending is disabled, skipping mail to {} (subject={})", maskSecret(recipient.email), mail.subject)
    }
}
