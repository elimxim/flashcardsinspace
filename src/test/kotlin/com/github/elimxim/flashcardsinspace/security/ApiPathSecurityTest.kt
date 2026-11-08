package com.github.elimxim.flashcardsinspace.security

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@SpringBootTest(properties = ["app.security.enabled=true"])
@AutoConfigureMockMvc
class ApiPathSecurityTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @ParameterizedTest
    @ValueSource(strings = ["/api/flashcard-sets", "/API/flashcard-sets", "/Api/Flashcard-Sets"])
    fun `should reject unauthenticated api requests regardless of path case`(path: String) {
        mockMvc.get(path).andExpect {
            status { isForbidden() }
        }
    }
}
