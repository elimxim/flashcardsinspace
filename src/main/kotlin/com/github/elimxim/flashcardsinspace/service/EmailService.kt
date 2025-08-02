package com.github.elimxim.flashcardsinspace.service

import com.github.elimxim.flashcardsinspace.entity.User
import jakarta.mail.MessagingException
import org.springframework.core.io.ClassPathResource
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context

//@Service
class EmailService(
    private val mailSender: JavaMailSender,
    private val templateEngine: TemplateEngine
) {

    // todo more logs
    // todo logo
    fun sendWelcomeEmail(user: User) {
        return
        // fixme
        val mimeMessage = mailSender.createMimeMessage()
        try {
            val helper = MimeMessageHelper(mimeMessage, true, "UTF-8")
            helper.setTo(user.email)
            helper.setSubject("Welcome to Flashcards from Space!")

            val context = Context()
            context.setVariable("name", user.getUsername())

            val htmlContent = templateEngine.process("welcome-email", context)
            helper.setText(htmlContent, true)

            helper.addInline("logo", ClassPathResource("static/images/logo.svg"))

            mailSender.send(mimeMessage)
        } catch (e: MessagingException) {
            // todo log and handle (retry?)
        }
    }
}
