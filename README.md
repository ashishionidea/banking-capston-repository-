# Banking Microservices System

Java 21 / Spring Boot 3.4 / Spring Cloud / MySQL / OAuth2-JWT / Kafka.

This repository implements the Banking Application BRD. The planned service boundaries cover customer registration/profile, authentication, accounts, transfers, bill payments, statements, loans, transaction history and support. Shared platform capabilities include API Gateway, Eureka discovery, centralized configuration, resilience, observability, audit, OpenAPI and CI.

## BRD requirements
See [docs/requirements-traceability.md](docs/requirements-traceability.md) for mapping of all ten BRD use cases and the non-functional requirements to modules, APIs and implementation status. See [docs/architecture.md](docs/architecture.md) for the architecture and service boundaries.

## Local development
Prerequisites: JDK 21, Maven 3.9+, Docker Compose.

```bash
docker compose up -d mysql kafka
mvn clean verify
```

The modules have Dockerfiles and can be run with the Compose stack after implementation is reviewed.

## Important
This is a development baseline, not production-ready banking software. Before production use, complete security review, encryption/key management, TLS/mTLS, refresh-token rotation, outbox/retry/DLT policies, audit retention and regulatory compliance validation. No real customer data or credentials belong in this repository.
