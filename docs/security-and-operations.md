# Security and operations guide

## OAuth2 and JWT
Configure `JWT_ISSUER_URI` to an OAuth2/OIDC issuer that issues tokens with the customer's numeric database ID as `sub`. Services validate issuer, signature, expiry and (in production) audience. The repository intentionally does not pretend to be an OAuth2 authorization server. User login, consent, refresh-token rotation and session policy must be provided by a hardened identity provider.

## AES-256-GCM PII field encryption
Customer Service includes `EncryptionService`. Configure `PII_AES_KEY_B64` to a base64 string encoding exactly 32 random bytes, supplied by a secrets manager/KMS. Never store this key in Git, an image, or Compose file. The utility fails closed when encryption is invoked without a key. A production rollout also needs key identifiers, rotation/decryption of older ciphertext, access controls and database migration of encrypted fields.

## TLS
Terminate TLS at a trusted ingress/load balancer and use TLS/mTLS for service-to-service connections. The local Compose topology is plaintext-only and is not suitable for production. Configure Kafka TLS/SASL and ACLs in a production cluster.

## Audit and log retention
Audit events must capture actor/customer ID, action, resource, timestamp, correlation ID, outcome and source metadata while excluding secrets and payment credentials. Store immutable audit records and enforce at least six months' retention in the logging/storage platform. Log shipping/retention policy is infrastructure-specific and must be validated before compliance sign-off.

## Reliability, tracing and monitoring
Use bounded retry/backoff and dead-letter handling for Kafka consumers. Apply circuit breakers/timeouts to remote calls. Actuator exposes health and metrics; scrape Prometheus endpoints and create dashboards/alerts. Ship structured logs to ELK and propagate trace/correlation IDs.

## BRD measurable targets
- p95 API response time below 200 ms for 95% of supported operations.
- At least 100 TPS per microservice under normal conditions.
- Batch processing maximum 2 seconds.
- 99.9% availability.
These require repeatable load tests, capacity planning, multi-zone runtime, dashboards and incident runbooks. They are not guaranteed by a code scaffold.

## Compliance
GDPR and PCI DSS are compliance programs involving governance, personnel, infrastructure, processes and audit evidence. The codebase alone cannot certify compliance. Do not persist cardholder authentication data or real card details in this capstone.
