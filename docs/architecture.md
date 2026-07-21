# Celtigar — AI Platform

## Concept

Three capabilities live under one roof: an LLM gateway, a RAG (retrieval-augmented generation) knowledge base, and a multi-agent workflow engine, each as a separate service in the same platform. Together they're a smaller, personally-designed take on what real-world AI platforms (LiteLLM, Dify, LangChain Server) do. The "pluggable components" idea fits naturally here: every tool, retriever, or sub-agent is an independent service that implements a shared contract (a `.proto` definition) and can be added or removed later without touching core code.

Splitting Go and Java across layers makes sense: Go handles the low-latency, high-concurrency parts (gateway, streaming, agent execution); Java/Spring Boot handles the data-processing-heavy parts with strong ecosystem support (document ingestion, embedding pipelines). This split is both technically defensible and gives a clear answer to "why two languages" on a CV.

## Architecture

```
                        ┌─────────────────────┐
                        │   Client (Web/CLI)   │
                        └──────────┬───────────┘
                                   │ WebSocket (streaming) / REST
                        ┌──────────▼───────────┐
                        │   Gateway (Go)        │  ← auth, rate-limit, routing
                        └──────────┬───────────┘
                                   │ gRPC
              ┌────────────────────┼────────────────────┐
              │                    │                     │
   ┌──────────▼─────────┐ ┌────────▼────────┐  ┌─────────▼──────────┐
   │  LLM Router (Go)    │ │ Agent            │  │ RAG Service (Java/  │
   │  (OpenAI/Anthropic/ │ │ Orchestrator(Go) │  │ Spring Boot)        │
   │  local model, etc.) │ │                  │  │ ingestion+embedding │
   └─────────────────────┘ └────────┬─────────┘  └─────────┬──────────┘
                                     │ gRPC                 │
                          ┌──────────▼──────────┐  ┌────────▼────────┐
                          │ Component Registry   │  │ Postgres+pgvector│
                          │ (tool/agent catalog)  │  │ / Qdrant         │
                          └──────────────────────┘  └─────────────────┘
```

Every component (web-search tool, calculator, sub-agent, retriever...) implements a shared `.proto` contract and registers itself with the Component Registry. The orchestrator discovers available components from the registry and invokes them at runtime — adding a new component means writing and deploying an independent service and registering it, not touching core code.

## Technology Choices Per Service

**Gateway (Go).** The single point of contact with clients. Streams token-by-token responses over WebSocket, serves synchronous requests over REST. This is the WebSocket topic from the learning roadmap put into practice.

**LLM Router (Go).** Abstracts different providers (OpenAI, Anthropic, a local model) behind a common interface; handles retry, fallback, and cost tracking. Talks to the Gateway over gRPC — the roadmap's gRPC topic in practice.

**Agent Orchestrator (Go).** Runs multi-step agent flows (plan → call tool → evaluate result → continue). Looks up whichever component it needs from the registry at each step and calls it over gRPC.

**RAG Service (Java/Spring Boot).** Handles document upload, chunking, embedding generation, and writing to the vector store. Spring's ecosystem support for file processing and batch jobs is useful here. Exposes a gRPC endpoint for querying; the Gateway or Orchestrator retrieves through it. (Detailed breakdown below, under "RAG Service — Detailed Responsibilities.")

**Component Registry (Java/Spring Boot).** The catalog service tracking which components are alive and what interface they implement — essentially a CRUD service plus service discovery. Keeping it in Java rather than Go adds to the Java footprint and is a low-risk service to write quickly with Spring Data.

### RAG Service — Detailed Responsibilities

If you want to write more Java, this is the place: the RAG Service isn't a single "embed and store" script, it's a pipeline with several sub-components:

