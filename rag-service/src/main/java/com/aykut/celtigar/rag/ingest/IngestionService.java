package com.aykut.celtigar.rag.ingest;

import com.aykut.celtigar.rag.chunking.Chunker;
import com.aykut.celtigar.rag.embedding.EmbeddingModel;
import com.aykut.celtigar.rag.store.VectorStore;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Ties the ingestion pipeline together: chunk the text, embed each chunk, store it.
 *
 * <p>Synchronous and single-shot for now — a deliberate starting point. Large files
 * need async processing with status tracking and batched embedding (see the roadmap);
 * that is intentionally left as the next step.
 */
@Service
public class IngestionService {

    private final Chunker chunker;
    private final EmbeddingModel embeddingModel;
    private final VectorStore vectorStore;

    public IngestionService(Chunker chunker, EmbeddingModel embeddingModel, VectorStore vectorStore) {
        this.chunker = chunker;
        this.embeddingModel = embeddingModel;
        this.vectorStore = vectorStore;
    }

    /** Ingest one document's text and return its generated id and chunk count. */
    public IngestResult ingest(String name, String text) {
        String documentId = UUID.randomUUID().toString();
        List<String> chunks = chunker.chunk(text);
        for (String chunk : chunks) {
            float[] vector = embeddingModel.embed(chunk);
            vectorStore.add(documentId, name, chunk, vector);
        }
        return new IngestResult(documentId, name, chunks.size());
    }

    public record IngestResult(String documentId, String name, int chunks) {
    }
}
