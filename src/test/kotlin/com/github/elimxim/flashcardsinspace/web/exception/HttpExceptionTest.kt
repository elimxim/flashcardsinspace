package com.github.elimxim.flashcardsinspace.web.exception

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class HttpExceptionTest {

    // @Transactional rolls back only on unchecked exceptions; a checked HttpException
    // would commit whatever a service wrote before throwing it
    @Test
    fun `should be unchecked so that transactions roll back on it`() {
        assertThat(RuntimeException::class.java.isAssignableFrom(HttpException::class.java)).isTrue()
    }
}