- **Ingestion:** a layer parsing PDF, DOCX, Markdown, and plain text formats (Apache Tika is a solid choice). A file upload endpoint (REST/gRPC) writes document metadata (title, source, upload date) to the database.
- **Chunking:** splits text along meaningful boundaries (paragraphs, headings) with overlap, rather than fixed-size cuts. Making different chunking strategies (fixed-size, sentence-based, recursive) pluggable is a nice design decision.
- **Embedding generation:** calls an embedding model (OpenAI's embeddings API or a local model) per chunk to produce a vector; needs batching and retry logic.
- **Storage:** writes vectors plus metadata to pgvector or Qdrant, keeping track of which document/chunk each vector came from for later citation.
- **Retrieval:** embeds the incoming query and runs a similarity search, optionally followed by a reranking step (e.g. a cross-encoder) to improve results.
- **Citation tracking:** links every returned chunk back to its original document and location, so the Agent Orchestrator can say "this answer came from that source."
- **Re-indexing:** a background job (via Spring's scheduling support) that periodically reprocesses documents and refreshes embeddings.
- **Management API:** CRUD endpoints for creating/deleting knowledge bases, listing documents, and access control — another area where Spring Boot is strong.

With this much responsibility, the RAG Service stops being a small platform service and becomes a sub-system of its own — most of the Java codebase will live here.

**Shared proto layer.** `.proto` files under `/proto` define interfaces like Tool, Agent, and Retriever — both Go and Java services generate code from them (via protoc). This stands out on a CV as "cross-language service contract design."

## Repo Structure

```
celtigar/
  proto/                 # shared .proto definitions (Tool, Agent, Retriever)
  gateway/                # Go — HTTP/WebSocket entrypoint
  llm-router/             # Go — multi-provider LLM abstraction
  agent-orchestrator/     # Go — workflow engine
  rag-service/            # Java/Spring Boot — ingestion + embedding + retrieval
  component-registry/     # Java/Spring Boot — component catalog service
  components/             # example pluggable components (web-search, calculator...)
  infra/                  # docker-compose, k8s manifests
  docs/                   # architecture decisions (ADRs), diagrams
```

## Phased Roadmap

Build incrementally rather than all at once — each phase is independently demoable and leaves a meaningful commit history on GitHub.

**Phase 0/1 — Core gateway + streaming (done).** Single Go service, WebSocket streaming, a fake (echo) LLM provider to validate the flow end to end.

**Phase 1.5 — Real LLM provider.** Replace `EchoProvider` with a real OpenAI/Anthropic `Provider` implementation.

**Phase 1.75 — LLM Router split.** Pull the LLM Router out into its own Go service, put gRPC between it and the Gateway.

**Phase 2 — RAG integration (3-4 weeks).** Stand up the RAG service in Java/Spring Boot (pgvector or Qdrant), connect it to the Gateway over gRPC. The platform can now do both chat and knowledge-grounded Q&A.

**Phase 3 — Agent orchestrator + component registry (4+ weeks).** Add multi-step agent flows, write the first pluggable components (e.g. a calculator tool, a web-search tool), and build the dynamic registration mechanism.

**Phase 4 — Observability and deploy (optional, advanced).** Distributed tracing across Go+Java services with OpenTelemetry, Prometheus/Grafana. If you want, this is also where the "build Docker from scratch" exercise we discussed earlier could come in — running the platform on your own minimal container runtime adds a rare depth to the project.

## Component Catalog

Pluggable components fall into six categories.

**Tools.** Functions an agent can call: web search, sandboxed code execution, a SQL query runner, file read/write, email/calendar operations, a general-purpose HTTP "API caller."

**Retrievers.** Data sources for the RAG side: vector database queries (pgvector/Qdrant), live web scraping, structured data (CSV/SQL) queries, a repository retriever that indexes and queries a code repo.

**Sub-agents.** Agents focused on a specific job, callable by the orchestrator: a researcher agent, a coding/reviewing agent, a summarizer agent, a planner agent.

**Model adapters.** Connectors exposing different LLM/embedding providers behind a common interface: OpenAI, Anthropic, a local model (Ollama/llama.cpp), an embedding model adapter.

**Guardrails.** Middleware inspecting input/output: content moderation, PII redaction, cost/rate-limit protection, an output schema validator (e.g. JSON schema checks).

**Integrations.** Components connecting to external services: Slack/Discord notifications, GitHub issue/PR creation, Jira/Linear connectivity, webhook triggers.

### Shared Component Interface

For a component to register with the registry and be discoverable at runtime, it implements this manifest (think of it as a proto definition):

```protobuf
message ComponentManifest {
  string name = 1;              // e.g. "web-search"
  string category = 2;          // tool | retriever | agent | model | guardrail | integration
  string description = 3;       // so the agent knows when to call this component
  Schema input_schema = 4;      // expected input fields
  Schema output_schema = 5;     // shape of the returned output
  string version = 6;
}

service Component {
  rpc Describe(Empty) returns (ComponentManifest);
  rpc Invoke(InvokeRequest) returns (InvokeResponse);
}
```

Adding a new component is just writing an independent service that implements these two RPCs and registering it with the Component Registry — no changes to the orchestrator or core code required.

## CV Bullet Point Suggestions

Short version:

- Designed and built a modular, polyglot AI orchestration platform (Go + Java) combining an LLM gateway, RAG pipeline, and multi-agent workflow engine with a pluggable component architecture communicating over gRPC.

Detailed version (two bullets):

- Architected a distributed AI platform with a Go-based API gateway (WebSocket streaming, gRPC service mesh) and a Java/Spring Boot RAG pipeline (vector search with pgvector), enabling real-time LLM responses grounded in a custom knowledge base.
- Implemented a plugin system using shared Protocol Buffer contracts, allowing tools and sub-agents to be registered and invoked dynamically at runtime across a cross-language backend — no core code changes required to add new capabilities.

## GitHub Repo Setup

Monorepo (single repo, multiple services) — one issue tracker, one CI pipeline, one commit history; each service still builds independently via its own `go.mod`/`pom.xml`.

1. Create an empty `celtigar` repo on GitHub.
2. `git init`, `git add .`, first commit: `chore: scaffold monorepo structure + gateway websocket skeleton`.
3. `git remote add origin`, `git push -u origin main`.
4. From here, push each phase as its own commit/PR series.
