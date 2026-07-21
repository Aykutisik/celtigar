# Gateway

Celtigar's first service. Streams responses to the client over WebSocket. Not wired up to a real LLM provider yet — `internal/llm.EchoProvider` streams the incoming message back word by word to prove the flow works end to end. In Phase 1.5, `EchoProvider` will be replaced by a real OpenAI/Anthropic `Provider` implementation; thanks to the `Provider` interface, only one line in `main.go` will change.

## Running

```bash
cd gateway
go mod tidy   # downloads the gorilla/websocket dependency
go run .
```

Then open `http://localhost:8080/` in your browser — a minimal test client loads automatically and connects to `/ws`. Type a message and send it to see the response stream back token by token.

## Endpoints

- `GET /` — browser-based WebSocket test client (`testclient/index.html`)
- `GET /ws` — WebSocket endpoint; expects `{"message": "..."}` JSON, responds with `{"type": "token"|"done"|"error", "delta": "..."}` messages
- `GET /healthz` — health check

## Next Step

Split the LLM Router into its own service and put gRPC between it and the Gateway (see `docs/architecture.md` — Phase 1.75).
