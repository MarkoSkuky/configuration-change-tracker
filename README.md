# Configuration Change Tracker

## 1. Project overview

Configuration Change Tracker is a small Spring Boot backend for tracking domain-specific configuration changes such as credit limits, approval policies, and similar business rules.

The application allows you to:
- create a new configuration change
- retrieve changes by ID
- list changes with filters
- validate request payloads
- notify a simulated external monitoring service when a change is marked as critical

The data is stored in-memory, so no database is required.

---

## 2. Architecture / design

The project is organized into a simple layered architecture:

### Controller
The `controller` layer exposes REST endpoints and handles HTTP requests and responses.

### Service
The `service` layer contains the main business logic:
- validates change action rules
- maps request DTOs to domain models
- stores and retrieves data through the repository
- triggers monitoring notifications for critical changes

### Critical change handling
The `critical` flag is provided by the API client instead of being calculated internally.
The application assumes that the system performing the actual configuration change is also responsible for determining whether the change should be considered critical.
The tracker service simply stores this information and triggers the monitoring notification when required.
If business rules for criticality become centralized in the future, the logic could be moved into the service layer.

### Repository
The `repository` layer provides in-memory persistence using a map-based storage.

### DTO
The `dto` package contains request and response objects used for API communication.
This keeps the API contract separate from the internal domain model.

### Exception handling
The `exception` package contains custom exceptions and a global exception handler.
This converts application errors into clear HTTP responses with proper status codes.

---

## 3. Features

- **Create change**
  - supports `ADD`, `UPDATE`, and `DELETE`
  - validates old/new value combinations according to the action type

- **Get changes**
  - returns all stored configuration changes
  - supports filtering by:
    - `priority`
    - `ruleType`
    - `action`
    - date range (`from`, `to`)

- **Filtering**
  - list endpoint supports optional query parameters
  - date filtering works with `LocalDate` boundaries

- **Health endpoint**
  - exposed through Spring Boot Actuator at `/actuator/health`

- **Tests**
  - service tests cover positive and negative scenarios
  - integration test coverage for the controller flow

- **Monitoring notification**
  - critical changes trigger a simulated monitoring notification
  - implemented as a simulated external monitoring notification service

---

## 4. API endpoints

### Base path
`/configuration-changes`

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/configuration-changes` | Create a new configuration change |
| `GET` | `/configuration-changes` | List changes with optional filters |
| `GET` | `/configuration-changes/{id}` | Retrieve a specific change by ID |
| `GET` | `/actuator/health` | Health check endpoint |

### Query parameters for `GET /configuration-changes`
- `priority` — `LOW`, `MEDIUM`, `HIGH`
- `ruleType` — e.g. `CARD_LIMIT`, `APPROVAL_POLICY`, `CREDIT_LIMIT`
- `action` — `ADD`, `UPDATE`, `DELETE`
- `from` — date in `YYYY-MM-DD` format
- `to` — date in `YYYY-MM-DD` format

### Example request body for `POST /configuration-changes`
```json
{
  "clientId": "client-123",
  "clientPriority": "HIGH",
  "ruleType": "CARD_LIMIT",
  "changeAction": "UPDATE",
  "oldValue": "5000",
  "newValue": "10000",
  "critical": true
}
```

---

## 5. Validation & error handling

The application handles several edge cases and validation rules:

### Request validation
- `clientId` must not be blank
- `clientPriority`, `ruleType`, and `changeAction` must be provided
- malformed JSON request bodies are rejected

### Business rule validation
The service validates the values based on `changeAction`:
- **ADD** → `oldValue` must be `null`, `newValue` must be present
- **UPDATE** → both `oldValue` and `newValue` must be present
- **DELETE** → `oldValue` must be present, `newValue` must be `null`

### Error responses
The global exception handler returns clean JSON error responses for:
- missing configuration change ID (`404`)
- invalid request body (`400`)
- invalid request parameter values (`400`)
- validation errors (`400`)
- unexpected internal server errors (`500`)

---

## 6. How to run the app

### Requirements
- Java 21
- Maven Wrapper included in the project

### Start the application
```bash
./mvnw spring-boot:run
```

### Run tests
```bash
./mvnw test
```

### Access the app
- API base URL: `http://localhost:8080`
- Health endpoint: `http://localhost:8080/actuator/health`

### Example curl requests

Create a change:
```bash
curl -X POST http://localhost:8080/configuration-changes \
  -H "Content-Type: application/json" \
  -d '{
    "clientId":"client-123",
    "clientPriority":"HIGH",
    "ruleType":"CARD_LIMIT",
    "changeAction":"UPDATE",
    "oldValue":"5000",
    "newValue":"10000",
    "critical":true
  }'
```

List changes:
```bash
curl http://localhost:8080/configuration-changes
```

Get a change by ID:
```bash
curl http://localhost:8080/configuration-changes/1
```

Filter changes:
```bash
curl "http://localhost:8080/configuration-changes?ruleType=CARD_LIMIT&priority=HIGH"
```

