# CLAUDE.md

Guidance for Claude Code (claude.ai/code) when working in this repository.

> After every code change, check whether this file needs updating to reflect new patterns, conventions, or architectural decisions introduced by the change. If it does, update it before considering the task complete.

## Project Overview

Flashcards in Space is a spaced repetition flashcard web app with a space theme. Users create flashcard sets with front/back sides, and the app generates a study schedule ("Lightspeed Schedule") that determines which flashcard stages to review on each day.

- **Backend**: Kotlin + Spring Boot 3.5, JPA/Hibernate, PostgreSQL, Liquibase. JDK 24, target JVM 23.
- **Frontend**: Vue 3 + TypeScript, Pinia, Vue Router, Vite, Axios. No UI framework — components are built from scratch.
- **Auth**: stateless JWT, email verification via Brevo.
- **Build**: Gradle with node-gradle plugin; the frontend builds into `build/resources/main/static/`.

## Commands

```bash
./gradlew build          # frontend + backend
./gradlew compileKotlin  # backend only
./gradlew test           # backend JUnit + frontend Vitest
./gradlew test --tests "com.github.elimxim.flashcardsinspace.service.LightspeedServiceTest"

# frontend via Gradle: npmRunBuild, npmRunTest, npmRunLint, npmRunTypeCheck
# frontend directly (from src/main/vue/): npm run dev|build|test|lint|lint-fix|type-check
```

## Architecture

**Backend** (`src/main/kotlin/com/github/elimxim/flashcardsinspace/`) — standard Spring layering: `entity/`, `service/`, `web/` (with `dto/` and `exception/`, where errors carry `ApiErrorCode` enums), `security/` (`JwtService`, `JwtAuthFilter`, `SecurityConfig`, `VerificationCodeService`), and `schedule/`, which holds `LightspeedSchedule` — the spaced repetition algorithm deciding which stages are reviewed on a given day.

URL patterns: `/api/**` authenticated, `/api-public/**` public, `/auth/**` auth. `ForwardController` forwards all non-API routes to `index.html` for Vue Router.

**Frontend** (`src/main/vue/src/`) — `pages/`, `components/`, `modals/`, `stores/` (Pinia), `model/`, `utils/`, plus two directories worth knowing:
- `core-logic/` — pure business logic, unit-tested with Vitest: `stage-logic.ts`, `review-logic.ts`, `chrono-logic.ts`, `review-session-attendant.ts`, `flashcard-media-prefetch.ts`, and `flashcard-audio-logic.ts` / `flashcard-picture-logic.ts` (fetch/upload/remove).
- `api/` — Axios clients: `api-client.ts` (authenticated), `auth-client.ts`, `public-api-client.ts`. `token-refresh.ts` handles automatic JWT refresh.

## Key Domain Concepts

**Flashcard Stages**: S1 → S2 → S3 → S4 → S5 → S6 → S7 → OUTER_SPACE. Special stages: UNKNOWN (new, never reviewed), ATTEMPTED (reviewed but sent back to S1).

**Chronodays**: Each day in a flashcard set's timeline. The Lightspeed Schedule determines which stages are reviewed on each chronoday. Statuses: INITIAL, NOT_STARTED, IN_PROGRESS, COMPLETED, OFF (suspended).

**Review Sessions**: LIGHTSPEED (normal schedule-based), UNKNOWN, ATTEMPTED, OUTER_SPACE (special stage reviews), QUIZ.

**Flashcard media (audio/pictures)**: During a review session the `FlashcardMediaPrefetcher` (`flashcard-media-prefetch.ts`, one instance per review store) is the *only* thing that fetches media.

**Review session lifecycle**: Review pages never call the review-session endpoints directly. Each page owns one `ReviewSessionAttendant` (`review-session-attendant.ts`, built by `createReviewSessionAttendant`) holding the session, its stopwatch, and the ids of the flashcards reviewed so far. Pages `create`/`loadOrCreate` a session on start, `track(flashcardId)` *after* a flashcard write succeeds, `flush()` to persist progress, and `flush({ all: true })` to finish. A session is finished only by `finishReview()` — the exit button, `onBeforeRouteLeave`, or `onUnmounted` — never mid-review; the attendant refuses to finish twice, since the backend rejects that with `SAF400`. `clear()` and `destroyReviewStore` belong in `onUnmounted`, not the route guard, so a cancelled navigation cannot tear down a live page. Quiz rounds chain through child sessions: `createChildReviewSession` closes the parent server-side. `loadOrCreate` resumes a stored session only if `canBeOnboarded` accepts it — for a quiz, one whose metadata still shows unanswered cards; for any other type, one that was never finished.

