package com.github.elimxim.flashcardsinspace.web

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component
import org.springframework.web.util.HtmlUtils
import java.util.concurrent.ConcurrentHashMap

private val log = LoggerFactory.getLogger(IndexPageRenderer::class.java)

/**
 * Serves the built index.html with route-specific head tags, so crawlers see the right
 * canonical URL and structured data without running JavaScript.
 */
@Component
class IndexPageRenderer(
    @param:Value("classpath:static/index.html")
    private val indexHtml: Resource,
) {
    private val template: String? by lazy {
        if (indexHtml.exists()) indexHtml.getContentAsString(Charsets.UTF_8) else null
    }

    private val rendered = ConcurrentHashMap<String, String>()

    fun render(requestPath: String): String? {
        val html = template ?: return null
        val path = requestPath.trimEnd('/')
        val head = pageHeads[path] ?: return html
        return rendered.computeIfAbsent(path) { applyHead(html, path, head) }
    }

    private fun applyHead(html: String, path: String, head: PageHead): String {
        val url = SITE_URL + path
        var result = html
            .replaceTag(LINK_CANONICAL, url)
            .replaceTag(META_OG_URL, url)
            .replaceTag(META_TWITTER_URL, url)

        head.title?.let {
            result = result
                .replaceTag(META_OG_TITLE, it)
                .replaceTag(META_TWITTER_TITLE, it)
        }

        head.description?.let {
            result = result
                .replaceTag(META_DESCRIPTION, it)
                .replaceTag(META_OG_DESCRIPTION, it)
                .replaceTag(META_TWITTER_DESCRIPTION, it)
        }

        head.structuredDataResource?.let {
            val json = ClassPathResource(it).getContentAsString(Charsets.UTF_8).trim()
            result = result.replaceFirst(
                "</head>",
                "<script type=\"application/ld+json\">\n$json\n</script>\n</head>",
            )
        }
        return result
    }

    private fun String.replaceTag(pattern: Regex, value: String): String {
        val match = pattern.find(this)
        if (match == null) {
            log.warn("index.html has no tag matching {}", pattern.pattern)
            return this
        }
        val group = match.groups[1]!!.range
        return replaceRange(group, HtmlUtils.htmlEscape(value))
    }

    private companion object {
        val LINK_CANONICAL = attr("link", "rel", "canonical", "href")
        val META_DESCRIPTION = attr("meta", "name", "description", "content")
        val META_OG_URL = attr("meta", "property", "og:url", "content")
        val META_OG_TITLE = attr("meta", "property", "og:title", "content")
        val META_OG_DESCRIPTION = attr("meta", "property", "og:description", "content")
        val META_TWITTER_URL = attr("meta", "property", "twitter:url", "content")
        val META_TWITTER_TITLE = attr("meta", "property", "twitter:title", "content")
        val META_TWITTER_DESCRIPTION = attr("meta", "property", "twitter:description", "content")

        /** Matches e.g. `<meta property="og:url" content="...">`, capturing the value. */
        fun attr(tag: String, key: String, keyValue: String, valueAttr: String) =
            Regex("""<$tag\s+$key="${Regex.escape(keyValue)}"\s+$valueAttr="([^"]*)"""")
    }
}
