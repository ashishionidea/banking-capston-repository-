# Banking Platform Deployment Guide

DEPLOYMENT GUIDE

Banking Microservices Platform | Build, container and Kubernetes deployment

| Document control | Value |
| --- | --- |
| Document ID | DEP-BANK-001 |
| Repository | https://github.com/ashishionidea/banking-capston-repository- |
| Branch | feature/brd-banking-platform |
| Version | 1.0 |
| Date | 18 November 2026 |
| Status | Draft - environment-specific values require confirmation |

Prepared from the source files and architecture notes available on the specified Git branch.

## 1. Purpose and scope

This document describes how to build, configure, package and deploy the Banking Microservices Platform represented by the feature/brd-banking-platform branch. It covers local validation, container image creation, Kubernetes deployment, rollout verification, operational readiness and rollback.

The repository is a Java 21 / Spring Boot 3.4.5 / Spring Cloud 2024.0.1 Maven multi-module application. Its root POM declares the platform modules and the README identifies MySQL, OAuth2/JWT and Kafka as platform dependencies.

| Important baseline finding:  The reviewed branch currently has no checked-in Dockerfiles, Compose manifest, Kubernetes manifests/Helm chart, or ready-to-run CI workflow. The sequence below is the deployment design to implement; it is not a claim that these deployment assets already exist. The README references Docker Compose, but the manifest was not present in the branch at review time. |
| --- |

## 2. Repository and deployable components

| Module / component | Port | Deployment responsibility |
| --- | --- | --- |
| api-gateway | 8080 | External HTTP entry point; route incoming API requests to discovered services |
| service-registry | 8761 | Eureka server for service registration and discovery |
| config-server | 8888 | Centralized Spring configuration; requires a configured Git-backed config source |
| customer-service | 8081 | Customer registration/profile and authenticated customer data |
| account-service | 8082 | Accounts, balance inquiry and transaction history |
| transfer-service | 8083 | Fund transfers and Kafka-based Saga workflow |
| bill-payment-service | 8084 | Bill payment intent/status; provider adapter remains a mock boundary |
| statement-service | 8085 | Statement generation API; CSV is baseline, PDF/Excel are extensions |
| loan-service | TBD | Declared in root Maven aggregator; verify runtime port/configuration before deployment |
| support-service | TBD | Declared in root Maven aggregator; verify runtime port/configuration before deployment |
| notification-service | TBD | Declared in root Maven aggregator; verify runtime port/configuration before deployment |
| External dependencies | Platform | MySQL databases/schemas, Kafka broker, OAuth2/OIDC identity provider, external Config Git repository |

The existing architecture notes define service-specific ports for all named domain services, including loans (8086), support (8087), and notifications (8088). However, their module build descriptors and runtime files were not all verifiable in the branch tree snapshot. Validate those modules before adding them to a release deployment.

## 3. Target deployment architecture

Recommended target: Kubernetes cluster with one Deployment and ClusterIP Service per Spring Boot component, an Ingress/API Gateway boundary, external managed data services where possible, and managed secret storage. The application gateway is the only public application endpoint. Eureka and Config Server are internal-only.

```text
Client -> TLS Ingress / Load Balancer -> API Gateway -> Eureka-discovered services
                                               |
                           Config Server ------+------> domain services

Domain services -> MySQL (service-owned schema/database)
Domain services <-> Kafka (events / Saga commands)
Services -> Actuator / Prometheus -> monitoring and alerting
```

| Kubernetes object | Recommended configuration |
| --- | --- |
| Namespace | banking-dev / banking-qa / banking-prod; isolate environments |
| Deployment | One per application; immutable image tag; rolling update; resource requests/limits |
| Service | ClusterIP for internal services; expose gateway only via Ingress |
| ConfigMap | Non-secret configuration: ports, service URLs, feature toggles, log levels |
| Secret / external secret | DB passwords, Kafka credentials, JWT/identity provider client secrets, encryption key references |
| Ingress | TLS termination, host/path routing to API Gateway, request size/timeouts and rate limiting |
| HPA | Scale stateless gateway/domain services using CPU/memory and, where available, request metrics |
| PDB | Protect critical services from simultaneous voluntary disruption |
| NetworkPolicy | Default deny; allow only required service-to-service, database and Kafka flows |

## 4. Prerequisites

- Git access to the target branch and a release commit SHA.

- JDK 21 and Maven 3.9+ for local build; Docker/BuildKit for image packaging.

- A Kubernetes cluster and kubectl access; Helm only if the team chooses to introduce a Helm chart.

- Container registry namespace and push credentials (for example, GHCR or an enterprise registry).

- MySQL database/server, Kafka cluster, and an OAuth2/OIDC issuer with correct issuer/audience configuration.

- External Config Server repository URL and environment-specific property sets.

- Secret manager or Kubernetes External Secrets integration; do not commit production credentials.

- TLS certificates, DNS names, ingress controller, monitoring and log collection endpoints.

## 5. Configuration model

