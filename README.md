# sf-fds

Fraud detection HTTP API (`POST /api/v1/detect-frauds`).

## Authentication

Every request needs an API key in the `X-API-Key` header. Each calling service has its own key.
A missing or unknown key gets `401 Unauthorized`.

Only the SHA-256 hash of each key is configured, under `fds.api-keys`:

```properties
fds.api-keys[0].client=payments-gateway
fds.api-keys[0].sha256=<sha-256 hex of the key>
```

The app refuses to start if no keys are configured.

### Running locally

`application.properties` contains one well-known development key, and the app logs a warning at startup while it is active:

```bash
curl -X POST localhost:8080/api/v1/detect-frauds \
  -H 'X-API-Key: local-dev-api-key-do-not-use-in-production' \
  -H 'Content-Type: application/json' \
  -d '{"amount":125.50,"currency":"EUR","terminalId":"TERM-001","terminalThreatScore":42,"cardNumber":"4111111111111111"}'
```

### Real environments

Outside local development, set the key list with env vars. Setting the list anywhere replaces it entirely, so the development key is no longer accepted:

```bash
FDS_APIKEYS_0_CLIENT=payments-gateway
FDS_APIKEYS_0_SHA256=<hash>
FDS_APIKEYS_1_CLIENT=card-issuer
FDS_APIKEYS_1_SHA256=<hash>
```

Create a key and its hash with:

```bash
KEY=$(openssl rand -base64 32); echo "$KEY"; printf %s "$KEY" | shasum -a 256
```

Give the key to the calling service and configure only the hash.

To rotate a key, add a second entry for the same client with the new hash, move the caller to the new key, then remove the old entry.

## Architecture

A single Spring Boot service with three packages under `com.github.jnaalisv.sf_fds`. All state is in memory; there is no database yet.

```
http/                  HTTP layer
  FraudDetectionController       POST /api/v1/detect-frauds
  security/                      API key authentication (Spring Security)
domain/                Business logic, no HTTP or storage details
  frauddetection/                FraudDetectionService and the fraud detectors
  terminals/                     Terminal, TerminalRepository, TerminalTransactionCounter
  parameters/                    FraudDetectionParameters, FraudDetectionParametersRepository
persistence/           In-memory implementations of the domain repositories
```

### HTTP layer

`SecurityConfig` sets up a stateless filter chain (no sessions, CSRF, form login or HTTP Basic). `ApiKeyAuthenticationFilter` reads `X-API-Key`, and `ApiKeyRegistry` hashes it with SHA-256 and looks up the calling service by hash. A match authenticates the request as that client with role `API_CLIENT`; anything else stays anonymous and is rejected with `401` and a JSON error body. Every path except `/error` requires authentication.

`FraudDetectionController` maps the JSON body to a `FraudDetectionRequest` and returns the service's `FraudDetectionResponse` as JSON. `FraudDetectionRequest` validates itself in its constructor (required fields, `terminalThreatScore` between 0 and 100).

### Domain

`FraudDetectionService` loads the current `FraudDetectionParameters` and the `Terminal`, puts them in a `FraudDetectionContext`, and runs the fraud detectors in order. The first detector that returns a response wins; if none does, the result is `OK` with fraud score `0`. A denial is `DENIED` with fraud score `99`.

The detectors, in order:

1. `TerminalExistenceFraudDetector` denies with `unknown terminal` if the terminal is not in `TerminalRepository`.
2. `TerminalTxCountFraudDetector` denies with `Too many transactions` if the terminal has more than `transactionLimitPerTerminal` transactions in the last `transactionWindow`.

`TerminalTransactionCounter` keeps a sliding window of transaction timestamps per terminal (`TerminalTimestamps` in a `ConcurrentHashMap`, each list guarded by its own lock). Each call records the current transaction and then counts the ones inside the window, so the count includes the request being checked. Time comes from an injected `InstantSource` (`Clock.systemUTC()` in the app, a fixed clock in tests).

`FraudDetectionParametersRepository` and `TerminalRepository` are domain interfaces. Their implementations in `persistence/` are in memory: parameters are fixed at a 5 minute window and 100 transactions per terminal, and terminals are added with `InMemoryTerminalRepository.add`.

### Request flow

```
client
  │  POST /api/v1/detect-frauds, X-API-Key
  ▼
ApiKeyAuthenticationFilter ── unknown or missing key ──▶ 401
  │
  ▼
FraudDetectionController
  │  FraudDetectionRequest
  ▼
FraudDetectionService
  ├─ FraudDetectionParametersRepository.load()
  ├─ TerminalRepository.findById(terminalId)
  ├─ TerminalExistenceFraudDetector  ── unknown terminal ──▶ DENIED
  └─ TerminalTxCountFraudDetector
        └─ TerminalTransactionCounter ── over limit ──▶ DENIED
  │
  ▼
200 OK, FraudDetectionResponse (OK or DENIED)
```

Because the transaction counts live in memory, they are per instance and are lost on restart.
