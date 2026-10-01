# CLAUDE.md

Guidance for Claude Code (claude.ai/code) when working in this repository.

> Update this file only for something essential: a new architectural pattern, a cross-cutting convention, or a non-obvious invariant that would otherwise cause bugs. Skip it for feature details, styling, tweaks, and anything the code already makes clear.

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

**Flashcard Stages**: S1 → … → S7 → OUTER_SPACE. Special stages: UNKNOWN (never reviewed), ATTEMPTED (sent back to S1).

**Chronodays**: Days in a set's timeline; the Lightspeed Schedule picks the stages reviewed on each. Statuses: INITIAL, NOT_STARTED, IN_PROGRESS, COMPLETED, OFF (suspended).

**Day Streak**: Consecutive learning days. OFF days don't break it; IN_PROGRESS days do.

**Review Sessions**: LIGHTSPEED, UNKNOWN, ATTEMPTED, OUTER_SPACE, QUIZ.

- Pages never call the session endpoints directly — each owns one `ReviewSessionAttendant` (`review-session-attendant.ts`): `create`/`loadOrCreate` on start, `track(id)` after a flashcard write succeeds, `flush()` to persist, `flush({ all: true })` to finish.
- A session is finished only by `finishReview()` (exit button, `onBeforeRouteLeave`, `onUnmounted`), and only once — the backend rejects a second finish with `SAF400`. `clear()` and `destroyReviewStore` go in `onUnmounted`, not the route guard.
- A flashcard PUT can piggyback a session flush (`sendFlashcardUpdateRequestWithinSession`, payload from `attendant.touch(options)`, which also does the local bookkeeping). The response is the flashcard with an optional `session` flattened in — strip `session` before storing the flashcard, or `copyFlashcard` replays it on the next PUT.
- Media (audio/pictures) is fetched only by the review store's `FlashcardMediaPrefetcher`.
- Reverse mode (front/back swapped) is per session type, saved in a cookie via `useReverseMode`, and reaches the review page only as the `?reversed=true` query param.

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
