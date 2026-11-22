package com.github.elimxim.flashcardsinspace.web

import org.assertj.core.api.Assertions.assertThat
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.FileSystemResource
import java.io.File
import kotlin.test.Test

class IndexPageRendererTest {
    private val renderer = IndexPageRenderer(FileSystemResource(File("src/main/vue/index.html")))
    private val source = File("src/main/vue/index.html").readText()

    @Test
    fun `website guide gets its own canonical, description and video structured data`() {
        val html = renderer.render("/website-guide")!!

        assertThat(html)
            .contains("""<link rel="canonical" href="https://flashcardsinspace.com/website-guide">""")
            .contains("""<meta property="og:url" content="https://flashcardsinspace.com/website-guide">""")
            .contains("""<meta property="twitter:url" content="https://flashcardsinspace.com/website-guide">""")
            .contains("""content="In-Depth Tutorial | Flashcards in Space"""")
            .contains("""content="A complete, step-by-step video guide""")
            .contains(""""@type": "VideoObject"""")
            .contains("<title>In-Depth Tutorial | Flashcards in Space</title>")
            .contains("""<meta name="robots" content="index, follow">""")
            .doesNotContain("""href="https://flashcardsinspace.com/">""")
        assertThat(html.indexOf("VideoObject")).isLessThan(html.indexOf("</head>"))
    }

    @Test
    fun `trailing slash resolves to the same page`() {
        assertThat(renderer.render("/website-guide/")).isEqualTo(renderer.render("/website-guide"))
    }

    @Test
    fun `summary goes into the app div before the spinner`() {
        val html = renderer.render("/emotional-support")!!

        assertThat(html).contains(
            """<div id="app">
    <div class="seo-summary"><h1>Emotional Support</h1><p>Hi, my name is Melvin.""",
        )
        assertThat(html.indexOf("seo-summary\">")).isLessThan(html.indexOf("id=\"app-spinner\""))
    }

    @Test
    fun `page without description keeps the default one and is noindex`() {
        val html = renderer.render("/login")!!

        assertThat(html)
            .contains("<title>Log In | Flashcards in Space</title>")
            .contains("""<meta name="robots" content="noindex, follow">""")
            .doesNotContain("seo-summary\">")
            .contains("""<link rel="canonical" href="https://flashcardsinspace.com/login">""")
            .contains("A spaced-repetition system for people who want to remember what they learn")
            .doesNotContain("VideoObject")
    }

    @Test
    fun `unknown path keeps index html untouched`() {
        assertThat(renderer.render("/control-panel")).isEqualTo(source)
    }

    @Test
    fun `sitemap lists exactly the indexable pages`() {
        val sitemap = Regex("<loc>$SITE_URL(/[^<]*)</loc>")
            .findAll(File("src/main/vue/public/sitemap.xml").readText())
            .map { it.groupValues[1] }
            .toSet()
        val indexable = pageHeads.filterValues { !it.noindex }.keys + "/"

        assertThat(sitemap).isEqualTo(indexable)
    }

    @Test
    fun `titles match the vue router`() {
        val router = File("src/main/vue/src/router/index.ts").readText()

        pageHeads.forEach { (path, head) ->
            val route = Regex("path: '${Regex.escape(path)}'.*?title: '([^']+)'", RegexOption.DOT_MATCHES_ALL)
                .find(router)!!.groupValues[1]
            assertThat(head.title).isEqualTo("$route | Flashcards in Space")
        }
    }

    @Test
    fun `missing index html renders nothing`() {
        val missing = object : ByteArrayResource(ByteArray(0)) {
            override fun exists() = false
        }

        assertThat(IndexPageRenderer(missing).render("/website-guide")).isNull()
    }
}
