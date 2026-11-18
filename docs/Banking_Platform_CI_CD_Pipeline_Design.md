# Banking Platform CI/CD Pipeline Design

CI/CD PIPELINE DESIGN

Banking Microservices Platform | GitHub Actions continuous integration and delivery

| Document control | Value |
| --- | --- |
| Document ID | CICD-BANK-001 |
| Repository | https://github.com/ashishionidea/banking-capston-repository- |
| Branch | feature/brd-banking-platform |
| Version | 1.0 |
| Date | 18 November 2026 |
| Status | Draft - environment-specific values require confirmation |

Prepared from the source files and architecture notes available on the specified Git branch.

## 1. Purpose

This document defines the proposed continuous integration and continuous delivery pipeline for the Banking Microservices Platform on feature/brd-banking-platform. The pipeline is designed for the repository’s Maven multi-module Java 21 / Spring Boot 3.4.5 structure and the service boundaries described in docs/architecture.md and docs/requirements-traceability.md.

| Current-state note:  The reviewed branch declares “CI/CD: Jenkins or GitLab CI” in the BRD traceability and README mentions CI, but no GitHub Actions workflow, Dockerfiles, Compose file or Kubernetes deployment manifests were present in the reviewed docs/root snapshot. This document specifies a proposed GitHub Actions implementation. It does not claim a pipeline is already active. |
| --- |

## 2. Pipeline goals and design principles

- Fail fast on compile, unit test and static quality checks for every pull request.

- Build once from a commit SHA and promote the same immutable image digests across environments.

- Separate CI validation from deployment authorization; production requires protected-environment approval.

- Use least-privilege GitHub Actions permissions and short-lived cloud identity (OIDC) wherever supported.

- Never store production credentials, database secrets, JWT signing material or registry passwords in source control.

- Deploy in dependency-aware order and apply post-deployment smoke tests before promotion.

- Keep financial workflows safe: idempotency, Saga compensation, ledger reconciliation and backward-compatible event contracts are release gates.

## 3. Proposed end-to-end flow

```text
Developer push / Pull Request
        |
        v
Checkout + JDK 21 + Maven cache
        |
        v
Validate POMs -> Compile -> Unit / Integration tests
        |
        +--> Static analysis / dependency & secret scans
        |
        v
PR checks required -> Review / merge to main or release branch
        |
        v
Build executable module JARs -> Package container images
        |
        v
Container scan -> Push immutable SHA-tagged images + record digests
        |
        v
Deploy DEV -> health / smoke / integration tests
        |
        v
Deploy QA / UAT -> regression / performance / security sign-off
        |
        v
Production approval -> Deploy canary or rolling release
        |
        v
Post-deploy verification -> monitor -> promote/close or rollback
```

## 4. Branching and trigger strategy

