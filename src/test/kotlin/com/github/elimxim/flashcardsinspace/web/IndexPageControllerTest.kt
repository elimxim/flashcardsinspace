package com.github.elimxim.flashcardsinspace.web

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler
import java.io.File
import kotlin.test.Test

@SpringBootTest
@AutoConfigureMockMvc
class IndexPageControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `router paths are found`() {
        assertThat(routerPaths()).contains("/", "/website-guide")
    }

    @ParameterizedTest
    @MethodSource("routerPaths")
    fun `should serve the app page for every vue router path`(path: String) {
        mockMvc.get(path).andExpect {
            match(handler().handlerType(IndexPageController::class.java))
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["/unknown", "/home", "/website-guide/extra"])
    fun `should return 404 for paths the app does not have`(path: String) {
        mockMvc.get(path).andExpect {
            status { isNotFound() }
        }
    }

    companion object {
        @JvmStatic
        fun routerPaths(): List<String> =
            Regex("""path: '([^']+)'""")
                .findAll(File("src/main/vue/src/router/index.ts").readText())
                .map { it.groupValues[1] }
                .toList()
    }
}
