# Celtigar

A polyglot AI orchestration platform combining an LLM gateway, a RAG pipeline, and a multi-agent workflow engine under one system, with a pluggable component architecture. Go powers the gateway, LLM router, and agent orchestrator; Java/Spring Boot powers the RAG service and component registry. Services communicate over gRPC internally, and stream to the client over WebSocket.

See `docs/architecture.md` for the full design and phased roadmap.

## Status

- [x] Phase 0/1 — Gateway (Go), first service with WebSocket streaming
- [ ] LLM Router
- [~] RAG Service (Java) — Phase 1 RAG API: in-memory ingest + search (`/documents`, `/search`) on :8081
- [ ] Agent Orchestrator
- [ ] Component Registry (Java)

## Structure

```
celtigar/
  gateway/            # Go — WebSocket entrypoint (first service, working)
  llm-router/          # Go — multi-provider LLM abstraction (not yet)
  agent-orchestrator/  # Go — workflow engine (not yet)
  rag-service/         # Java/Spring Boot — ingestion + embedding + retrieval (skeleton)
  component-registry/  # Java/Spring Boot — component catalog service (not yet)
  proto/               # shared .proto definitions (not yet)
  docs/                # architecture notes
```
