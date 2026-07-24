# RAG Service

Celtigar's knowledge-base service (Java 21 / Spring Boot, Gradle).

**Phase 1 — RAG API (working).** A full ingest → chunk → embed → store → retrieve flow runs end to end, but on stand-in parts: a fake embedding model and an in-memory vector store, both behind interfaces. So the API works with no API key and no database. Phase 2 swaps the fakes for a real embedding model + pgvector, adds file upload (Tika), and makes ingestion async/batched for large files.

## Running

```bash
cd rag-service
make run          # or: ./gradlew bootRun
```

The service listens on **:8081** (the Gateway owns :8080).

```bash
# health
curl http://localhost:8081/healthz
# {"status":"ok","service":"rag-service"}

# ingest a document (raw text for now; file upload via Tika comes later)
curl -X POST http://localhost:8081/documents \
  -H "Content-Type: application/json" \
  -d '{"name":"handbook","text":"Employees get 20 days of annual leave..."}'
# {"documentId":"...","name":"handbook","chunks":1}

# retrieve the most similar chunks
curl "http://localhost:8081/search?q=how%20many%20leave%20days&k=3"
```

## Endpoints

| Method | Path | Purpose |
|--------|------|---------|
| `GET`  | `/healthz` | Liveness probe |
| `POST` | `/documents` | Ingest text: chunk → embed → store |
| `GET`  | `/search?q=&k=` | Embed the query, return top-k similar chunks |
| `GET`  | `/stats` | How many chunks are currently stored |

The pipeline runs behind interfaces (`Chunker`, `EmbeddingModel`, `VectorStore`) with
in-memory/fake implementations, so it works with no API key or database. Ingestion is
synchronous today — async/batched processing for large files is the next step.

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
