package com.aykut.celtigar.rag.embedding;

/**
 * Turns text into a vector whose geometry reflects meaning: similar texts should
 * produce nearby vectors. The same model must be used for both indexing and
 * querying, or the vectors are not comparable.
 *
 * <p>Today a fake implementation backs this. Phase 2 adds a real one calling an
 * embedding API (OpenAI) or a local model (Ollama) — callers do not change.
 */
public interface EmbeddingModel {

    /** Produce the embedding vector for {@code text}. */
    float[] embed(String text);

    /** Fixed length of every vector this model produces. */
    int dimension();
}
