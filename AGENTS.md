# AGENTS.md

## Implementation Process

For implementation work in this repository, follow this order:

1. read `docs/context/`
2. plan implementation and the test approach before changing code
3. write or update tests first when practical; prefer TDD for new behavior, bug fixes, and regressions
4. implement
5. update test fixtures, sample projects, snapshots, and UI test data meaningfully and reasonably when behavior changes
6. run formatting, inspections, unit/integration tests, and plugin verification
7. fix issues from inspections, tests, and verification
8. update `docs/context/`
9. update user-facing or developer-facing docs affected by the change
10. use your review workflow or review tool to review the code
11. fix all found issues
12. rerun formatting, inspections, tests, and verification
13. fix remaining issues
14. if needed, update docs again

Do not skip the documentation updates when implementation changes behavior,
capabilities, constraints, compatibility, threading model, settings, or UX.

Do not skip test work for implementation changes. Add or update automated tests at
the highest-value layer for the change, and keep fixtures focused on
representative, durable cases rather than incidental ones.

---

## Kotlin / JetBrains Plugin Quality Standard

Write Kotlin plugin code for readability, correctness, maintainability,
platform compatibility, responsiveness, and security.

Priority order:

1. readability
2. correctness
3. maintainability
4. compatibility
5. responsiveness
6. performance

Prefer explicit, idiomatic, production-grade Kotlin and IntelliJ Platform code.
Do not trade correctness, compatibility, or UX for cleverness or speculative optimization.

---

## Core Principles

- Prefer simple, idiomatic Kotlin over cleverness.
- Follow official Kotlin coding conventions and keep code easy to scan.
- Preserve IntelliJ Platform conventions instead of forcing generic app architecture onto plugin code.
- Prefer direct wiring over unnecessary abstraction.
- Avoid framework-like patterns, speculative layers, and custom infrastructure unless clearly justified.

---

## Project and Plugin Structure

- Keep packages small, focused, and easy to navigate.
- Keep plugin responsibilities clear: actions, services, settings, UI, indexing/PSI integration, and external integrations should not blur together.
- Keep `plugin.xml` and extension registrations minimal and explicit.
- Register only the extension points, listeners, services, and actions that are actually needed.
- Minimize startup cost and avoid loading functionality before it is needed.

---

## IntelliJ Platform API Usage

- Use stable, documented IntelliJ Platform APIs whenever possible.
- Avoid `@Internal`, deprecated-for-removal, experimental, or otherwise unstable APIs unless there is a strong, explicit reason.
- Keep platform dependencies minimal and explicit.
- Be deliberate about target IDEs, bundled plugins, and optional dependencies.
- Re-check compatibility assumptions when changing target platform versions.

---

## Kotlin Style

- Follow official Kotlin naming, formatting, and file organization conventions.
- Prefer `val` over `var`.
- Prefer immutable data flow and local reasoning.
- Use nullability deliberately; do not weaken types to avoid thinking.
- Use sealed types, enums, and data classes when they make states and transitions clearer.
- Make invalid states hard to represent.

---

## Classes and Functions

- Keep classes small, cohesive, and focused on one responsibility.
- Keep functions short, direct, and easy to scan.
- Prefer straightforward control flow and early returns.
- Do not extract helpers that make the call site harder to understand.
- Be explicit about lifecycle, ownership, and threading expectations.

---

## Services and State

- Use platform services only when they are the right lifecycle boundary.
- Do not turn everything into a service.
- Keep application-level state, project-level state, and ephemeral UI state clearly separated.
- Persist only what must survive restarts.
- Keep state schemas simple, stable, and easy to migrate.

---

## Threading, Read/Write Actions, and Coroutines

- Keep the UI responsive at all times.
- Do not block the EDT with long work, I/O, indexing work, or network calls.
- Use Kotlin coroutines in line with modern IntelliJ Platform guidance instead of ad hoc thread management when possible.
- Use explicit read/write action APIs when accessing PSI, VFS, or project model state that requires them.
- Keep read actions as small as possible.
- Prefer non-blocking or coroutine-friendly approaches for longer reads.
- Do not assume EDT code implicitly has the lock behavior you want; be explicit.
- Be explicit about cancellation, disposal, and lifecycle when launching background work.

---

## PSI, Documents, VFS, and Indexing

- Respect IntelliJ Platform rules around PSI, documents, VFS, dumb mode, and indexing.
- Keep PSI access scoped, valid, and cheap.
- Validate object validity before using PSI or project-related handles that may become stale.
- Avoid expensive PSI or index work on hot paths.
- Do not perform unnecessary reparsing, rescanning, or global work.
- Make background analysis cancellable where possible.

---

