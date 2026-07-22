package com.aykut.celtigar.rag;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Celtigar RAG Service.
 *
 * <p>Skeleton only: today it exposes a health check. Later phases add document
 * ingestion, chunking, embedding generation, vector storage (pgvector/Qdrant),
 * and a gRPC retrieval endpoint the Gateway/Orchestrator can call.
 */
@SpringBootApplication
public class RagServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RagServiceApplication.class, args);
    }
}
