package com.github.elimxim.flashcardsinspace.entity.repository

import com.github.elimxim.flashcardsinspace.entity.ConfirmationCode
import com.github.elimxim.flashcardsinspace.entity.ConfirmationPurpose
import com.github.elimxim.flashcardsinspace.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ConfirmationCodeRepository : JpaRepository<ConfirmationCode, Long> {
    fun findByUserAndPurpose(user: User, purpose: ConfirmationPurpose): List<ConfirmationCode>
}

