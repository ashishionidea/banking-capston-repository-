# BRD requirements traceability

This matrix is derived from the supplied Business Requirements Document for Banking Application. "Implemented baseline" means a deployable starter API/workflow is present; business/vendor integrations and production controls are explicitly identified as follow-up work.

## Functional use cases

| BRD use case | Owning service | Initial API / workflow | Current baseline |
|---|---|---|---|
| 1. User Registration | Customer Service | `POST /api/customers/register` | Customer identity/profile persistence; email confirmation is an event to Notification Service |
| 2. Account Login | Customer Service / identity provider | `POST /api/auth/token` | OAuth2 resource-server compatible JWT contract; local password login is deliberately not represented as a full OAuth2 authorization server |
| 3. Account Balance Inquiry | Account Service | `GET /api/accounts/{accountNumber}/balance` | Account ownership and balance query |
| 4. Fund Transfer | Transfer Service | `POST /api/transfers`, `GET /api/transfers/{reference}` | Kafka Saga: debit, credit, ledger event and compensation |
| 5. Bill Payment | Bill Payment Service | `POST /api/bill-payments`, `GET /api/bill-payments/{reference}` | Validated payment intent and status; biller adapter is a mock boundary pending provider integration |
| 6. Account Statement Generation | Statement Service | `GET /api/statements?accountNumber=&from=&to=` | Builds CSV statement from transaction API contract; PDF/Excel rendering is an extension point |
| 7. Loan Application | Loan Service | `POST /api/loans/applications`, `GET /api/loans/applications/{reference}` | Application capture/acknowledgement; eligibility and underwriting rules need business inputs |
| 8. Update Personal Information | Customer Service | `GET/PUT /api/customers/me` | Profile read/update with authenticated subject |
| 9. Transaction History Review | Account Service | `GET /api/accounts/{accountNumber}/transactions` | Chronological transaction retrieval contract; transfer service publishes ledger records |
| 10. Customer Support Inquiry | Support Service | `POST /api/support/tickets`, `GET /api/support/tickets/{reference}` | Ticket creation, reference and status |

## Non-functional requirements

| BRD area | BRD requirement | Design / implementation |
|---|---|---|
| Scalability | Horizontal scaling, load balancing, service discovery | Independent Spring Boot deployables, Eureka and Gateway load-balanced routes |
| Performance | 95% operations within 200 ms; batch max 2 s; 100 TPS per service | These are target SLOs, not proven by code. Load-test plan and measurement required before sign-off |
| Security | AES-256 for sensitive data at rest; TLS between services | Encryption adapter uses AES-256-GCM and environment-provided key; deployment TLS/mTLS is documented and must be supplied by platform ingress/service mesh |
| Authentication | OAuth2/JWT, 15-minute expiry, refreshable active sessions | Resource server verifies issuer/audience/expiry; external OIDC authorization server must issue tokens and own refresh-token rotation |
| Availability | 99.9% uptime | Health/Actuator and resilience patterns; uptime needs infrastructure/SLO monitoring |
| Failover | Resilience4j circuit breaker | Resilience4j dependency and starter configuration on service-to-service integrations |
| Compliance | GDPR and PCI DSS | Design consideration only; certification requires legal, security and operational assessment |
| Audit | Activities, access and changes; retain minimum 6 months | Audit event service writes records; production retention/immutability policy must be configured |
| Reliability | HTTP 4xx/5xx; distributed consistency via Saga/CQRS | Common error response; Kafka Saga for transfer workflow |
| Maintainability | SRP, independently deployable modules | Separate Maven modules, databases/schema ownership and service containers |
| Configuration | Spring Cloud Config Server | Config Server module; external Git-backed config URI is deployment configuration |
| CI/CD | Jenkins or GitLab CI | GitHub Actions CI supplied as repository automation; can be replaced by Jenkins/GitLab pipeline |
| Usability | Swagger/OpenAPI and user-friendly errors | Springdoc OpenAPI, validation and common error response |
| Monitoring | ELK, Actuator, Prometheus/Grafana | Actuator/Prometheus endpoint, structured JSON logs; ELK/Grafana deployment manifests are platform work |
| Interoperability | REST/gRPC, JSON | REST/JSON contracts, Gateway routing and service discovery |

## Design clarifications and assumptions
- The BRD names an "account" both for a customer profile and for a bank account. This design separates Customer identity/profile from Account/balance.
- The BRD does not define supported billers, loan eligibility rules, KYC/AML provider, statement branding, support SLA or notification vendor. Those remain adapter/ruleset decisions, not fabricated production integrations.
- The BRD's registration step asks for valid identification information, but does not define a document/KYC schema. Registration captures name/email/phone, while real identity verification is a required integration.
- OAuth2 refresh flow is the responsibility of the configured identity provider; a resource server must never mint or refresh user tokens.
- The repository starts with separate service databases in Compose to preserve service ownership; transfer events provide eventual consistency.
