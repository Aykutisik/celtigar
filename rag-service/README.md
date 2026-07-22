# RAG Service

Celtigar's knowledge-base service (Java 21 / Spring Boot, Gradle). **Skeleton stage** — today it only exposes a health check to prove the service builds and runs inside the monorepo. The full pipeline (ingestion, chunking, embedding, vector storage, retrieval) arrives in Phase 2.

## Running

```bash
cd rag-service
make run          # or: ./gradlew bootRun
```

The service listens on **:8081** (the Gateway owns :8080). Verify it:

```bash
curl http://localhost:8081/healthz
# {"status":"ok","service":"rag-service"}
```

## Make targets

| Command | What it does |
|---------|--------------|
| `make run`   | Build and run (`gradlew bootRun`) |
| `make build` | Compile and package into `build/libs` |
| `make test`  | Run tests |
| `make clean` | Remove build artifacts |

## Layout

```
rag-service/
  build.gradle                              # dependencies + plugins
  settings.gradle
  gradlew, gradle/wrapper/                  # pinned Gradle version (no global install needed)
  src/main/java/com/aykut/celtigar/rag/
    RagServiceApplication.java              # Spring Boot entry point
    web/HealthController.java               # GET /healthz
  src/main/resources/application.yml        # port 8081, service name
```

## Planned (Phase 2+)

- **Ingestion** — parse PDF/DOCX/Markdown (Apache Tika), store document metadata.
- **Chunking** — pluggable strategies (fixed-size, sentence, recursive) with overlap.
- **Embedding** — batch + retry against an embedding model.
- **Storage** — pgvector / Qdrant, with source/chunk tracking for citations.
- **Retrieval** — similarity search + optional reranking, exposed over gRPC to the Gateway/Orchestrator.

See `docs/architecture.md` for the full design.
