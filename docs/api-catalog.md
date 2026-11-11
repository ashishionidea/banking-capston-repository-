# API catalog (initial implementation)

All APIs below are routed through API Gateway at `http://localhost:8080`. Resource endpoints except public registration and health require an OAuth2 Bearer JWT issued by the configured identity provider.

| BRD use case | Method and path | Service |
|---|---|---|
| Register customer | POST `/api/customers/register` | Customer |
| Login | OAuth2 OIDC flow at configured identity provider; JWT sent to Gateway | Identity provider (external) |
| Balance inquiry | GET `/api/accounts/{accountNumber}/balance` | Account |
| Fund transfer | POST `/api/transfers` with `Idempotency-Key`; GET `/api/transfers/{reference}` | Transfer |
| Bill payment | POST `/api/bill-payments`; GET `/api/bill-payments/{reference}` | Bill Payment |
| Statement | GET `/api/statements?accountNumber=&from=YYYY-MM-DD&to=YYYY-MM-DD` | Statement |
| Loan application | POST `/api/loans/applications`; GET `/api/loans/applications/{reference}` | Loan |
| Update profile | GET/PUT `/api/customers/me` | Customer |
| Transaction history | GET `/api/accounts/{accountNumber}/transactions` | Account |
| Support ticket | POST `/api/support/tickets`; GET `/api/support/tickets/{reference}` | Support |

## Notes
- Transfer and bill payment use HTTP 202 to acknowledge an asynchronous or provider-dependent workflow.
- Bill payment currently stores a submitted request; a real biller validation/settlement adapter is not supplied by the BRD.
- Loan service stores an acknowledged application; underwriting, eligibility and disbursement rules are not specified by the BRD.
- Notification service is an internal record/adapter boundary; no real SMS/email provider credentials or integration were supplied.
- Statement service currently emits CSV; PDF and Excel output need document-generation requirements and templates.
- Account transaction history is a read-model contract placeholder until the transfer ledger event consumer is connected.