**Piggybacked session updates**: `PUT /flashcard-sets/{setId}/flashcards/{id}?sessionId=` optionally carries a `sessionRequest` block, so a flashcard write and a session flush cost one round trip. The session id travels as a query param, never in the body. The response is `FlashcardUpdateResponse` — the `FlashcardDto` `@JsonUnwrapped` at the top level plus an optional `session`, omitted when absent. Two client functions rather than one overloaded one: `sendFlashcardUpdateRequest` for a plain write, `sendFlashcardUpdateRequestWithinSession` for the combined one. The page never builds the session payload itself — `ReviewSessionAttendant.touch(options)` returns what `flush()` would have sent *and* applies the same local bookkeeping (`all: true` sets `finished` and stops the stopwatch), which is what keeps `finishReview()` from finishing a session the flashcard PUT already closed and getting `SAF400`. Since the response is flat, strip `session` (`const { session, ...flashcard } = response.data`) before handing the flashcard to the store *or* to `currFlashcard` — `copyFlashcard` is a deep JSON copy and would otherwise replay a stale session block on the next PUT.

**Day Streak**: Consecutive learning days. OFF days do not break the streak; IN_PROGRESS days do.

## Backend Notes

- **Database**: PostgreSQL, schema `flashcardsinspace`, `ddl-auto: validate`. Liquibase changesets in `src/main/resources/db/changelog/changeset/`.
- **Timezone**: UTC enforced at app startup; user-facing conversion happens at the presentation layer only.
- **Caching**: Caffeine in-memory cache (`CacheConfig.kt`).
- **Input security**: OWASP HTML sanitizer for user-supplied content (`UserInputUtils`).
- **Virtual threads**: enabled.

## Testing

**Backend**: JUnit 5 + AssertJ + MockK (`io.mockk:mockk`). `spring-boot-starter-test` also provides `@MockBean` (Mockito) + `org.mockito.kotlin` for Spring context tests.

- Pure unit tests (services, validators): instantiate the class directly, no Spring context. Use MockK.
- Controller tests (`@WebMvcTest`): `@MockBean` the service under test plus `JwtService` and `UserRepository` — `SecurityConfig` defines `@Bean` methods that inject the latter two even when `app.security.enabled=false`. Set the principal with `SecurityMockMvcRequestPostProcessors.user(mockUser)`.
- To disable the JWT filter chain in `@WebMvcTest`, set `app.security.enabled=false` via `@TestPropertySource`. `SecurityProperties` binding still requires all `app.security.*` sub-properties (jwt, verification-tokens) to be present.

**Frontend**: Vitest, pure unit tests only — no HTTP mocking library in use.

## Frontend Code Style

- **No semicolons** in `.ts` and `.vue` files — ESLint forbids it.
- **No `any` type** — ESLint forbids it. Use explicit types, generics, or a named alias with a targeted `as` cast where a heterogeneous collection forces it.
- **Globally registered components** (registered via `app.component(...)` in `main.ts`) must be declared in `env.d.ts` under `declare module 'vue' { interface GlobalComponents { ... } }`. Locally imported SFCs need nothing — the import is the registration. Without the declaration neither `vue-tsc` nor the IDE type-checks the tag's props.

## Dev Environment

Vite dev server (port 5174) proxies `/api`, `/api-public`, `/auth`, and `/actuator` to the backend on port 8442.

Runtime config lives in `props/` (sibling to `src/`), passed via `--spring.config.additional-location=file:props/`: `application.yaml` (production, env-var placeholders), `application-dev.yaml` (`dev` profile overrides), and `postgresql.conf`. The release workflow copies only `application.yaml` and does not activate the `dev` profile, so `application-dev.yaml` is safe for local-only settings.
