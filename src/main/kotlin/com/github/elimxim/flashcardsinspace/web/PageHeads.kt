package com.github.elimxim.flashcardsinspace.web

const val SITE_URL = "https://flashcardsinspace.com"

/**
 * Head tags that differ per public route.
 * [title] goes to og:title and twitter:title only.
 * [structuredDataResource] is a classpath JSON-LD file added to the page's <head>.
 */
data class PageHead(
    val title: String? = null,
    val description: String? = null,
    val structuredDataResource: String? = null,
)

/**
 * Public routes that get their own canonical URL in the HTML served to crawlers.
 * Routes not listed here (private pages, unknown paths) keep the homepage canonical
 * from index.html. Keep in sync with sitemap.xml.
 */
val pageHeads: Map<String, PageHead> = mapOf(
    "/get-help" to PageHead(
        title = "Get Help | Flashcards in Space",
        description = "Find answers and get help with Flashcards in Space.",
    ),
    "/emotional-support" to PageHead(
        title = "Emotional Support | Flashcards in Space",
        description = "Tips on memory and motivation from Melvin, " +
            "the emotional support possum of Flashcards in Space.",
    ),
    "/website-guide" to PageHead(
        title = "In-Depth Tutorial | Flashcards in Space",
        description = "A complete, step-by-step video guide to mastering Flashcards in Space " +
            "and how its Lightspeed Schedule works.",
        structuredDataResource = "seo/website-guide-video.json",
    ),
    "/signup" to PageHead(),
    "/login" to PageHead(),
    "/password-reset" to PageHead(),
    "/email-confirmation" to PageHead(),
    "/code-verification" to PageHead(),
)
