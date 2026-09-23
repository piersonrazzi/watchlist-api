# Dependency vulnerability scan: Spring Boot 2.7.18 + in-line patches

- Scanned: 2026-09-23 against [OSV.dev](https://osv.dev)
- Components (runtime scope): 62
- Vulnerabilities: **54** across **15** packages

| Severity | Count |
|---|---|
| CRITICAL | 1 |
| HIGH | 18 |
| MODERATE | 26 |
| LOW | 9 |
| UNKNOWN | 0 |

| Severity | Package | Version | Advisory | CVE | Fixed in | Summary |
|---|---|---|---|---|---|---|
| CRITICAL | `org.springframework:spring-web` | 5.3.39 | [GHSA-4wrc-f8pq-fpqp](https://osv.dev/vulnerability/GHSA-4wrc-f8pq-fpqp) | CVE-2016-1000027 | 6.0.0 | Pivotal Spring Framework contains unsafe Java deserialization methods |
| HIGH | `com.fasterxml.jackson.core:jackson-core` | 2.13.5 | [GHSA-h46c-h94j-95f3](https://osv.dev/vulnerability/GHSA-h46c-h94j-95f3) | CVE-2025-52999 | 2.15.0 | jackson-core can throw a StackoverflowError when processing deeply nested data |
| HIGH | `com.fasterxml.jackson.core:jackson-core` | 2.13.5 | [GHSA-r7wm-3cxj-wff9](https://osv.dev/vulnerability/GHSA-r7wm-3cxj-wff9) |  | 2.18.8, 2.21.4 | jackson-core: Async parser maxNumberLength bypass via chunked digit accumulation (incomplete fix for GHSA-72hv-8253-57qq) |
| HIGH | `com.fasterxml.jackson.core:jackson-databind` | 2.13.5 | [GHSA-rmj7-2vxq-3g9f](https://osv.dev/vulnerability/GHSA-rmj7-2vxq-3g9f) | CVE-2026-54513 | 2.18.8, 2.21.4, 3.1.4 | jackson-databind has an array subtype allowlist bypass in BasicPolymorphicTypeValidator (allowIfSubTypeIsArray) |
| HIGH | `com.fasterxml.jackson.core:jackson-databind` | 2.13.5 | [GHSA-j3rv-43j4-c7qm](https://osv.dev/vulnerability/GHSA-j3rv-43j4-c7qm) | CVE-2026-54512 | 2.18.8, 3.1.4, 2.21.4 | jackson-databind has a PolymorphicTypeValidator bypass via generic type parameters that allows arbitrary class instantiation |
| HIGH | `com.h2database:h2` | 2.1.214 | [GHSA-22wj-vf5f-wrvj](https://osv.dev/vulnerability/GHSA-22wj-vf5f-wrvj) | CVE-2022-45868 | 2.2.220 | Password exposure in H2 Database  |
| HIGH | `org.hibernate:hibernate-core` | 5.6.15.Final | [GHSA-2p5w-cvg5-gc5c](https://osv.dev/vulnerability/GHSA-2p5w-cvg5-gc5c) | CVE-2026-0603 | none published | Hibernate vulnerable to SQL Injection |
| HIGH | `org.springframework.boot:spring-boot` | 2.7.18 | [GHSA-wwpq-f5c3-7hvx](https://osv.dev/vulnerability/GHSA-wwpq-f5c3-7hvx) | CVE-2026-40973 | 4.0.6, 3.5.14 | Spring Boot accepts predictable temp directory without ownership verification |
| HIGH | `org.springframework.boot:spring-boot` | 2.7.18 | [GHSA-rc42-6c7j-7h5r](https://osv.dev/vulnerability/GHSA-rc42-6c7j-7h5r) | CVE-2025-22235 | 3.3.11, 3.4.5 | Spring Boot EndpointRequest.to() creates wrong matcher if actuator endpoint is not exposed |
| HIGH | `org.springframework.data:spring-data-commons` | 2.7.18 | [GHSA-9fw2-h3hf-293r](https://osv.dev/vulnerability/GHSA-9fw2-h3hf-293r) | CVE-2026-41716 | 4.0.6, 3.5.12 | Spring Data Commons: Heap exhaustion from unbounded property-lookup cache retaining crafted string keys |
| HIGH | `org.springframework:spring-core` | 5.3.39 | [GHSA-jmp9-x22r-554x](https://osv.dev/vulnerability/GHSA-jmp9-x22r-554x) | CVE-2025-41249 | 6.2.11 | Spring Framework annotation detection mechanism may result in improper authorization |
| HIGH | `org.springframework:spring-expression` | 5.3.39 | [GHSA-r5w3-xv2f-j59q](https://osv.dev/vulnerability/GHSA-r5w3-xv2f-j59q) | CVE-2026-41850 | 7.0.8, 6.2.19 | Spring Framework Algorithmic Denial of Service via SpEL Expressions |
| HIGH | `org.springframework:spring-expression` | 5.3.39 | [GHSA-775g-4xr8-78h8](https://osv.dev/vulnerability/GHSA-775g-4xr8-78h8) | CVE-2026-41849 | none published | Spring Framework Denial of Service via Integer Overflow in SpEL Expressions |
| HIGH | `org.springframework:spring-webmvc` | 5.3.39 | [GHSA-g5vr-rgqm-vf78](https://osv.dev/vulnerability/GHSA-g5vr-rgqm-vf78) | CVE-2024-38819 | 6.1.14 | Spring Framework Path Traversal vulnerability |
| HIGH | `org.springframework:spring-webmvc` | 5.3.39 | [GHSA-x23c-287f-qqv5](https://osv.dev/vulnerability/GHSA-x23c-287f-qqv5) | CVE-2026-41842 | 7.0.8, 6.2.19 | Spring Framework Denial of Service via Versioned Resources in Spring MVC and WebFlux |
| HIGH | `org.springframework:spring-webmvc` | 5.3.39 | [GHSA-cx7f-g6mp-7hqm](https://osv.dev/vulnerability/GHSA-cx7f-g6mp-7hqm) | CVE-2024-38816 | 6.1.13 | Path traversal vulnerability in functional web frameworks |
| HIGH | `org.springframework:spring-webmvc` | 5.3.39 | [GHSA-3chg-m5w7-qfv5](https://osv.dev/vulnerability/GHSA-3chg-m5w7-qfv5) | CVE-2026-41845 | 7.0.8, 6.2.19 | Spring Framework Cross-site Scripting via JavaScriptUtils |
| HIGH | `org.yaml:snakeyaml` | 1.30 | [GHSA-3mc7-4q67-w48m](https://osv.dev/vulnerability/GHSA-3mc7-4q67-w48m) | CVE-2022-25857 | 1.31 | Uncontrolled Resource Consumption in snakeyaml |
| HIGH | `org.yaml:snakeyaml` | 1.30 | [GHSA-mjmj-j48q-9wg2](https://osv.dev/vulnerability/GHSA-mjmj-j48q-9wg2) | CVE-2022-1471 | 2.0 | SnakeYaml Constructor Deserialization Remote Code Execution |
| MODERATE | `ch.qos.logback:logback-core` | 1.2.13 | [GHSA-pr98-23f8-jwxv](https://osv.dev/vulnerability/GHSA-pr98-23f8-jwxv) | CVE-2024-12798 | 1.5.13, 1.3.15 | QOS.CH logback-core Expression Language Injection vulnerability |
| MODERATE | `ch.qos.logback:logback-core` | 1.2.13 | [GHSA-25qh-j22f-pwp8](https://osv.dev/vulnerability/GHSA-25qh-j22f-pwp8) | CVE-2025-11226 | 1.5.19, 1.3.16 | QOS.CH logback-core is vulnerable to Arbitrary Code Execution through file processing |
| MODERATE | `com.fasterxml.jackson.core:jackson-databind` | 2.13.5 | [GHSA-hgj6-7826-r7m5](https://osv.dev/vulnerability/GHSA-hgj6-7826-r7m5) | CVE-2026-54514 | 2.18.8, 2.21.4, 3.1.4 | jackson-databind: InetSocketAddress deserialization triggers eager DNS resolution (SSRF) |
| MODERATE | `com.fasterxml.jackson.core:jackson-databind` | 2.13.5 | [GHSA-3wrr-7qpf-2prh](https://osv.dev/vulnerability/GHSA-3wrr-7qpf-2prh) | CVE-2026-50193 | 2.14.0 | jackson-databind: Deeply nested JsonNode throws StackOverflowError for toString() |
| MODERATE | `com.fasterxml.jackson.core:jackson-databind` | 2.13.5 | [GHSA-5jmj-h7xm-6q6v](https://osv.dev/vulnerability/GHSA-5jmj-h7xm-6q6v) | CVE-2026-54515 | 3.1.4, 2.18.9, 2.21.5, 2.22.1 | jackson-databind has case-insensitive deserialization bypasses per-property @JsonIgnoreProperties |
| MODERATE | `org.apache.logging.log4j:log4j-api` | 2.17.2 | [GHSA-qv9r-c865-cp47](https://osv.dev/vulnerability/GHSA-qv9r-c865-cp47) | CVE-2026-49844 | 2.25.5, 2.26.1 | Apache Log4j API: Improper encoding of non-finite floating-point values during MapMessage JSON serialization |
| MODERATE | `org.springframework.boot:spring-boot-autoconfigure` | 2.7.18 | [GHSA-ggg2-9786-hwc8](https://osv.dev/vulnerability/GHSA-ggg2-9786-hwc8) | CVE-2026-41001 | 4.0.7, 3.5.15 | Spring Boot: Predictable Temp Directory in Artemis Auto-configuration |
| MODERATE | `org.springframework.data:spring-data-commons` | 2.7.18 | [GHSA-5m4m-73w9-8433](https://osv.dev/vulnerability/GHSA-5m4m-73w9-8433) | CVE-2026-41721 | 4.0.6, 3.5.12 | Spring Data Commons: Denial of Service via excessive memory allocation in projection binding |
| MODERATE | `org.springframework.data:spring-data-commons` | 2.7.18 | [GHSA-5vpf-xvv7-c8vh](https://osv.dev/vulnerability/GHSA-5vpf-xvv7-c8vh) | CVE-2026-41711 | 4.0.6, 3.5.12 | Spring Data Commons: StackOverflowException when parsing Sort parameters (DoS) |
| MODERATE | `org.springframework:spring-context` | 5.3.39 | [GHSA-4gc7-5j7h-4qph](https://osv.dev/vulnerability/GHSA-4gc7-5j7h-4qph) | CVE-2024-38820 | 6.1.14 | Spring Framework DataBinder Case Sensitive Match Exception |
| MODERATE | `org.springframework:spring-expression` | 5.3.39 | [GHSA-wxpp-56q6-5pcg](https://osv.dev/vulnerability/GHSA-wxpp-56q6-5pcg) | CVE-2026-41851 | 7.0.8, 6.2.19 | Spring Framework Denial of Service via Unbounded Cache in SpEL |
| MODERATE | `org.springframework:spring-web` | 5.3.39 | [GHSA-4gc7-5j7h-4qph](https://osv.dev/vulnerability/GHSA-4gc7-5j7h-4qph) | CVE-2024-38820 | 6.1.14 | Spring Framework DataBinder Case Sensitive Match Exception |
| MODERATE | `org.springframework:spring-webmvc` | 5.3.39 | [GHSA-r936-gwx5-v52f](https://osv.dev/vulnerability/GHSA-r936-gwx5-v52f) | CVE-2025-41242 | 6.2.10 | Spring Framework MVC Applications Path Traversal Vulnerability |
| MODERATE | `org.springframework:spring-webmvc` | 5.3.39 | [GHSA-h3qp-gqrc-q736](https://osv.dev/vulnerability/GHSA-h3qp-gqrc-q736) | CVE-2026-41844 | 7.0.8, 6.2.19 | Spring Framework Open Redirect in Spring MVC and WebFlux |
| MODERATE | `org.springframework:spring-webmvc` | 5.3.39 | [GHSA-w3c8-7r8f-9jp8](https://osv.dev/vulnerability/GHSA-w3c8-7r8f-9jp8) | CVE-2024-38828 | 5.3.42 | Spring MVC controller vulnerable to a DoS attack |
| MODERATE | `org.springframework:spring-webmvc` | 5.3.39 | [GHSA-mq64-j8f9-9gcj](https://osv.dev/vulnerability/GHSA-mq64-j8f9-9gcj) | CVE-2026-41841 | 7.0.8, 6.2.19 | Spring Framework Information Disclosure via Static Resource Cache in Spring MVC and WebFlux |
| MODERATE | `org.springframework:spring-webmvc` | 5.3.39 | [GHSA-6p4f-wcwh-5vvm](https://osv.dev/vulnerability/GHSA-6p4f-wcwh-5vvm) | CVE-2026-22745 | 7.0.7, 6.2.18 | Spring MVC and WebFlux applications are vulnerable to Denial of Service attacks when resolving static resources |
| MODERATE | `org.springframework:spring-webmvc` | 5.3.39 | [GHSA-4773-3jfm-qmx3](https://osv.dev/vulnerability/GHSA-4773-3jfm-qmx3) | CVE-2026-22737 | 7.0.6, 6.2.17 | Spring Framework Improper Path Limitation with Script View Templates |
| MODERATE | `org.springframework:spring-webmvc` | 5.3.39 | [GHSA-72pg-x5f8-j25j](https://osv.dev/vulnerability/GHSA-72pg-x5f8-j25j) | CVE-2026-41843 | 7.0.8, 6.2.19 | Spring Framework Path Traversal via Versioned Static Resources in Spring MVC and WebFlux |
| MODERATE | `org.springframework:spring-webmvc` | 5.3.39 | [GHSA-cjpg-rgq5-fr37](https://osv.dev/vulnerability/GHSA-cjpg-rgq5-fr37) | CVE-2026-41853 | 7.0.8, 6.2.19 | Spring Framework Multipart Request Smuggling in Spring MVC and WebFlux |
| MODERATE | `org.springframework:spring-webmvc` | 5.3.39 | [GHSA-957g-f97v-vppc](https://osv.dev/vulnerability/GHSA-957g-f97v-vppc) | CVE-2026-41846 | 7.0.8, 6.2.19 | Spring Framework Cross-site Scripting via JSP Form Tags |
| MODERATE | `org.yaml:snakeyaml` | 1.30 | [GHSA-w37g-rhq8-7m4j](https://osv.dev/vulnerability/GHSA-w37g-rhq8-7m4j) | CVE-2022-41854 | 1.32 | Snakeyaml vulnerable to Stack overflow leading to denial of service |
| MODERATE | `org.yaml:snakeyaml` | 1.30 | [GHSA-hhhw-99gj-p3c3](https://osv.dev/vulnerability/GHSA-hhhw-99gj-p3c3) | CVE-2022-38750 | 1.31 | snakeYAML before 1.31 vulnerable to Denial of Service due to Out-of-bounds Write |
| MODERATE | `org.yaml:snakeyaml` | 1.30 | [GHSA-9w3m-gqgf-c4p9](https://osv.dev/vulnerability/GHSA-9w3m-gqgf-c4p9) | CVE-2022-38752 | 1.32 | snakeYAML before 1.32 vulnerable to Denial of Service due to Out-of-bounds Write |
| MODERATE | `org.yaml:snakeyaml` | 1.30 | [GHSA-c4r9-r8fh-9vj2](https://osv.dev/vulnerability/GHSA-c4r9-r8fh-9vj2) | CVE-2022-38749 | 1.31 | snakeYAML before 1.31 vulnerable to Denial of Service due to Out-of-bounds Write |
| MODERATE | `org.yaml:snakeyaml` | 1.30 | [GHSA-98wm-3w3q-mw94](https://osv.dev/vulnerability/GHSA-98wm-3w3q-mw94) | CVE-2022-38751 | 1.31 | snakeYAML before 1.31 vulnerable to Denial of Service due to Out-of-bounds Write |
| LOW | `ch.qos.logback:logback-core` | 1.2.13 | [GHSA-6v67-2wr5-gvf4](https://osv.dev/vulnerability/GHSA-6v67-2wr5-gvf4) | CVE-2024-12801 | 1.5.13, 1.3.15 | QOS.CH logback-core Server-Side Request Forgery vulnerability |
| LOW | `ch.qos.logback:logback-core` | 1.2.13 | [GHSA-jhq6-gfmj-v8fx](https://osv.dev/vulnerability/GHSA-jhq6-gfmj-v8fx) | CVE-2026-10532 | 1.5.34 | Logback vulnerable to Object Injection through HardenedObjectInputStream modules |
| LOW | `ch.qos.logback:logback-core` | 1.2.13 | [GHSA-p47f-322f-whfh](https://osv.dev/vulnerability/GHSA-p47f-322f-whfh) | CVE-2026-9828 | 1.5.33 | QOS.CH Sarl logback logback-core has a deserialization of untrusted data vulnerability |
| LOW | `ch.qos.logback:logback-core` | 1.2.13 | [GHSA-qqpg-mvqg-649v](https://osv.dev/vulnerability/GHSA-qqpg-mvqg-649v) | CVE-2026-1225 | 1.5.25 | Logback allows an attacker to instantiate classes already present on the class path |
| LOW | `org.springframework:spring-context` | 5.3.39 | [GHSA-4wp7-92pw-q264](https://osv.dev/vulnerability/GHSA-4wp7-92pw-q264) | CVE-2025-22233 | 6.2.7, 6.1.20 | Spring Framework DataBinder Case Sensitive Match Exception |
| LOW | `org.springframework:spring-core` | 5.3.39 | [GHSA-659m-px2c-25wj](https://osv.dev/vulnerability/GHSA-659m-px2c-25wj) | CVE-2026-41848 | 7.0.8, 6.2.19 | Spring Framework Denial of Service via AntPathMatcher |
| LOW | `org.springframework:spring-expression` | 5.3.39 | [GHSA-9f52-rjqv-25qv](https://osv.dev/vulnerability/GHSA-9f52-rjqv-25qv) | CVE-2026-41852 | 7.0.8, 6.2.19 | Spring Framework Arbitrary Method Invocation in SpEL Expressions |
| LOW | `org.springframework:spring-webmvc` | 5.3.39 | [GHSA-6hcq-hmm3-jj3c](https://osv.dev/vulnerability/GHSA-6hcq-hmm3-jj3c) | CVE-2026-22735 | 7.0.6, 6.2.17 | Spring MVC and WebFlux has Server Sent Event stream corruption |
| LOW | `org.springframework:spring-webmvc` | 5.3.39 | [GHSA-wg35-8jpf-2xv3](https://osv.dev/vulnerability/GHSA-wg35-8jpf-2xv3) | CVE-2026-22741 | 7.0.7, 6.2.18 | Spring MVC and WebFlux applications are vulnerable to cache poisoning when resolving static resources. |
