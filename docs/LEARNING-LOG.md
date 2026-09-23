# Learning Log

Study notes written while building this project. Each phase lists what was built,
the concepts behind it, and questions an interviewer might ask.

---

## Phase 1 — Legacy baseline (Spring Boot 2.7.18, Java 11)

**Goal:** a realistic REST API on an end-of-life stack, as the starting point for a migration.

### Maven essentials
- **Coordinates** `groupId:artifactId:version` uniquely identify an artifact. `-SNAPSHOT` = unreleased.
- **Parent POM** (`spring-boot-starter-parent`) manages versions of hundreds of libraries
  ("dependency management"), so dependencies in our POM have no `<version>`. Upgrading Boot is
  mostly one line, which is also why one stale line can leave dozens of libraries unpatched.
- **Scopes:** default `compile` (always), `runtime` (H2: needed to run, not to compile),
  `test` (never ships in the jar).
- **Lifecycle:** `validate → compile → test → package → verify → install → deploy`.
  Running a phase runs every earlier phase.
- **Maven Wrapper** (`mvnw`, `.mvn/wrapper/`) pins the Maven version per repo. No global install.
- **Profiles** switch config on/off by condition (OS, JDK, property). We use one to apply a
  Windows-only JVM flag (see "Debugging" below).

### Architecture: controller → service → repository
| Layer | Class | Responsibility |
|---|---|---|
| Web | `WatchlistController` | HTTP mapping, `@Valid`, status codes. No business logic. |
| Web | `ApiExceptionHandler` | One place that turns exceptions into JSON errors. |
| Service | `WatchlistService` | Business rules + transaction boundaries (`@Transactional`). |
| Repository | `WatchlistRepository` | Spring Data interface; implementation generated at startup. |
| Domain | `Watchlist`, `WatchlistItem` | JPA entities mapped to tables. |
| DTOs | `*Request`, `*Response` | The API contract, decoupled from the table layout. |

### JPA / Hibernate concepts used
- **Bidirectional one-to-many** with `mappedBy`; helper methods (`addItem`/`removeItem`) keep both sides in sync.
- **Cascade + orphanRemoval:** saving a watchlist saves its items; removing an item from the list deletes its row.
- **LAZY vs EAGER:** `@ManyToOne(fetch = LAZY)` avoids loading data you don't need.
- **N+1 problem:** listing N watchlists and then touching each one's items runs N extra queries.
  Fixed with `left join fetch` in a JPQL `@Query`. `distinct` prevents duplicate parents in Hibernate 5.
- **`@Enumerated(STRING)`** so reordering the enum never corrupts data.
- **`BigDecimal` for money**, compared with `compareTo` (150 vs 150.00), never `equals` or `double`.
- **Open-in-view disabled:** all DB work happens inside service transactions, and entities are
  mapped to DTOs before the transaction ends (otherwise `LazyInitializationException`).

### Validation
- Field rules as annotations on DTOs (`@NotBlank`, `@Pattern`, `@Positive`, `@FutureOrPresent`), triggered by `@Valid`.
- Cross-field rules (options need strike + expiration) in the service, because annotations see one field at a time.

### Testing pyramid (15 tests)
| Type | Class | Spring context? | Speed |
|---|---|---|---|
| Unit | `WatchlistServiceTest` | None (Mockito) | ms |
| Web slice | `WatchlistControllerTest` (`@WebMvcTest` + `@MockBean`) | MVC only | fast |
| JPA slice | `WatchlistRepositoryTest` (`@DataJpaTest`) | JPA + H2 only | fast |
| End-to-end | `WatchlistApiIntegrationTest` (`@SpringBootTest(RANDOM_PORT)`) | Everything, real HTTP | slowest |

Rule of thumb: edge cases in fast tests, one happy-path lifecycle in the slow test.

### Debugging story: "Unable to establish loopback connection"
- **Symptom:** only the end-to-end test failed; Tomcat couldn't start its connector.
- **Root cause (found by reading the deepest `Caused by`):** `SocketException: Invalid argument: connect`.
  Since JDK 16, Java NIO on Windows uses a Unix-domain socket in the temp dir to wake its selector.
  This machine's temp dir is an 8.3 short path (`C:\Users\CUSTOM~1\...`, from the space in the user name).