## Extension Points, Actions, and Listeners

- Keep actions focused and predictable.
- Put logic outside action classes when it improves clarity and testability.
- Register listeners, startup hooks, and extension points sparingly.
- Dispose and unregister correctly through platform lifecycles.
- Avoid hidden global behavior and surprising side effects.

---

## UI and UX

- Follow IntelliJ Platform UI conventions rather than inventing custom patterns.
- Prefer native platform components and recommended wrappers.
- Avoid modal dialogs unless they are clearly justified.
- Prefer non-modal notifications and inline validation where appropriate.
- Keep text concise, specific, and useful.
- Do not spam notifications, tool windows, popups, or editor decorations.
- Make the happy path obvious and failure states actionable.

---

## Settings and Persistence

- Use the platform persistence model for normal plugin settings.
- Keep settings small, explicit, and easy to reason about.
- Do not persist secrets in plain settings storage.
- Use the Credentials Store API for sensitive data.
- Keep migrations safe, explicit, and backward-aware when stored state evolves.

---

## Error Handling and Logging

- Handle failures explicitly.
- Surface actionable user-facing errors when users can do something about them.
- Keep logs structured, useful, and low-noise.
- Never log secrets, tokens, credentials, or sensitive file contents.
- Fail safely and preserve IDE stability.
- Prefer graceful degradation over crashing plugin behavior.

---

## Performance

- Protect startup time, typing latency, indexing speed, and editor responsiveness.
- Measure before optimizing.
- Avoid unnecessary allocations, scanning, PSI traversal, VFS churn, and repeated computation.
- Cache only when it clearly helps and invalidation is correct.
- Do not trade maintainability for hypothetical speedups.

---

## Security

- Treat filesystem, VCS, terminal, network, and external process input as untrusted.
- Validate and bound all external input.
- Use least privilege for external integrations.
- Store secrets only in approved secure storage.
- Do not execute destructive or surprising actions without clear user intent.

---

## Comments and Documentation

- Write comments only when they add signal.
- Explain why, invariants, lifecycle assumptions, threading expectations, or non-obvious tradeoffs.
- Keep KDoc and plugin docs accurate.
- Keep examples minimal and representative.

---

## Testing

- Test behavior, edge cases, regressions, disposal, and failure paths.
- Prefer deterministic tests.
- Use the highest-value test layer first.
- Use light tests when they are sufficient.
- Use heavy, integration, or UI tests when platform behavior, UI wiring, or IDE interaction is the real risk.
- Keep fixtures small and representative.
- Avoid brittle timing-based tests and hidden inter-test coupling.
- Cover compatibility-sensitive behavior with focused tests where practical.

---

## Compatibility and Build Tooling

- Use the modern IntelliJ Platform Gradle Plugin for supported platform versions.
- Keep plugin metadata, dependencies, and target IDE compatibility explicit.
- Run Plugin Verifier as part of the normal validation flow.
- Check against the IDE builds and product variants you claim to support.
- Watch for incompatible platform API changes when upgrading.
- Prefer official platform tooling and templates over custom build hacks.

---

## Dependency Management

- Prefer Kotlin stdlib, JDK, and IntelliJ Platform APIs first.
- Add third-party dependencies only when clearly justified.
- Be conservative with UI, concurrency, serialization, and HTTP dependencies inside plugins.
- Avoid dependencies that increase classpath risk, shading complexity, or compatibility burden without strong payoff.

---

## Change Guidelines

- Preserve good existing style.
- Prefer minimal, high-signal changes over broad rewrites.
- Improve naming, structure, lifecycle handling, and boundaries where needed.
- Call out tradeoffs for compatibility-sensitive, threading-sensitive, performance-sensitive, or security-sensitive changes.

---

## Review Checklist

Before finalizing a plugin change, verify:

- readability and idiomatic Kotlin style
- correctness on edge cases
- proper read/write action usage
- no EDT blocking
- correct coroutine, cancellation, and disposal handling
- safe PSI/VFS/index usage
- minimal and explicit extension/service registration
- stable compatibility assumptions
- useful and non-spammy UX
- secure settings and secret handling
- sufficient automated test coverage
- successful verification against supported IDE targets

---

## Hard Rules

- Prefer simple, explicit, idiomatic Kotlin.
- Keep the IDE responsive.
- Do not block the EDT.
- Use explicit read/write action APIs when required.
- Prefer coroutines over ad hoc threading where appropriate.
- Keep services, listeners, and extension registrations minimal.
- Use platform persistence correctly.
- Never store secrets in plain settings.
- Avoid unstable internal APIs unless explicitly justified.
- Run tests and Plugin Verifier before finalizing.
- Keep code maintainable, compatible, and safe.
