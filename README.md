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