- **Verified** by rerunning with `-Djdk.net.unixdomain.tmpdir=<space-free dir>` → pass.
- **Fix:** an OS-activated Maven profile sets that flag for tests and `spring-boot:run`, Windows only.
- **Lesson:** read the *last* `Caused by`, reproduce with one variable changed, then fix at the narrowest scope.

### Legacy markers (things the migration will change)
- `javax.persistence`, `javax.validation`, `javax.servlet` imports → `jakarta.*` in Boot 3.
- Java 11: no records, so verbose DTO classes; `Collectors.toList()` instead of `.toList()`.
- Hand-rolled error JSON → Spring 6 `ProblemDetail` (RFC 9457).
- `@MockBean` → `@MockitoBean` (deprecated in Boot 3.4, removed in Boot 4).
- Hibernate 5 → 6 → 7.

### Interview questions to be ready for
1. Why DTOs instead of returning entities from controllers?
2. What is the N+1 problem and how did you fix it?
3. Why disable open-in-view?
4. `@WebMvcTest` vs `@SpringBootTest`: when do you use each?
5. Why `BigDecimal.compareTo` instead of `equals`?
6. Walk me through how you debugged the Tomcat startup failure.

---

## Phase 2 — Know what you ship: SBOM, vulnerability scan, CI

**Goal:** measure the risk of the legacy stack, reduce it without a framework upgrade, and automate the check.

### SBOM (Software Bill of Materials)
- `cyclonedx-maven-plugin` writes `target/bom.json` during `package`: every library in the jar,
  including **transitive** dependencies (things you never declared, like Tomcat or SnakeYAML).
- Each entry has a **purl** (package URL), e.g. `pkg:maven/org.apache.tomcat.embed/tomcat-embed-core@9.0.83`,
  a universal ID that scanners understand. 62 runtime components here; test scope excluded.

### Scanning against OSV.dev
- `scripts/osv-scan.ps1` sends all purls to OSV's batch API, then pulls severity, CVE aliases and
  fixed versions per advisory. Same data family used by Dependabot / OSV-Scanner / Snyk.
- An advisory can have several IDs: `GHSA-...` (GitHub) and `CVE-...` (MITRE) are aliases for the same issue.
- A finding means "this version is affected," not "this app is exploitable." Triage asks whether
  the vulnerable feature is actually used (e.g. Tomcat DIGEST auth bugs don't apply if you don't use DIGEST auth).

### Results (see `docs/security/`)
| Scan | Total | Critical | High | Moderate | Low |
|---|---|---|---|---|---|
| Boot 2.7.18 as released | 99 | 8 | 40 | 37 | 14 |
| + in-line patches | 54 | 1 | 18 | 26 | 9 |

### Tactical patching (what Never-Ending Support is about)
- Boot's parent POM exposes library versions as properties (`tomcat.version`, `spring-framework.version`,
  `logback.version`...). Overriding them pulls patch releases **without upgrading the framework**.
- **Same-line bumps only** (9.0.83 → 9.0.121), so behavior shouldn't change; the tests confirmed it.
- **The ceiling:** Spring Framework's last *free* 5.3 release is **5.3.39**. OSV lists fixes in 5.3.42,
  but that build is commercial-only; it's not on Maven Central (verified: HTTP 404). Jackson 2.13,
  SnakeYAML 1.x and Hibernate 5.6 have no in-line fixes at all. The remaining critical
  (CVE-2016-1000027, unsafe deserialization in `spring-web`) is only fixed in Spring 6.
- So an EOL app has three options: migrate, pay for extended support, or accept the risk.
  HeroDevs sells the second option; this repo demonstrates the first.

### CI/CD (`.github/workflows/ci.yml`)
- On every push/PR: Temurin 21, cached `~/.m2`, `./mvnw -B verify`, then the OSV scan as a
  **quality gate** (`-FailOn Critical`). SBOM + report uploaded as build artifacts.
