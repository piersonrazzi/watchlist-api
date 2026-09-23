# watchlist-api

An options-watchlist REST API, built on an **end-of-life** Spring Boot 2.7 / Java 11 stack on
purpose, then secured and migrated step by step to **Spring Boot 4.1 / Java 21**, with every
step tagged, tested and measured with dependency vulnerability scans.

| | Start (`v0.1`) | Finish (`v1.0.0`) |
|---|---|---|
| Spring Boot / Framework | 2.7.18 / 5.3.31 | 4.1.1 / 7.0.9 |
| Java | 11 | 21 (LTS) |
| Hibernate / Jackson / Tomcat | 5.6 / 2.13 / 9.0 | 7.4 / 3.1 / 11.0 |
| Known vulnerabilities (runtime deps) | **99** (8 critical) | **0** |
| Tests | 15 passing | 15 passing |

## The migration, by tag

| Tag | What happened | Vulns (critical) |
|---|---|---|
| `v0.1-legacy-boot2.7` | Working API on Boot 2.7.18 / Java 11 (free support ended 2023-06-30) | 99 (8) |
| `v0.2-legacy-patched` | Patched in place, no framework upgrade: Tomcat, Spring 5.3.39 (last free 5.3), Logback. Added SBOM, OSV scanner, CI gate, Docker | 54 (1) |
| `v0.3-boot3.5` | OpenRewrite-assisted migration to Boot 3.5 (`javax` → `jakarta`), Java 21, records, ProblemDetail. But 3.5 went EOL on 2026-06-30 | 7 (3) |
| `v1.0.0` | Boot 4.1.1: modular starters, Jackson 3, Hibernate 7, RestTestClient, Mockito agent; temporary Tomcat pin for fixes newer than the latest Boot release | **0 (0)** |

Details: [docs/MIGRATION.md](docs/MIGRATION.md) · Scan reports: [docs/security/](docs/security/) ·
Study notes: [docs/LEARNING-LOG.md](docs/LEARNING-LOG.md)

## API

| Method | Path | Result |
|---|---|---|
| `POST` | `/api/watchlists` | `201` + `Location` header |
| `GET` | `/api/watchlists` | All watchlists with their items |
| `GET` | `/api/watchlists/{id}` | One watchlist, or `404` |
| `DELETE` | `/api/watchlists/{id}` | `204` |
| `POST` | `/api/watchlists/{id}/items` | Add a `STOCK`, `CALL` or `PUT`; `201` |
| `DELETE` | `/api/watchlists/{id}/items/{itemId}` | `204` |

Rules: options need `strike` + `expiration`; stocks must have neither; symbols are
upper-cased; duplicates are rejected (strike `150` equals `150.00`).
Errors use [RFC 9457](https://www.rfc-editor.org/rfc/rfc9457) `application/problem+json`:

```json
{"title":"Invalid request","status":400,"detail":"CALL options require both strike and expiration","instance":"/api/watchlists/1/items"}
```

Example:

```bash
curl -X POST localhost:8080/api/watchlists -H "Content-Type: application/json" -d '{"name":"Earnings plays"}'
curl -X POST localhost:8080/api/watchlists/1/items -H "Content-Type: application/json" \
     -d '{"symbol":"nvda","type":"CALL","strike":150,"expiration":"2026-12-18","notes":"into earnings"}'
```

## Run it

Requires JDK 21. No Maven install needed (Maven Wrapper).

```bash
./mvnw spring-boot:run          # macOS / Linux
.\mvnw.cmd spring-boot:run      # Windows
```

Running the jar directly on Windows, if your user folder has a space in its name
(see [the debugging story](docs/LEARNING-LOG.md#debugging-story-unable-to-establish-loopback-connection)):

```powershell
.\mvnw.cmd package
java "-Djdk.net.unixdomain.tmpdir=$env:PUBLIC" -jar target\watchlist-api-1.0.0.jar
```

Docker:

```bash
docker build -t watchlist-api .
docker run -p 8080:8080 watchlist-api
```

## Test and scan

```bash
./mvnw clean verify                                  # 15 tests + jar + SBOM
pwsh scripts/osv-scan.ps1 -FailOn Critical           # vulnerability scan (OSV.dev)
```

| Test | Type | Scope |
|---|---|---|
| `WatchlistServiceTest` | Unit (Mockito) | Business rules |
| `WatchlistControllerTest` | `@WebMvcTest` slice | HTTP, validation, ProblemDetail |
| `WatchlistRepositoryTest` | `@DataJpaTest` slice | Queries, cascades, orphan removal |
| `WatchlistApiIntegrationTest` | `@SpringBootTest` + `RestTestClient` | Full lifecycle over real HTTP |

## CI/CD

GitHub Actions ([ci.yml](.github/workflows/ci.yml)) on every push/PR: build + tests, CycloneDX SBOM,
OSV scan that **fails the build on any critical vulnerability**, and artifacts upload.
Dependabot opens weekly update PRs for Maven and GitHub Actions.

## Project structure

```
src/main/java/dev/razzi/watchlist/
  domain/       JPA entities (Watchlist, WatchlistItem, InstrumentType)
  repository/   Spring Data repository with fetch-join queries (no N+1)
  service/      Business rules and transaction boundaries
  web/          REST controller, ProblemDetail error handling
  web/dto/      Request/response records (the API contract)
scripts/osv-scan.ps1   SBOM -> OSV.dev vulnerability report + quality gate
docs/                  Migration guide, scan reports, learning log
```

## How this was built

Built with AI-assisted development (Claude) as a pair programmer: design decisions, debugging
and every line reviewed and understood, with notes in [docs/LEARNING-LOG.md](docs/LEARNING-LOG.md).
