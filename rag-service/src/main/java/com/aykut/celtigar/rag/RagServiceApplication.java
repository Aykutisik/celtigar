package com.aykut.celtigar.rag;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Celtigar RAG Service.
 *
 * <p>Early-phase plumbing: ingest text, chunk it, embed each chunk, store the
 * vectors, and retrieve the most similar chunks for a query. The embedding model
 * and vector store are fakes (in-memory) behind interfaces, so the whole flow
 * runs with no API key or database. Phase 2 swaps in a real embedding API and
 * pgvector, and makes ingestion async/batched for large files.
 */
@SpringBootApplication
public class RagServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RagServiceApplication.class, args);
    }
}
