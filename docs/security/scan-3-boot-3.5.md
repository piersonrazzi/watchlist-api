# Dependency vulnerability scan: Spring Boot 3.5.16 / Java 21

- Scanned: 2026-09-23 against [OSV.dev](https://osv.dev)
- Components (runtime scope): 65
- Vulnerabilities: **7** across **3** packages

| Severity | Count |
|---|---|
| CRITICAL | 3 |
| HIGH | 0 |
| MODERATE | 4 |
| LOW | 0 |
| UNKNOWN | 0 |

| Severity | Package | Version | Advisory | CVE | Fixed in | Summary |
|---|---|---|---|---|---|---|
| CRITICAL | `org.apache.tomcat.embed:tomcat-embed-core` | 10.1.55 | [GHSA-9xv2-5v5q-p794](https://osv.dev/vulnerability/GHSA-9xv2-5v5q-p794) | CVE-2026-65905 | 11.0.25, 10.1.58, 9.0.121 | Apache Tomcat's DIGEST authenticator has an Authentication Bypass by Capture-replay vulnerability |
| CRITICAL | `org.apache.tomcat.embed:tomcat-embed-core` | 10.1.55 | [GHSA-gcx9-497g-6cp6](https://osv.dev/vulnerability/GHSA-gcx9-497g-6cp6) | CVE-2026-65182 | 11.0.25, 10.1.58, 9.0.121 | Apache Tomcat has an Improper Access Control, Incorrect Authorization vulnerability |
| CRITICAL | `org.apache.tomcat.embed:tomcat-embed-core` | 10.1.55 | [GHSA-h3x4-894j-xpx5](https://osv.dev/vulnerability/GHSA-h3x4-894j-xpx5) | CVE-2026-68525 | 11.0.25, 10.1.58, 9.0.121 | Apache Tomcat's FORM authentication process has an Incorrect Authorization vulnerability |
| MODERATE | `com.fasterxml.jackson.core:jackson-databind` | 2.21.4 | [GHSA-mhm7-754m-9p8w](https://osv.dev/vulnerability/GHSA-mhm7-754m-9p8w) |  | 2.18.9, 2.21.5 | jackson-databind: `@JsonView` bypass for creator properties with `@JsonTypeInfo(include=As.EXTERNAL_PROPERTY)` |
| MODERATE | `com.fasterxml.jackson.core:jackson-databind` | 2.21.4 | [GHSA-5gvw-p9qm-jgwh](https://osv.dev/vulnerability/GHSA-5gvw-p9qm-jgwh) | CVE-2026-59889 | 2.21.5, 2.18.9, 2.22.1 | jackson-databind: @JsonView bypassed for @JsonUnwrapped container properties on deserialization |
| MODERATE | `com.fasterxml.jackson.core:jackson-databind` | 2.21.4 | [GHSA-5jmj-h7xm-6q6v](https://osv.dev/vulnerability/GHSA-5jmj-h7xm-6q6v) | CVE-2026-54515 | 3.1.4, 2.18.9, 2.21.5, 2.22.1 | jackson-databind has case-insensitive deserialization bypasses per-property @JsonIgnoreProperties |
| MODERATE | `org.apache.logging.log4j:log4j-api` | 2.24.3 | [GHSA-qv9r-c865-cp47](https://osv.dev/vulnerability/GHSA-qv9r-c865-cp47) | CVE-2026-49844 | 2.25.5, 2.26.1 | Apache Log4j API: Improper encoding of non-finite floating-point values during MapMessage JSON serialization |
