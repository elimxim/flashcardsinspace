package com.github.elimxim.flashcardsinspace.web

const val SITE_URL = "https://flashcardsinspace.com"

/**
 * Head tags that differ per public route.
 * [title] goes to <title>, og:title and twitter:title.
 * [summary] is plain text placed in #app as an <h1> and paragraphs, so crawlers see
 * page-specific content before JavaScript runs; Vue replaces it on mount.
 * [noindex] marks pages that should stay out of search results (keep them out of sitemap.xml).
 * [structuredDataResource] is a classpath JSON-LD file added to the page's <head>.
 */
data class PageHead(
    val title: String,
    val description: String? = null,
    val summary: List<String> = emptyList(),
    val noindex: Boolean = false,
    val structuredDataResource: String? = null,
)

/**
 * Public routes that get their own canonical URL in the HTML served to crawlers.
 * Routes not listed here (private pages, unknown paths) keep index.html as is.
 * Indexable entries must be in sitemap.xml; titles must match `meta.title` in the Vue router.
 */
val pageHeads: Map<String, PageHead> = mapOf(
    "/get-help" to PageHead(
        title = "Get Help | Flashcards in Space",
        description = "Find answers and get help with Flashcards in Space.",
        summary = listOf(
            "Get Help",
            "Need a hand with Flashcards in Space? Watch the in-depth website guide to learn " +
                "how flashcard sets, stages and the Lightspeed Schedule work, or visit Melvin, " +
                "our emotional support possum, for tips on memory and motivation.",
        ),
    ),
    "/emotional-support" to PageHead(
        title = "Emotional Support | Flashcards in Space",
        description = "Tips on memory and motivation from Melvin, " +
            "the emotional support possum of Flashcards in Space.",
        summary = listOf(
            "Emotional Support",
            "Hi, my name is Melvin. I am an emotional support possum. Don't panic! I'm here to help you.",
            "Browse tips on improving your memory, staying motivated with spaced repetition, " +
                "and what to do when your flashcard reviews feel overwhelming.",
        ),
    ),
    "/website-guide" to PageHead(
        title = "In-Depth Tutorial | Flashcards in Space",
        description = "A complete, step-by-step video guide to mastering Flashcards in Space " +
            "and how its Lightspeed Schedule works.",
        summary = listOf(
            "In-Depth Tutorial",
            "A complete, step-by-step video guide to mastering Flashcards in Space. Learn how to " +
                "get the most out of your Flashcards in Space experience.",
            "The Lightspeed Schedule moves each flashcard through stages S1 to S7 and on to " +
                "outer space, deciding which stages you review on each day so you remember " +
                "what you learn for the long term.",
        ),
        structuredDataResource = "seo/website-guide-video.json",
    ),
    "/signup" to PageHead(title = "Sign Up | Flashcards in Space", noindex = true),
    "/login" to PageHead(title = "Log In | Flashcards in Space", noindex = true),
    "/password-reset" to PageHead(title = "Reset Password | Flashcards in Space", noindex = true),
    "/email-confirmation" to PageHead(title = "Confirm Email | Flashcards in Space", noindex = true),
    "/code-verification" to PageHead(title = "Verify Code | Flashcards in Space", noindex = true),
)
