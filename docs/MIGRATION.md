# Migration Guide: Spring Boot 2.7 → 3.5 → 4.1

A record of what changed at each step, what broke, and why. Each step is a git tag.

## Support timeline (source: endoflife.date, checked 2026-09-23)

| Line | Final/latest release | Free support ended/ends | Paid support until |
|---|---|---|---|
| 2.7 | 2.7.18 | 2023-06-30 | 2029-06-30 |
| 3.5 | 3.5.16 | **2026-06-30** | 2032-06-30 |
| 4.0 | 4.0.8 | 2026-12-31 | — |
| 4.1 | 4.1.1 | 2027-07-31 | 2028-07-31 |

Takeaway: migration targets move. 3.5 was the natural "next step" from 2.7,
but it went end-of-life three months before this migration was done.

## Vulnerability trend (OSV.dev, runtime dependencies only)

| Tag | Stack | Total | Critical | High | Moderate | Low | Report |
|---|---|---|---|---|---|---|---|
| v0.1 | Boot 2.7.18 / Java 11 | 99 | 8 | 40 | 37 | 14 | [scan-1](security/scan-1-boot-2.7.md) |
| v0.2 | 2.7.18 + in-line patches | 54 | 1 | 18 | 26 | 9 | [scan-2](security/scan-2-boot-2.7-patched.md) |
| v0.3 | Boot 3.5.16 / Java 21 | 7 | 3 | 0 | 4 | 0 | [scan-3](security/scan-3-boot-3.5.md) |

---

## Step 1: 2.7.18 → 3.5.16 (tag `v0.3-boot3.5`)

### Approach
1. **Automated:** OpenRewrite recipe `org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_5`
   (rewrite-maven-plugin 6.46.1, rewrite-spring 6.37.1). Ran `dryRun` first, reviewed
   `target/rewrite/rewrite.patch`, then `run`. Committed the unedited output separately.
2. **Manual fixes** to make it work, committed separately.
3. **Modernization** once green, committed separately.

### What OpenRewrite did
- Parent `2.7.18 → 3.5.16`, `java.version 11 → 17`.
- `javax.persistence` / `javax.validation` / `javax.servlet` → `jakarta.*` in 7 files.
- Pattern-matching `instanceof` (Java 16).
- Removed our explicit CycloneDX plugin version (Boot 3.5 manages it).
- Added an explicit `jakarta.servlet-api` dependency (redundant; removed by hand).

### What it could not know
- **Our tactical-patch overrides** (`tomcat.version 9.0.121`, `spring-framework.version 5.3.39`,
  `logback.version 1.2.13`) stayed in the POM and pinned Spring 5 / Tomcat 9 underneath Boot 3.5.
- **Symptom:** the code *compiled*, unit tests passed, but every test that started a Spring
  context failed with `NoClassDefFoundError: org/springframework/test/context/aot/AotTestAttributes`
  (a Spring 6 class). Mixed major versions fail at runtime, not compile time.
- **Fix:** delete the overrides. Lesson: temporary patches need an exit plan, and a migration
  checklist should include "remove every version override."

### Manual changes
| Area | Before (2.7 / Java 11) | After (3.5 / Java 21) |
|---|---|---|
| Java | 11 | 21 (LTS) |
| DTOs | 4 classes with getters/setters (~250 lines) | 4 records (~40 lines); accessors are `name()` not `getName()` |
| Errors | Hand-built `Map` JSON | `ProblemDetail` (RFC 9457, `application/problem+json`) via `ResponseEntityExceptionHandler` |
| Error field names | `message`, `path` | `detail`, `instance` (+ `title`, `status`, `type`) — **API contract change for clients** |
| JPQL | `select distinct w ... join fetch` | `select w ... join fetch` (Hibernate 6 de-duplicates automatically) |
| Mocking | `@MockBean` (Boot, deprecated in 3.4) | `@MockitoBean` (Spring Framework 6.2+) |
| Streams | `Collectors.toList()` | `.toList()` |
| SBOM | Hand-configured, `target/bom.json` | Boot-managed, `target/classes/META-INF/sbom/application.cdx.json` (inside the jar) |

### Gotcha: the stale SBOM
Boot 3.3+ pre-configures the CycloneDX plugin and overrides its output path, so after the
upgrade the scanner kept reading the *old* `target/bom.json` and reported Boot 2.7 versions.
Fixes: adopt Boot's SBOM path, build with `clean`, and a staleness guard in `osv-scan.ps1`
that refuses an SBOM older than the jar.

### Result
- 15/15 tests pass. 99 → 7 vulnerabilities.
- But 3 new criticals: Tomcat 10.1.55 (shipped in 3.5.16) has advisories fixed in 10.1.58,
  released after 3.5 went end-of-life. No free 3.5 release will include them.
  **The CI gate is red at this tag by design.** Next step: 4.1.
