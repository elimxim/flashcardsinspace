package com.github.elimxim.flashcardsinspace.entity.repository

import com.github.elimxim.flashcardsinspace.entity.VerificationIntent
import com.github.elimxim.flashcardsinspace.entity.VerificationType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.ZonedDateTime

@Repository
interface VerificationIntentRepository : JpaRepository<VerificationIntent, Long> {
    fun findByTokenHash(tokenHash: String): VerificationIntent?
    fun countByEmailAndTypeAndCreatedAtAfter(email: String, type: VerificationType, createdAt: ZonedDateTime): Long
    fun findByEmailAndType(email: String, type: VerificationType): List<VerificationIntent>
}