| Event / branch | Pipeline behavior | Deployment |
| --- | --- | --- |
| Pull request to main | Build, tests, quality, vulnerability/secret scan, optionally build images without push | No environment deployment |
| Push to feature/* | Same fast validation checks; optional image build for developer sandbox | No shared/prod deploy by default |
| Push/merge to main | Full build, tests, scan, image publish with commit SHA and provenance | Auto deploy DEV after success |
| Release tag vX.Y.Z | Rebuild or use signed release artifacts; publish version tag mapped to immutable digest | Deploy QA/UAT; production requires approval |
| Manual workflow dispatch | Controlled redeploy of an existing image digest or approved release | Environment and image digest must be explicit inputs |

Protect main with required pull request review, required status checks, no force-push, and CODEOWNERS for workflow, deployment, authentication, payment and infrastructure changes. The feature branch supplied by the user is treated as the documentation/source-review branch, not as the production deployment branch.

## 5. CI stages and quality gates

| Stage | Commands / tools | Gate |
| --- | --- | --- |
| Checkout | actions/checkout; pin action versions or SHAs | Correct commit; clean workspace |
| Toolchain | Temurin JDK 21; Maven wrapper preferred or Maven 3.9+ | Java version and build tool are reproducible |
| Validate | mvn -B -ntp validate | No invalid POM/module graph |
| Compile + tests | mvn -B -ntp clean verify | All reactor modules compile; unit/integration tests pass |
| Coverage | JaCoCo report; publish per-module results | Agree project threshold; baseline proposal 70% line coverage for changed code, risk-based higher for money movement |
| Static analysis | SpotBugs/Checkstyle or SonarQube quality gate | No blocker/critical findings; quality gate green |
| Dependency scan | OWASP Dependency-Check or SCA provider | No unapproved critical/high vulnerabilities |
| Secret scan | Gitleaks or GitHub secret scanning | No leaked credentials or key material |
| Container scan | Trivy or approved registry scanner | No unapproved critical/high OS or app vulnerabilities |
| Artifact attest | Attach SBOM and build provenance; sign image if supported | Image tied to source commit and pipeline run |
| Publish | Push images with immutable commit SHA; record digest | Only trusted branches/tags publish release images |

## 6. Monorepo module strategy

The root pom.xml is a Maven aggregator. It declares service-registry, config-server, api-gateway, customer-service, account-service, transfer-service, bill-payment-service, statement-service, loan-service, support-service and notification-service. The pipeline should validate the complete reactor; a later optimization may use changed-module detection, but downstream dependency impacts must be included.

```text
# Full repository validation (recommended baseline)
mvn -B -ntp clean verify

# Build one runnable service with Maven dependencies, after checking its POM exists
mvn -B -ntp -pl customer-service -am package -DskipTests
```

Avoid ten parallel Maven builds in the first iteration. Start with one reactor build and one test report. Only introduce parallel per-service image packaging after measuring the bottleneck and proving the parent POM/dependency graph is handled correctly.

## 7. GitHub Actions implementation outline

Suggested workflow files: .github/workflows/ci.yml for PR and branch validation; .github/workflows/release.yml for image build/publish; .github/workflows/deploy.yml for environment deployment. Pin third-party actions to reviewed immutable commit SHAs in the final secured implementation.

```text
name: Banking platform CI
on:
  pull_request:
    branches: [ main ]
  push:
    branches: [ main, "feature/**" ]
permissions:
  contents: read
jobs:
  build-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: "21"
          cache: maven
      - name: Validate and test Maven reactor
        run: mvn -B -ntp clean verify
```

The snippet is a minimal starter only. Expand it with test report publication, coverage, static analysis, SCA, secret scanning, SBOM, image build/scan/sign, and guarded deployment jobs before treating it as a release pipeline. Pin action SHAs and configure explicit job permissions in the implemented workflow.

## 8. Image build, versioning and artifact promotion

- Build images only after successful Maven verification. Use service-specific Dockerfiles; these need to be added to the repository.

- Tag images with full Git SHA and optional semantic version (for example, 1.2.0); do not deploy mutable latest tags.

- Record the registry digest (sha256:...) in release metadata and deployment outputs.

- Generate an SBOM (CycloneDX or SPDX) for each image and retain it with the release.

- Sign images and verify signatures/admission policy in Kubernetes where registry and cluster capabilities support it.

- Promote the exact digest from DEV to QA to PROD; rebuilds between environments break traceability.

- Store Maven artifacts, image metadata, test reports and SBOMs with defined retention and access control.

## 9. CD environment model

| Environment | Trigger / approval | Configuration and test focus |
| --- | --- | --- |
| DEV | Automatic after merge to main | Synthetic/test data; deploy all platform dependencies; smoke and component integration |
| QA | Successful DEV gates and release candidate promotion | Stable seeded test data; regression, API contract and security verification |
| UAT / pre-prod | Release manager or product approval | Production-like topology; business acceptance, performance, resilience and operational rehearsal |
| PROD | Protected GitHub Environment with named approvers/change ticket | Immutable digest, canary/rolling strategy, maintenance window where required, post-deploy verification |

## 10. Deployment job responsibilities

1. Authenticate to the cloud/cluster using OIDC short-lived credentials. Avoid long-lived kubeconfig in repository secrets.

1. Select namespace/cluster from protected environment configuration, not arbitrary pull-request input.

1. Run manifest/Helm lint, schema validation and policy checks before apply.

1. Apply ConfigMaps and external secret references; do not print secret values in logs.

1. Deploy dependencies and services in order: Config Server, Eureka, verified domain services, API Gateway.

1. Use immutable image digest, readiness probes, resource requests/limits and rolling update settings.

1. Wait for Kubernetes rollout completion and check pod readiness/restarts.

1. Run post-deployment smoke tests through the API Gateway, including authentication and transfer idempotency/compensation checks.

1. Publish deployment annotations, image digests, commit SHA and test results to the release record.

## 11. Release gates specific to banking workflows

| Risk area | Required automated/manual evidence |
| --- | --- |
| Transfers and ledger | Idempotency replay test, duplicate event test, debit-credit consistency, Saga compensation test, reconciliation report |
| Authentication and authorization | Invalid/expired token rejection, issuer/audience validation, account ownership and role authorization checks |
| Data protection | PII masking tests, encryption configuration, TLS verification, key/secret source validation |
| Event compatibility | Kafka schema compatibility and old/new consumer interoperability across rolling deployment |
| Database migration | Forward-compatible migration tested against a production-like snapshot; restore/rollback plan documented |
| Availability and performance | Load test against BRD targets (100 TPS/service, p95 200 ms) and evidence for 99.9% SLO |
| Audit and compliance | Audit event completeness/retention configuration; security/compliance review and approvals |

## 12. Monitoring, deployment verification and rollback

During and after deployment, monitor API 4xx/5xx rates, request latency percentiles, JVM heap/GC, pod restarts, readiness, HPA saturation, DB connection-pool usage, MySQL errors/replication lag, Kafka consumer lag, dead-letter queue count and Saga states stuck in intermediate status. Ensure log correlation IDs and traces link gateway requests to downstream operations.

Rollback decision: stop rollout if the release gate fails or error/latency thresholds exceed the agreed baseline. Redeploy the previous verified digest. Database changes must follow expand/migrate/contract discipline; application rollback is safe only when the old application remains compatible with the migrated schema and event contracts.

```text
kubectl -n banking-prod rollout status deployment/transfer-service
kubectl -n banking-prod rollout history deployment/transfer-service
kubectl -n banking-prod rollout undo deployment/transfer-service
```

## 13. Secrets, permissions and supply-chain controls

- Workflow permissions default to read-only; grant id-token: write only to deployment jobs that require OIDC.

- Use GitHub Environments for environment-scoped protection rules, variables and approvals.

- Use OIDC federation to cloud provider/registry rather than personal access tokens wherever possible.

- Mask secrets and never echo env output; avoid storing database passwords or JWT keys in build artifacts.

- Pin GitHub Actions by commit SHA and review updates; restrict third-party actions to approved publishers.

- Enable branch protection, required checks, CODEOWNERS and signed commits/tags as required by enterprise policy.

- Retain audit logs and deployment approvals to support change management and forensic review.

## 14. Operational ownership and release checklist

| Owner | Accountability |
| --- | --- |
| Developer | Code quality, tests, API/event compatibility, secure coding, pull request evidence |
| Build/Platform engineering | Runner maintenance, toolchain, registry, reusable workflows, cluster credentials and policy |
| QA / Test engineering | Regression, contract, integration, performance and resilience evidence |
| Security | SCA/container scan triage, secrets, threat review, identity and encryption controls |
| Operations / SRE | Deployment monitoring, SLOs, incident response, capacity, backup/restore and rollback rehearsal |
| Product / Release manager | UAT acceptance, release risk sign-off, change ticket and production approval |

- ☐ Pull request approved and required CI checks green.

- ☐ All 11 declared Maven modules reconciled with actual checked-in module directories and POMs.

- ☐ Unit/integration/contract tests green and reports attached.

- ☐ Quality, SCA, secret and container scans pass or have approved exceptions.

- ☐ SBOM, image signatures/provenance and immutable digests are available.

- ☐ DEV/QA smoke and regression tests pass; business owner accepts UAT.

- ☐ Database/event backward compatibility verified; backup/restore and rollback rehearsed.

- ☐ Production approvers, change window, on-call coverage and monitoring dashboard confirmed.

- ☐ Post-deployment smoke tests pass and release record captures commit + image digest.

## 15. Implementation backlog to activate this design

| Priority | Work item | Exit criterion |
| --- | --- | --- |
| P0 | Reconcile root POM with checked-in modules; confirm loan/support/notification module files | mvn clean verify runs on clean checkout |
| P0 | Add Dockerfiles for runnable modules and local dependency orchestration (Compose or dev profile) | All application images build and run in an isolated test stack |
| P0 | Add CI workflow for PR validation and test reporting | Required checks block merge when build/test fails |
| P1 | Add SAST/SCA/secret scan/SBOM and container scan | Reports visible; policy gates enforced |
| P1 | Add Kubernetes manifests or Helm chart and environment overlays | Deployments, Services, probes, policies and resources validated |
| P1 | Add release and deploy workflows with OIDC and protected environments | DEV auto deploy; PROD approval; digest promotion proven |
| P1 | Add post-deployment API smoke, transfer Saga and idempotency tests | Release cannot progress when critical business checks fail |
| P2 | Add canary strategy, progressive delivery, SLO alerts and automated rollback policy | Controlled rollout and incident drill completed |
| P2 | Add performance/chaos/DR pipeline stages | Evidence supports BRD targets and recovery objectives |

## 16. Repository references

- pom.xml - reactor modules and Java/Spring version properties.

- README.md - development prerequisites and baseline/production caveat.

- docs/architecture.md - service topology, transfer Saga and trust boundaries.

- docs/requirements-traceability.md - BRD functional use cases, NFRs and CI/CD requirement.

- .env.example and service application YAML - environment settings to externalize.

Source branch URL: https://github.com/ashishionidea/banking-capston-repository-/tree/feature/brd-banking-platform