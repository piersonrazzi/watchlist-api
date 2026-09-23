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
| — | Boot 4.1.1 as released | 3 | 3 | 0 | 0 | 0 | (Tomcat 11.0.24) |
| v1.0.0 | Boot 4.1.1 + Tomcat 11.0.26 | **0** | 0 | 0 | 0 | 0 | [scan-4](security/scan-4-boot-4.1.md) |

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

---

## Step 2: 3.5.16 → 4.1.1 (tag `v1.0.0`)

### Approach
Manual, compiler-driven. For the new starter layout, the project's **first commit** (a fresh
Boot 4.1.1 project from start.spring.io) served as the authoritative reference. For moved
classes, the new locations were read directly from the downloaded jars rather than guessed.

### Build changes
| Before (3.5) | After (4.1) | Why |
|---|---|---|
| `spring-boot-starter-web` | `spring-boot-starter-webmvc` | Boot 4 is modular; starters are per technology |
| `spring-boot-starter-test` | `spring-boot-starter-webmvc-test`, `-data-jpa-test`, `-validation-test` | Test support split per technology |
| (none) | `maven-dependency-plugin:properties` + Surefire `-javaagent` | Load Mockito as an agent (JDK 21 warns on self-attach, JEP 451) |
| (none) | `tomcat.version` 11.0.26 | Temporary: fixes newer than Boot 4.1.1. Has a written exit plan |

**Main application code compiled on Boot 4 with zero changes.** All breakage was in tests and config.

### Moved classes (tests)
| Old | New |
|---|---|
| `org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest` | `org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest` |
| `org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest` | `org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest` |
| `org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager` | `org.springframework.boot.jpa.test.autoconfigure.TestEntityManager` |
| `TestRestTemplate` + Jackson `JsonNode` | `RestTestClient` (Spring Framework 7) + `@AutoConfigureRestTestClient`, typed with our records |
| `com.fasterxml.jackson.databind.*` | `tools.jackson.databind.*` (Jackson 3); controller test now uses text blocks instead |

### The config-file failure
Six Spring-context tests failed with a deep chain ending in:
`No enum constant tools.jackson.databind.SerializationFeature.write-dates-as-timestamps`.
Jackson 3 moved that feature out of `SerializationFeature`, so the property
`spring.jackson.serialization.write-dates-as-timestamps=false` could no longer bind.
Jackson 3 already writes ISO-8601 dates by default, so the line was deleted, and tests
now assert the date format explicitly. **Migrations break config files, not just code.**

### Security result
- Boot 4.1.1 as released: 3 criticals, all in Tomcat 11.0.24, fixed in 11.0.25 (released after Boot 4.1.1).
- No Boot release contained the fix yet, so `tomcat.version` was pinned to 11.0.26 (same line,
  drop-in). Triage: the advisories affect Tomcat's DIGEST/FORM authentication and access control,
  which this app doesn't use, but the policy is zero known criticals.
- **0 known vulnerabilities** in 78 runtime components. CI gate green.
- Unlike the v0.2 overrides, this one has an **exit plan** in the POM comment: remove it when a
  Boot release manages Tomcat ≥ 11.0.25.

## Checklist for the next upgrade
1. Check support dates first (endoflife.date); target the newest supported line.
2. `mvnw clean` and re-verify tool inputs (SBOM path, CI artifact paths).
3. Search the POM for version overrides; remove any the new release makes obsolete.
4. Dry-run automation (OpenRewrite); commit its output separately from manual fixes.
5. Compile, then run *all* tests: runtime failures hide behind a green compile.
6. Read the last `Caused by`, including in config binding errors.
7. Rescan with the gate on; triage anything left; pin only with an exit plan.