The checked-in .env.example includes MYSQL_ROOT_PASSWORD, MYSQL_DATABASE, MYSQL_USER, MYSQL_PASSWORD and JWT_SECRET placeholders. The customer/account/transfer/bill-payment application YAML files use environment substitutions for DB_URL, DB_USERNAME, DB_PASSWORD, KAFKA_BOOTSTRAP_SERVERS, EUREKA_URL and JWT_ISSUER_URI. Keep environment values outside the image.

| Variable / property | Purpose | Deployment guidance |
| --- | --- | --- |
| DB_URL | JDBC connection URL | Set per service schema/database and environment; use TLS and connection pool sizing |
| DB_USERNAME / DB_PASSWORD | Database credentials | Inject from secret manager; use least-privilege service-specific user |
| KAFKA_BOOTSTRAP_SERVERS | Kafka bootstrap list | Use private endpoints; configure TLS/SASL and topic ACLs |
| EUREKA_URL | Eureka server URL | Internal Kubernetes DNS, e.g. http://service-registry:8761/eureka |
| JWT_ISSUER_URI | OIDC/JWT issuer | Must exactly match trusted issuer metadata; validate audience/signature/expiry |
| Config Server source | Git-backed config repository | Pin trusted branch/tag; avoid storing plaintext secrets in Git |
| MYSQL_* / JWT_SECRET | .env.example local placeholders | Local-only defaults; rotate and replace before any shared environment |

| Security warning:  Never use the example defaults or commit a production .env file. Do not treat JWT_SECRET in the sample as a production signing-key design. Use a managed identity provider and external key management with controlled rotation. |
| --- |

## 6. Build and validate locally

1. Checkout the desired branch and record its commit SHA.

git clone https://github.com/ashishionidea/banking-capston-repository-.git
cd banking-capston-repository-
git checkout feature/brd-banking-platform
git rev-parse HEAD

1. Confirm the declared Maven modules exist and compile. The aggregator lists 11 modules; a missing directory or POM will cause Maven reactor failure.

```text
java -version   # JDK 21
mvn -version     # Maven 3.9+
mvn -B clean verify
```

1. Run unit and integration tests. Add Testcontainers or equivalent ephemeral MySQL/Kafka tests where integration coverage is needed. Do not point automated tests at production data.

1. Resolve any failing tests, dependency vulnerabilities and build warnings before creating release images.

## 7. Container image packaging

Create a multi-stage Dockerfile for each executable module (gateway, registry, config server and each domain service). Build from the repository root so Maven parent POM/module dependencies are available. Use a JDK 21 build stage and a minimal supported JRE 21 runtime image, run as a non-root UID, expose only the application port, and add an OCI source/revision label.

