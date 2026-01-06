package com.github.elimxim.flashcardsinspace.entity.repository

import com.github.elimxim.flashcardsinspace.entity.ConfirmationCode
import com.github.elimxim.flashcardsinspace.entity.ConfirmationPurpose
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ConfirmationCodeRepository : JpaRepository<ConfirmationCode, Long> {
    fun findByEmailAndPurpose(email: String, purpose: ConfirmationPurpose): List<ConfirmationCode>
}

