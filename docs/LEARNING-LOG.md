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