# Illustrative pattern; adapt module and final JAR name per service
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY . .
RUN mvn -B -pl customer-service -am clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --uid 10001 appuser
COPY --from=build /workspace/customer-service/target/*.jar app.jar
USER 10001
EXPOSE 8081
ENTRYPOINT ["java","-XX:MaxRAMPercentage=75.0","-jar","/app/app.jar"]

Use a separate service-specific build argument or Dockerfile target for each module. Do not use the wildcard blindly if a module creates multiple JARs. Run the complete Maven build once in CI, then package each runnable module from its verified output. Pin base images by digest for controlled production builds.

```text
docker build -f customer-service/Dockerfile -t <registry>/banking/customer-service:<git-sha> .
docker push <registry>/banking/customer-service:<git-sha>
```

## 8. Deployment order and runbook

| Step | Action | Success criteria |
| --- | --- | --- |
| 1. Namespace + access | Create namespace, service account, RBAC, quotas and network policies | Namespace exists; deployment identity has least privilege |
| 2. Secrets/config | Provision DB/Kafka/OIDC credentials and Config Server repository settings | Pods can read required secrets; no secret appears in logs |
| 3. Data services | Provision MySQL databases/schemas and Kafka topics/ACLs | Connectivity and schema ownership validated |
| 4. Config Server | Deploy Config Server and configure trusted Git config source | /actuator/health UP; config endpoint returns expected profile settings |
| 5. Eureka | Deploy service registry with internal ClusterIP | /actuator/health UP; registry accepts service registrations |
| 6. Domain services | Deploy customer, account, transfer, bill-payment and statement services; then verified loan/support/notification modules | Readiness succeeds; registrations visible in Eureka; DB/Kafka dependencies reachable |
| 7. API Gateway | Deploy gateway and route rules; publish via TLS Ingress | Gateway readiness succeeds; routes resolve to healthy backends |
| 8. Smoke tests | Execute health, authentication, account read, transfer and statement checks using test identities | Expected HTTP status and business state; no duplicate money movement |
| 9. Observe | Watch rollout, logs, latency, errors, Kafka lag, DB pools and pod restarts | No new critical alerts; service-level checks pass |
| 10. Promote | Promote the exact immutable image digests to next environment | Same image digest deployed; environment config changes only |

## 9. Health checks and Kubernetes probes

Spring Boot Actuator is included in the domain-service POMs reviewed, and the service application YAML exposes health, info, metrics and prometheus. Configure probes explicitly after confirming the actual management port and path.

```text
readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 8081
  initialDelaySeconds: 20
  periodSeconds: 10
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8081
  initialDelaySeconds: 40
  periodSeconds: 20
resources:
  requests: { cpu: "250m", memory: "512Mi" }
  limits:   { cpu: "1", memory: "1Gi" }
```

Treat these values as starting examples, not load-tested sizing. Configure graceful shutdown, startup probes for slow-starting components, and health groups appropriate to each service. Avoid liveness probes that fail merely because a remote dependency is briefly unavailable.

## 10. Smoke and functional validation

| Area | Validation |
| --- | --- |
| Platform | Config Server and Eureka are reachable only internally; expected service instances register |
| Gateway | TLS endpoint responds; each required route returns a valid response |
| Authentication | Invalid/expired token rejected; valid token accepted; role and account ownership checked by owning service |
| Account | Balance and transaction history queries return correct test-account data |
| Transfer | Submit once with Idempotency-Key; verify one debit/credit and terminal status; replay same key and ensure no duplicate posting |
| Saga resilience | Simulate destination credit failure; ensure compensation restores source balance and status is COMPENSATED |
| Bill payment | Verify provider mock response and payment status handling; do not represent mock as real biller connectivity |
| Statement | Verify date filtering, account ownership and generated CSV content |
| Observability | Prometheus scrape and structured logs include correlation/trace IDs without tokens or full sensitive data |
| Performance | Run load tests for BRD targets: 100 TPS/service, p95 200 ms; report measured results and bottlenecks |

## 11. Rollback strategy

- Stop promotion when health checks, smoke tests, error rate, latency, or critical Kafka consumer lag breach the release gate.

- Redeploy the last known-good immutable image digest; do not rebuild an old source revision during an incident.

- Roll back application binaries first only when database changes remain backward-compatible. Use expand/migrate/contract database migrations; never assume schema rollback is safe.

- For an event contract change, preserve backward compatibility and keep old/new consumers compatible during the rollout.

- For failed transfers, reconcile persisted Saga state, ledger events and compensation records. Never manually replay financial commands without an approved reconciliation procedure.

- Capture deployment SHA, image digests, timestamps, alerts and operator actions in the incident record.

```text
kubectl -n banking-prod rollout history deployment/transfer-service
kubectl -n banking-prod rollout undo deployment/transfer-service
kubectl -n banking-prod rollout status deployment/transfer-service
```

## 12. Production readiness checklist

- ☐ All declared Maven modules and Docker image builds verified from the release commit.

- ☐ No high/critical dependency, container or source scan findings without approved exception.

- ☐ TLS at ingress and service-to-service; Kafka TLS/SASL/ACL; database TLS and least-privilege accounts.

- ☐ JWT issuer/audience validation; authorization at the owning service; refresh flow handled by trusted OIDC provider.

- ☐ Encryption at rest and key rotation integrated with managed KMS; PII masking in logs.

- ☐ Database backup/restore tested; point-in-time recovery and retention approved.

- ☐ Kafka replication, retention, retry/DLT, idempotency and consumer lag alerts configured.

- ☐ Transfer Saga, reconciliation, duplicate request and compensation scenarios tested.

- ☐ Audit immutability/retention aligned with policy; BRD calls for at least six months, but validate legal/regulatory obligations.

- ☐ HPA/PDB, multi-zone placement, capacity and disaster-recovery plans validated.

- ☐ SLO dashboards/alerts for 99.9% availability, p95 200 ms and 100 TPS target are reviewed with actual measurements.

- ☐ Runbooks, on-call ownership, change approvals and rollback tested.

## 13. Known gaps / assumptions

| Finding | Impact / action |
| --- | --- |
| No Dockerfiles or Compose manifest checked in | Container packaging and local dependency orchestration must be added and tested |
| No Kubernetes/Helm deployment assets checked in | Create manifests/chart, environment overlays, policies and probes |
| No workflow file checked in | Introduce CI/CD workflow as described in companion pipeline document |
| Some modules in root POM not verified in tree | Confirm loan/support/notification module POMs, runtime configs and ports before release |
| OAuth2 authorization server is external | Configure real issuer and token validation; the app is not itself a complete identity provider |
| Config Server requires Git source settings | Provision and test environment-specific config repository access |
| BRD performance/availability goals are targets | Produce load/chaos/DR evidence before claiming conformance |
| Service-level database migrations/ownership | Use managed migrations and avoid production ddl-auto update; define schema lifecycle |

## 14. Repository references

- README.md - prerequisites and development baseline statement.

- pom.xml - Java 21, Spring Boot 3.4.5, Spring Cloud 2024.0.1 and Maven module list.

- docs/architecture.md - component boundaries, service ports, Saga sequence and trust boundaries.

- docs/requirements-traceability.md - BRD use cases, NFRs and implementation caveats.

- .env.example and service application.yml files - environment variable names and local placeholders.

Source branch URL: https://github.com/ashishionidea/banking-capston-repository-/tree/feature/brd-banking-platform