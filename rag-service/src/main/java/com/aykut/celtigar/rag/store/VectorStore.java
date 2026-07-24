package com.aykut.celtigar.rag.store;

import java.util.List;

/**
 * Stores chunk vectors and finds the ones nearest to a query vector. The in-memory
 * implementation is a stand-in; Phase 2 swaps in pgvector/Qdrant behind this interface.
 */
public interface VectorStore {

    /** Store one chunk's vector along with the source metadata needed for citation. */
    void add(String documentId, String documentName, String text, float[] vector);

    /** Return the {@code topK} stored chunks most similar to {@code query}, best first. */
    List<SearchHit> search(float[] query, int topK);

    /** Total number of chunks currently stored. */
    int size();
}
