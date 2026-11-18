# Banking Microservices System — Architecture

## Component diagram

```mermaid
flowchart TB
  WEB[Web / Mobile Client] --> GW[API Gateway :8080]
  GW --> EUREKA[Eureka Discovery :8761]
  GW --> CUST[Customer Service :8081]
  GW --> ACC[Account Service :8082]
  GW --> TRANS[Transfer Service :8083]
  GW --> BILL[Bill Payment Service :8084]
  GW --> STMT[Statement Service :8085]
  GW --> LOAN[Loan Service :8086]
  GW --> SUPPORT[Support Service :8087]
  GW --> NOTIFY[Notification Service :8088]
  CUST --> CDB[(Customer DB)]
  ACC --> ADB[(Account DB)]
  TRANS --> TDB[(Transfer DB)]
  BILL --> BDB[(Bill Payment DB)]
  STMT --> SDB[(Statement DB)]
  LOAN --> LDB[(Loan DB)]
  SUPPORT --> UDB[(Support DB)]
  NOTIFY --> NDB[(Notification DB)]
  CUST <--> K[(Kafka)]
  ACC <--> K
  TRANS <--> K
  BILL <--> K
  STMT <--> K
  LOAN <--> K
  SUPPORT <--> K
  NOTIFY <--> K
  CONFIG[Spring Cloud Config Server :8888] --> CUST
  CONFIG --> ACC
  CONFIG --> TRANS
  CONFIG --> BILL
  CONFIG --> STMT
  CONFIG --> LOAN
  CONFIG --> SUPPORT
  CONFIG --> NOTIFY
  OBS[Actuator / Prometheus / ELK] -. metrics & logs .-> CUST
  OBS -.-> ACC
  OBS -.-> TRANS
  OBS -.-> BILL
  OBS -.-> STMT
  OBS -.-> LOAN
  OBS -.-> SUPPORT
  OBS -.-> NOTIFY
```

## Service responsibilities and ports

| Service | Port | Responsibility / BRD use cases |
|---|---:|---|
| API Gateway | 8080 | Single entry point, route predicates, load-balanced service IDs |
| Eureka Registry | 8761 | Service registration/discovery |
| Config Server | 8888 | Centralized external configuration via Git-backed repository |
| Customer Service | 8081 | Registration, profile update, identity-provider token validation integration |
| Account Service | 8082 | Accounts, balance inquiry, transaction history query |
| Transfer Service | 8083 | Fund transfers, Saga orchestration, compensation |
| Bill Payment Service | 8084 | Biller payment intent, payment status, provider adapter |
| Statement Service | 8085 | Period/date-range statement generation |
| Loan Service | 8086 | Loan application intake and acknowledgement |
| Support Service | 8087 | Customer support cases/tickets |
| Notification Service | 8088 | Email/SMS/push event adapter |

## Transfer Saga sequence

```mermaid
sequenceDiagram
  participant Client
  participant Gateway
  participant Transfer as Transfer Service
  participant DB as Transfer DB
  participant Kafka
  participant Account as Account Service
  Client->>Gateway: POST /api/transfers + Idempotency-Key
  Gateway->>Transfer: authenticated request
  Transfer->>DB: persist STARTED transfer
  Transfer->>Kafka: TransferRequested
  Transfer-->>Client: 202 Accepted + reference
  Kafka->>Account: debit source (idempotent command)
  Account->>Kafka: SourceDebited / DebitRejected
  Kafka->>Transfer: SourceDebited
  Transfer->>Kafka: CreditDestination command
  Kafka->>Account: credit destination (idempotent)
  Account->>Kafka: DestinationCredited / CreditRejected
  alt credit succeeded
    Kafka->>Transfer: DestinationCredited
    Transfer->>DB: mark COMPLETED
    Transfer->>Kafka: TransferCompleted
  else credit failed after debit
    Kafka->>Transfer: DestinationCreditRejected
    Transfer->>Kafka: CompensateSourceDebit
    Kafka->>Account: credit source back
    Account->>Kafka: SourceDebitCompensated
    Transfer->>DB: mark COMPENSATED
  end
```

## Explanation
1. The Gateway is the only externally published application entry point. It routes by Eureka service ID and can load balance across service instances.
2. Each domain service owns its database/schema and business rules. Services do not directly read each other's databases.
3. REST is used for synchronous queries and request acknowledgement. Kafka is used for durable asynchronous workflows and domain events.
4. A transfer is not a single cross-database ACID transaction. The Saga persists each local step and uses a compensating credit if the destination-credit step fails after a successful debit.
5. A transfer idempotency key and event/command idempotency records are required to prevent duplicate debits/credits during at-least-once delivery.
6. Config Server supplies non-secret external settings. Secrets and encryption keys must come from a managed secrets system, not source control.
7. Actuator/Prometheus expose health and metrics; logs carry correlation IDs and audit events are persisted independently.

## Trust boundaries
- Validate issuer, audience, signature and expiry of OAuth2 JWTs at Gateway and/or service resource servers.
- Use TLS for all network hops. For production, use mTLS between services and Kafka TLS/SASL with ACLs.
- Encrypt sensitive PII before database persistence using AES-256-GCM and external key management with rotation.
- Never log credentials, tokens, full card data or unmasked personal identifiers.
- All account/payment endpoints must enforce ownership and authorization in the owning service, not only at Gateway.

## Operational caveats
The BRD's 200 ms p95, 100 TPS and 99.9% availability requirements are measurable service-level objectives, not something a source-code scaffold can guarantee. They require load tests, multi-zone deployments, capacity planning, dashboards, alerts, runbooks and operational review. GDPR/PCI DSS compliance also requires organizational and infrastructure controls beyond application code.
