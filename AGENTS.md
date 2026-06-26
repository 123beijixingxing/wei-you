# AGENTS.md

This file is for coding agents working in `C:\openCode-AI\wei-you`.

## Scope

- Primary app code lives under `weiyou-app/`.
- Frontend: `weiyou-app/weiyou-frontend`
- Backend: `weiyou-app/weiyou-backend`
- Product and architecture docs: `docs/`

## Repository Rules Files

- No root `AGENTS.md` existed before this file.
- No `.cursorrules` file was found.
- No `.cursor/rules/` directory was found.
- No `.github/copilot-instructions.md` file was found.
- If any of those files are added later, treat them as higher-priority repo guidance.

## High-Level Layout

- `weiyou-app/README.md`: top-level runbook for Docker Compose and smoke checks.
- `weiyou-app/docker-compose.yml`: local full-stack environment.
- `weiyou-app/scripts/smoke-check.ps1`: end-to-end verification script.
- `weiyou-app/weiyou-frontend/pages/`: uni-app pages.
- `weiyou-app/weiyou-frontend/stores/`: Pinia stores.
- `weiyou-app/weiyou-frontend/api/`: HTTP API wrappers.
- `weiyou-app/weiyou-backend/weiyou-boot/`: Spring Boot entry modules.
- `weiyou-app/weiyou-backend/weiyou-modules/`: business modules.
- `weiyou-app/weiyou-backend/weiyou-common/`: shared backend infrastructure.

## Build, Run, Test Commands

### Frontend

Run from `weiyou-app/weiyou-frontend`.

- Install deps:
  - `npm install --legacy-peer-deps`
- Start H5 dev server:
  - `npm run dev:h5`
- Build H5:
  - `npm run build:h5`
- Start WeChat Mini Program dev build:
  - `npm run dev:mp-weixin`
- Build WeChat Mini Program:
  - `npm run build:mp-weixin`

Notes:

- There is currently no dedicated frontend lint script.
- There is currently no dedicated frontend unit test script.
- Use `npm run build:h5` as the minimum regression check for frontend changes.

### Backend

Run from `weiyou-app/weiyou-backend`.

- Compile all modules without tests:
  - `mvn -q -DskipTests compile`
- Run all backend tests:
  - `mvn -q test`
- Package a specific boot app:
  - `mvn -q -DskipTests -pl weiyou-boot/weiyou-app-server -am package`
- Run app server locally:
  - `mvn -pl weiyou-boot/weiyou-app-server -am spring-boot:run`
- Run IM gateway locally:
  - `mvn -pl weiyou-boot/weiyou-im-gateway -am spring-boot:run`
- Run gateway locally:
  - `mvn -pl weiyou-boot/weiyou-gateway -am spring-boot:run`

### Single-Test Commands

Run from `weiyou-app/weiyou-backend`.

- Single test class in a module:
  - `mvn -q -pl weiyou-modules/weiyou-module-wallet -am -Dtest=WalletPersistenceServiceImplTest test`
- Single test method:
  - `mvn -q -pl weiyou-boot/weiyou-gateway -am -Dtest=GatewayRateLimitFilterTest#shouldReturnTooManyRequestsWhenLimitExceeded test`
- Another example for IM gateway:
  - `mvn -q -pl weiyou-boot/weiyou-im-gateway -am -Dtest=ChatWebSocketHandlerTest test`

Notes:

- Surefire uses `ClassName#methodName` syntax for a single test method.
- Use `-pl <module> -am` to avoid rebuilding the full multi-module tree.

### Docker Compose

Run from `weiyou-app`.

- Build and start everything:
  - `docker compose up --build`
- Validate compose file:
  - `docker compose config`

### Smoke Checks

Run from `weiyou-app`.

- Standard smoke run:
  - `powershell -ExecutionPolicy Bypass -File .\scripts\smoke-check.ps1`
- Include auth-negative checks:
  - `powershell -ExecutionPolicy Bypass -File .\scripts\smoke-check.ps1 -IncludeNegativeChecks`
- Include rate-limit checks:
  - `powershell -ExecutionPolicy Bypass -File .\scripts\smoke-check.ps1 -IncludeRateLimitChecks`

### PowerShell Script Syntax Validation

Run from `weiyou-app`.

