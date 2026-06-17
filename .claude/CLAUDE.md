# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

> After every code change, check whether this file needs to be updated to reflect new patterns, conventions, or architectural decisions introduced by the change. If it does, update it before considering the task complete.

## Project Overview

Flashcards in Space is a spaced repetition flashcard web application with a space theme. Users create flashcard sets with front/back sides, and the app generates a study schedule ("Lightspeed Schedule") that determines which flashcard stages to review on each day.

## Tech Stack

- **Backend**: Kotlin + Spring Boot 3.5, JPA/Hibernate, PostgreSQL, Liquibase migrations
- **Frontend**: Vue 3 + TypeScript, Pinia stores, Vue Router, Vite, Axios
- **Auth**: JWT-based (stateless sessions), email verification via Brevo
- **Build**: Gradle with node-gradle plugin (frontend built into `build/resources/main/static/`)
- **Java**: JDK 24 (target JVM 23)

## Build & Dev Commands

```bash
# Full build (compiles Vue frontend + Kotlin backend)
./gradlew build

# Backend only (skips frontend)
./gradlew compileKotlin

# Run all tests (backend JUnit + frontend Vitest)
./gradlew test

# Run a single backend test class
./gradlew test --tests "com.github.elimxim.flashcardsinspace.service.LightspeedServiceTest"

# Frontend commands (run from src/main/vue/)
npm run dev          # Vite dev server on port 5174
npm run build        # Type-check + Vite production build
npm run test         # Vitest (watch mode)
npm run lint         # ESLint
npm run type-check   # vue-tsc

# Gradle wrappers for frontend
./gradlew npmRunBuild
./gradlew npmRunTest
./gradlew npmRunLint
./gradlew npmRunTypeCheck
```

## Architecture

### Backend (src/main/kotlin/com/github/elimxim/flashcardsinspace/)

Standard Spring Boot layered architecture:
- `entity/` — JPA entities and repositories. Core entities: `User`, `FlashcardSet`, `Flashcard`, `Chronoday`, `DayStreak`, `ReviewSession`, `FlashcardAudio`
- `service/` — Business logic. Key services: `FlashcardSetService`, `FlashcardService`, `ChronoService`, `LightspeedService`, `ReviewSessionService`, `DayStreakService`
- `web/` — REST controllers. `dto/` has DTOs, `exception/` has error handling with `ApiErrorCode` enums
- `security/` — JWT auth (`JwtService`, `JwtAuthFilter`), `SecurityConfig`, `AuthController`, email verification (`VerificationCodeService`)
- `schedule/` — `LightspeedSchedule` — the spaced repetition algorithm that generates which stages to review on each day

**API URL patterns**:
- `/api/**` — authenticated endpoints
- `/api-public/**` — public endpoints
- `/auth/**` — authentication endpoints

**ForwardController** forwards all non-API routes to `index.html` for Vue Router SPA handling.

### Frontend (src/main/vue/src/)

- `pages/` — Route-level page components (ControlPanel, UserPage, auth pages)
- `components/` — Reusable components built from scratch (no UI framework). `control-panel/` contains the main dashboard widgets; `review/` contains the review session UI (SpaceDeck, ReviewRouter, ReviewResult)
- `modals/` — Modal dialog components used across pages and features (confirmations, forms, and focused workflows)
- `stores/` — Pinia stores (flashcard-store, chrono-store, review-store, auth-store, audio-store, etc.)
- `core-logic/` — Pure business logic with Vitest unit tests: `stage-logic.ts` (stage progression S1-S7 + OUTER_SPACE), `review-logic.ts` (review queue algorithms), `chrono-logic.ts`
- `api/` — Axios API client (`api-client.ts` for authenticated, `auth-client.ts` for auth, `public-api-client.ts` for public). `token-refresh.ts` handles automatic JWT refresh.
- `model/` — TypeScript model types
- `utils/` — Shared utilities


### Key Domain Concepts

**Flashcard Stages**: S1 → S2 → S3 → S4 → S5 → S6 → S7 → OUTER_SPACE. Special stages: UNKNOWN (new, never reviewed), ATTEMPTED (reviewed but sent back to S1).

**Chronodays**: Each day in a flashcard set's timeline. The Lightspeed Schedule determines which stages are reviewed on each chronoday based on spaced repetition intervals. Statuses: INITIAL, NOT_STARTED, IN_PROGRESS, COMPLETED, OFF (suspended).

**Review Sessions**: Types include LIGHTSPEED (normal schedule-based), UNKNOWN, ATTEMPTED, OUTER_SPACE (special stage reviews), and QUIZ.

**Day Streak**: Tracks consecutive learning days. Off days (OFF status) do not break the streak; IN_PROGRESS days do break it.

### Database

PostgreSQL with Liquibase. Schema: `flashcardsinspace`. Migrations in `src/main/resources/db/changelog/changeset/`. DDL validation mode (`ddl-auto: validate`). HikariCP pool: 4 max connections, 2 idle minimum.

### Additional Backend Notes

- **Timezone**: UTC enforced at app startup; user-facing timezone conversion happens at the presentation layer only.
- **Caching**: Caffeine in-memory cache (`CacheConfig.kt`).
- **Input security**: OWASP HTML sanitizer used for user-supplied content (`UserInputUtils`).
- **Virtual threads**: Enabled (Project Loom / JDK 21+).

### Testing

**Backend**: JUnit 5 + AssertJ + MockK (`io.mockk:mockk`) for pure unit tests. `spring-boot-starter-test` (already included) provides `@MockBean` (Mockito) + `org.mockito.kotlin` extensions for Spring context tests.

- Pure unit tests (services, validators): instantiate the class directly, no Spring context needed. Use MockK.
- Controller tests (`@WebMvcTest`): use `@MockBean` for `FlashcardService`, `JwtService`, and `UserRepository` (the latter two because `SecurityConfig` defines `@Bean` methods that inject them even when `app.security.enabled=false`). Set the authenticated principal with `SecurityMockMvcRequestPostProcessors.user(mockUser)`.
- To disable the JWT security filter chain in `@WebMvcTest`, set `app.security.enabled=false` via `@TestPropertySource`. `SecurityProperties` binding still requires all `app.security.*` sub-properties (jwt, verification-tokens) to be present.

**Frontend**: Vitest. Pure unit tests only — no HTTP mocking library currently in use.

### Frontend Code Style

- **No semicolons** in `.ts` and `.vue` files — ESLint forbids it.
- **No `any` type** in `.ts` and `.vue` files — ESLint forbids it. Use explicit types, generics, or a named alias with a targeted `as` cast where a heterogeneous collection forces it.

### Dev Environment

Vite dev server (port 5174) proxies `/api`, `/api-public`, and `/auth` to backend on port 8442. The `dev` Spring profile disables HTTPS redirect and enables Hawtio/Jolokia monitoring endpoints. Runtime config lives in `props/` (sibling to `src/`): `application.yaml`, `postgresql.conf`, and a `dev/` subdirectory for dev-specific overrides.