- The gate is intentionally **red on the legacy tags** (1 critical left) and should turn green after migration.
- `permissions: contents: read`: least privilege for the workflow token.
- **Dependabot** (`.github/dependabot.yml`) opens weekly PRs for Maven and GitHub Actions updates.

### Docker (`Dockerfile`)
- **Multi-stage:** build with the JDK, run with the JRE only. No compiler, Maven or source in the final image.
- Copy `pom.xml` and run `dependency:go-offline` *before* copying `src/`, so dependency downloads are
  cached and only re-run when the POM changes.
- Runs as a **non-root** user.

### Gotcha: the executable bit
- Git on Windows doesn't track file permissions, so `mvnw` was committed as `100644`. On the Linux CI
  runner `./mvnw` would fail with "Permission denied". Fixed with `git update-index --chmod=+x mvnw` → `100755`.

### Interview questions to be ready for
1. What is an SBOM and why do regulators and customers ask for one?
2. What's a transitive dependency? Give an example from this project.
3. How do you patch a vulnerable library without upgrading the framework, and what are the limits?
4. Does a CVE in a dependency mean your app is vulnerable? How do you triage?
5. Why multi-stage Docker builds and a non-root user?

---

## Phase 3 — Migrate to Spring Boot 3.5 / Java 21

Full details in [MIGRATION.md](MIGRATION.md). Key concepts:

### Why Boot 3 was a "hard" upgrade
- **Jakarta EE:** Oracle gave Java EE to the Eclipse Foundation but kept the `javax` trademark,
  so every EE package was renamed `javax.* → jakarta.*` (JPA, Validation, Servlet...).
  Libraries compiled against `javax` simply don't work with Spring 6. Every file that touches
  JPA, validation or servlets changes.
- **Java 17 minimum**, Hibernate 5 → 6, Spring Framework 5 → 6.

### OpenRewrite
- Automated refactoring from "recipes" (tested transformations). Works on the syntax tree,
  not text, so it understands types and imports. Standard tool for large migrations.
- **Always `dryRun` first** and read the patch. **Commit automated output separately** from
  manual fixes so reviewers can see which is which.
- It can't know intent: it left our temporary version overrides in place.

### "It compiles" is not "it works"
- Mixing major versions of one framework compiles fine and then fails at runtime with
  `NoClassDefFoundError` / `ClassNotFoundException` / `NoSuchMethodError`. Those three errors
  after an upgrade almost always mean a version mismatch on the classpath.
  Diagnose with `mvnw dependency:tree`.

### Modern Java used
- **Records:** immutable data carriers. The compiler writes the constructor, accessors,
  `equals`, `hashCode` and `toString`. Perfect for DTOs; not for JPA entities (they need
  mutability and a no-arg constructor).
- **Pattern matching `instanceof`**, **`Stream.toList()`** (unmodifiable result).

### ProblemDetail (RFC 9457)
- Standard JSON error format: `type`, `title`, `status`, `detail`, `instance`, plus custom properties.
- Extending `ResponseEntityExceptionHandler` makes *all* Spring MVC errors use it.
- Changing error field names (`message` → `detail`) is a **breaking API change** for clients.
  In a real product: version the API or announce it.

### Moving targets
- Boot 3.5's free support ended 2026-06-30. Its final release (3.5.16, June 25) ships Tomcat 10.1.55;
  three critical Tomcat advisories fixed later in 10.1.58 will never land in a free 3.5 release.
- Choose migration targets by **support window**, not by "one version up."

### Interview questions to be ready for
1. Why did `javax` become `jakarta`, and why did that make Boot 3 a breaking upgrade?
2. How did you use OpenRewrite, and what did you have to fix by hand?
3. Your app compiles but fails with `NoClassDefFoundError` after an upgrade. What's your first guess?
4. Records vs. classes: when would you *not* use a record?
5. What is ProblemDetail, and what's the client-facing risk of adopting it?
6. Why wasn't Boot 3.5 the final target?