- `powershell -Command "[void][scriptblock]::Create((Get-Content -Raw -LiteralPath '.\scripts\smoke-check.ps1')); 'PS1 OK'"`

## Backend Code Style

- Java version is 21.
- Use 4-space indentation.
- Use constructor injection; do not add field injection.
- Prefer Spring annotations and small controller methods.
- Keep business logic out of controllers; push it into app/domain services.
- Return `ApiResponse<T>` for normal controller responses.
- Return `PageResponse<T>` for paged list payloads.
- Use `BusinessException` for business failures that should map to user-facing API errors.
- Let `GlobalExceptionHandler` handle exception-to-response conversion.
- Do not return raw entities directly from controllers unless the module already follows that pattern.
- Use MyBatis Plus with `BaseMapper<T>` and `LambdaQueryWrapper`.
- Persistence entities extend `BaseEntity` when appropriate.
- Keep `@TableName(...)` explicit, including logical schema prefixes already used in this repo.
- Follow package conventions already in place:
  - `controller`
  - `app.service`
  - `domain.entity`
  - `infra.persistence.mapper`
- Use Java `record` for small request/response DTOs inside controllers where the codebase already does this.
- Avoid Lombok; current codebase does not depend on it.
- Avoid wildcard imports.
- Keep imports grouped and sorted by the formatter used by the IDE/Maven defaults.
- Use lower-case path segments in request mappings, matching current REST style.
- Preserve existing Chinese user-facing copy when editing messages and labels.

## Frontend Code Style

- Use Vue 3 with `script setup`.
- Use 2-space indentation in Vue, JS, and JSON files.
- Use double quotes and trailing semicolons in JS files.
- Prefer Composition API primitives: `ref`, `reactive`, `computed`.
- Use Pinia stores for cross-page state.
- Put network calls in `api/modules.js`, not directly in page components.
- Put auth/session helpers in `utils/session.js` and runtime URL logic in `utils/runtime-config.js`.
- Use `userStore.requireAuth()` before protected page actions and data fetches.
- Surface recoverable UI errors with `uni.showToast({ icon: "none" })`.
- Keep pages thin: fetch, map, and delegate to stores where possible.
- Use kebab-case for component filenames like `chat-item.vue` and `quick-entry.vue`.
- Keep page routes registered in `pages.json` whenever adding a new page.
- Use existing route/query parameter names such as `id`, `groupId`, `officialId`, `appId`, `conversationId`.
- Preserve the existing visual language and CSS variable usage from current pages.
- Prefer local page-scoped styles with `lang="scss"` and avoid global overrides unless necessary.

## Naming Conventions

- Java classes: PascalCase.
- Java methods/fields: camelCase.
- Java packages: lower-case.
- Vue components and page folders: lower-case or kebab-case.
- Pinia stores: singular nouns like `user`, `chat`, `app`.
- API wrappers: `<domain>Api` naming in `api/modules.js`.

## Error Handling Expectations

- Backend:
  - Throw `BusinessException` for expected business-rule failures.
  - Use meaningful messages because they are surfaced to the frontend.
  - Prefer returning existing state for idempotent operations when the module already does so.
- Frontend:
  - Catch async API failures in page actions.
  - Show user-friendly toast messages.
  - Let `request()` handle `401` by clearing session and redirecting to login.

## Agent Workflow Expectations

- Before changing API contracts, inspect both frontend callers and backend endpoints.
- If you change a route, update all of these when relevant:
  - frontend API wrapper
  - page navigation code
  - smoke-check script
  - README instructions
- If you touch backend logic, run at least targeted Maven tests or full `mvn -q test` when practical.
- If you touch frontend logic, run `npm run build:h5`.
- Do not edit `node_modules`, `dist`, or generated build artifacts except when verifying outputs.
- Prefer additive changes over large refactors; preserve the existing scaffold style.

## Good Default Validation Set

For typical feature work, run:

- Backend compile: `mvn -q -DskipTests compile`
- Backend tests: `mvn -q test`
- Frontend build: `npm run build:h5`
- Smoke script parse: `powershell -Command "[void][scriptblock]::Create((Get-Content -Raw -LiteralPath '.\scripts\smoke-check.ps1')); 'PS1 OK'"`

If Docker is available, also run:

- `docker compose config`
